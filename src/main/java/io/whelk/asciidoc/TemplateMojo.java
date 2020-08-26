package io.whelk.asciidoc;

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

import lombok.Value;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import lombok.SneakyThrows;
import org.assertj.core.util.VisibleForTesting;

@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE)
public class TemplateMojo extends AbstractMojo {

    public static final String TAG = "tag";

    public static final String TAG_END = "end";

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

    @SneakyThrows
    public void execute() throws MojoExecutionException, MojoFailureException {
        setDefaultConfiguration();

        final var lines = this.readLines(templateDirectory, templateFile);
        final var updatedLines = this.updateLines(lines);

        Files.write(Paths.get(outputDirectory, outputFile), updatedLines);
    }

    @SneakyThrows
    private List<String> readLines(String first, String... more) {
        return new ArrayList<>(Files
                .readAllLines(Paths.get(first, more)));
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

    @VisibleForTesting
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
            boolean foundStart = x.contains(TAG + "::" + tag);
            boolean foundEnd = x.contains(TAG_END + "::" + tag);
            if (!startHasBeenReached.get()) {
                startHasBeenReached.set(foundStart);
            }
            if (startHasBeenReached.get() && !endHasBeenReached.get()) {
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

    @VisibleForTesting
    PathAndOptions extractPathAndOptions(String line) {
        int pathStart = 9;
        Pattern pattern = Pattern.compile("\\[.*\\]$");
        Matcher matcher = pattern.matcher(line);
        boolean found = matcher.find();
        String[] allOptions = matcher.group().replaceAll("[\\[\\]]", "").split(",");
        Map<String, String> optionMap = Arrays.asList(allOptions).stream()
                .filter(x -> x.trim().length() > 0)
                .map(x -> x.split("="))
                .collect(Collectors.toMap(x -> x[0], x -> x[1]));
        int pathEnd = matcher.start();
        String path = line.substring(pathStart, pathEnd);
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