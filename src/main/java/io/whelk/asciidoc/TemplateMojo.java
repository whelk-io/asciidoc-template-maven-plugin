package io.whelk.asciidoc;

import lombok.SneakyThrows;
import lombok.Value;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE)
public class TemplateMojo extends AbstractMojo {

    private static final String TAG = "tag";

    private static final String TAG_END = "end";

    @Parameter(property = "templateDirectory")
    String templateDirectory;

    @Parameter(property = "templateFile")
    String templateFile;

    @Parameter(property = "outputDirectory")
    String outputDirectory;

    @Parameter(property = "outputFile")
    String outputFile;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    // @VisibleForTesting
    Map<String, String> vars = Map.of();

    @SneakyThrows
    public void execute() throws MojoExecutionException, MojoFailureException {
        setDefaultConfiguration();

        final var lines = this.readLines(templateDirectory, templateFile);
        this.vars = loadVars(lines);
        final var updatedLines = this.updateLines(lines);

        Files.write(Paths.get(outputDirectory, outputFile), updatedLines);
    }

    @SneakyThrows
    private List<String> readLines(String first, String... more) {
        return new ArrayList<>(Files
                .readAllLines(Paths.get(first, more)));
    }

    // @VisibleForTesting
    Map<String, String> loadVars(List<String> strings) {
        String varRegex = "^:[\\w\\-]+:"; //includes dashes
        Pattern compile = Pattern.compile(varRegex);

        return strings.stream()
                .map(x -> {
                    Matcher matcher = compile.matcher(x);
                    if (matcher.find()) {
                        int end = matcher.end();
                        String varName = matcher.group().trim();
                        String trimMarkers = varName.substring(1, varName.length() - 1).trim();
                        String value = x.substring(end).trim();
                        return List.of(trimMarkers, value);
                    } else {
                        return List.<String>of();
                    }
                })
                .filter(x -> x.size() == 2)
                // when merging duplicates, last entry wins
                .collect(toMap(x -> x.get(0), x -> x.get(1), (first, second) -> second));
    }

    private List<String> updateLines(List<String> lines) {
        return lines
                .stream()
                .map(this::updateLine)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<String> updateLine(final String line) {
        if (matchesIncludeLine(line))
            return updateIncludeLine(line);

        return List.of(line);
    }

    private boolean matchesIncludeLine(final String line) {
        return line.startsWith("include::") &&
                line.endsWith("]");
    }

    // @VisibleForTesting
    List<String> updateIncludeLine(final String line) {
        var pathAndOptions = extractPathAndOptions(line);
        if (pathAndOptions.optionMap.containsKey(TAG)) {
            return readTaggedLines(templateDirectory, pathAndOptions);
        }
        return this.readLines(templateDirectory, pathAndOptions.path);
    }

    @SneakyThrows
    private List<String> readTaggedLines(String templateDirectory, PathAndOptions path) {
        ArrayList<String> lines = new ArrayList<>(Files
                .readAllLines(Paths.get(templateDirectory, path.path)));
        String tag = path.optionMap.get(TAG);
        AtomicReference<Boolean> startHasBeenReached = new AtomicReference<>(false);
        AtomicReference<Boolean> endHasBeenReached = new AtomicReference<>(false);
        List<String> taggedLines = lines.stream().filter(x -> {
            boolean foundStart = false;
            boolean foundEnd = false;
            if (!startHasBeenReached.get()) {
                foundStart = x.contains(TAG + "::" + tag);
                startHasBeenReached.set(foundStart);
            }
            if (startHasBeenReached.get() && !endHasBeenReached.get()) {
                foundEnd = x.contains(TAG_END + "::" + tag);
                endHasBeenReached.set(foundEnd);
            }
            boolean thisIsATagLine = foundStart || foundEnd;
            return !thisIsATagLine && startHasBeenReached.get() && !endHasBeenReached.get();
        }).collect(Collectors.toList());
        return taggedLines;
    }

    @Value
    static
    class PathAndOptions {
        String path;
        Map<String, String> optionMap;
    }

    // @VisibleForTesting
    PathAndOptions extractPathAndOptions(String line) {
        int pathStart = 9;
        Pattern pattern = Pattern.compile("\\[.*\\]$");
        Matcher matcher = pattern.matcher(line);
        matcher.find();
        String[] allOptions = matcher.group().replaceAll("[\\[\\]]", "").split(",");
        Map<String, String> optionMap = Arrays.asList(allOptions).stream()
                .filter(x -> x.trim().length() > 0)
                .map(x -> x.split("="))
                .collect(toMap(x -> x[0], x -> x[1]));
        int pathEnd = matcher.start();
        String path = line.substring(pathStart, pathEnd);
        for (var variable : this.vars.entrySet()) {
            String needle = "\\{" + variable.getKey() + "\\}";
            path = path.replaceAll(needle, variable.getValue());
        }
        return new PathAndOptions(path, optionMap);
    }

    private void setDefaultConfiguration() {

        if (templateDirectory == null || templateDirectory.isBlank())
            templateDirectory = "src/docs";

        if (templateFile == null || templateFile.isBlank())
            templateFile = "README-template.adoc";

        if (outputDirectory == null || outputDirectory.isBlank())
            outputDirectory = "./";

        if (outputFile == null || outputFile.isBlank())
            outputFile = templateFile.replace("-template", "");

    }

}
