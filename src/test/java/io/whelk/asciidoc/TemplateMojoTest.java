package io.whelk.asciidoc;

import io.whelk.asciidoc.TemplateMojo.PathAndOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateMojoTest {

    // tag::exampleShort[]
    String test = "this is a small test";
    // end::exampleShort[]

    @Test
    void filePathExtraction() {
        TemplateMojo templateMojo = new TemplateMojo();

        assertThat(templateMojo.extractPathAndOptions("include::otherFile.adoc[]"))
                .isEqualTo(new PathAndOptions("otherFile.adoc", Map.of()));

        assertThat(templateMojo.extractPathAndOptions("include::src/test/java/io/whelk/asciidoc/TemplateMojoTest.java[tag=exampleShort]"))
                .isEqualTo(new PathAndOptions("src/test/java/io/whelk/asciidoc/TemplateMojoTest.java", Map.of("tag", "exampleShort")));
    }

    @Test
    void includeJavaCode() {
        TemplateMojo templateMojo = new TemplateMojo();
        templateMojo.templateDirectory = "./";

        assertThat(templateMojo.updateIncludeLine("include::src/test/java/io/whelk/asciidoc/TemplateMojoTest.java[tag=exampleShort]"))
                .containsOnly("    String test = \"this is a small test\";");
    }

    @Test
    void loadVars() {
        TemplateMojo templateMojo = new TemplateMojo();
        Map<String, String> vars = templateMojo.loadVars(List.of(
                "My Doc",
                "::",
                "",
                ":: 2",
                ":myVar: 4",
                ":noSpace:5",
                ":baseDir: relativeDirectory",
                "some content",
                ":Here you can see a weirdly used colon",
                ":dash-var: dv",
                ":level-offset: +1",
                ":level-offset: -1"
        ));
        Map<String, String> expected = Map.of(
                "myVar", "4",
                "noSpace", "5",
                "baseDir", "relativeDirectory",
                "dash-var", "dv",
                "level-offset", "-1");
        assertThat(vars).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void variableInPath() {
        TemplateMojo templateMojo = new TemplateMojo();
        Map<String, String> vars = templateMojo.loadVars(List.of(":rootDir: rootHere",
                ":another: hereAlso"));
        templateMojo.vars = vars;

        assertThat(templateMojo.extractPathAndOptions("include::{rootDir}/{another}/intoThis[]").getPath())
                .isEqualTo("rootHere/hereAlso/intoThis");
    }

}
