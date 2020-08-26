package io.whelk.asciidoc;

import io.whelk.asciidoc.TemplateMojo.PathAndOptions;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateMojoTest {

    // tag::exampleShort[]
    String test = "this is a small test";
    // end::exampleShort[]

    @Test
    void testFilePathExtraction() {
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

}
