package io.whelk.asciidoc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import lombok.SneakyThrows;

@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE)
public class TemplateMojo extends AbstractMojo {

    @Parameter(property = "templateDirectory")
    String templateDirectory;

    @Parameter(property = "templateFile")
    String templateFile;

    @Parameter(property = "outputDirectory")
    String outputDirectory = "./";

    @Parameter(property = "outputFile")
    String outputFile;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @SneakyThrows
    public void execute() throws MojoExecutionException, MojoFailureException {
        setDefaultConfiguration();

        final var lines = this.readLines("src/docs/README.adoc");
        final var updatedLines = this.updateLines(lines);

        Files.write(Paths.get("./README.adoc"), updatedLines);
    }

    @SneakyThrows
    private List<String> readLines(String first, String... more) {
        return Files
                .readAllLines(Paths.get(first, more))
                .stream()
                .collect(Collectors.toList());
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
               line.endsWith(".adoc[]");
    }

    private List<String> updateIncludeLine(final String line) {
        var path = line.substring(9, line.length() - 2);
        return this.readLines(templateDirectory, path);
    }

    private void setDefaultConfiguration() {
        if (templateDirectory == null || templateDirectory.isBlank())
            templateDirectory = "src/docs";

        if (templateFile == null || templateFile.isBlank())
            templateFile = "README.adoc";

        if (outputDirectory == null || outputDirectory.isBlank())
            outputDirectory = "./";

        if (outputFile == null || outputFile.isBlank())
            outputFile = templateFile;
    }

}