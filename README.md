# asciidoc-template-maven-plugin

GitHub support for Asciidoctor on `README.adoc` does not include the `include::` syntax. This plugin reads a templated file and inserts the related markup wherever `include::` is found. Requires Java 11 at a minimum.

[![CodeFactor](https://www.codefactor.io/repository/github/whelk-io/asciidoc-template-maven-plugin/badge/master)](https://www.codefactor.io/repository/github/whelk-io/asciidoc-template-maven-plugin/overview/master) ![release](https://github.com/whelk-io/asciidoc-template-maven-plugin/workflows/release/badge.svg) ![CodeQL](https://github.com/whelk-io/asciidoc-template-maven-plugin/workflows/CodeQL/badge.svg)

## Basic Usage

**given template file**

`src/docs/README.adoc`

```asciidoc
= Some File

include::otherFile.adoc[]

= Footer
```

**and file to merge into template**

`src/docs/otherFile.adoc`
```asciidoc
== Other File

Other documentation
```

**when running asciidoc-template**

`mvn asciidoc-template::build`

**then output file contains**

`./README.adoc`

```asciidoc
= Some File

== Other File

Other documentation

= Footer
```

## Including Other Files

**given template file**

```asciidoc
= Some File

[source,java]
----
include::src/main/java/com/example/Demo.java[tag=snippet]
----

= Footer
```

**and a source file with segment tag**

```Java
package com.example;

public class Demo {

  // tag::snippet[]
  public void run() {
    log.info("running");
  }
  // end::snippet[]

}
````

**when running asciidoc-template**

`mvn asciidoc-template::build`

**then output file contains**

```asciidoc
= Some File

[source,java]
----
  public void run() {
    log.info("running");
  }
----

= Footer
```

## Maven Configuration

### Basic

```xml
<plugin>
	<groupId>io.whelk.asciidoc</groupId>
	<artifactId>asciidoc-template-maven-plugin</artifactId>
	<version>${asciidoc-template.version}</version>
</plugin>
```

> Check the available versions on [Maven Central](https://search.maven.org/artifact/io.whelk.asciidoc/asciidoc-template-maven-plugin)

### Full

```xml
<plugin>
	<groupId>io.whelk.asciidoc</groupId>
	<artifactId>asciidoc-template-maven-plugin</artifactId>
	<configuration>
		<templateDirectory>src/docs</templateDirectory>
		<templateFile>README-template.adoc</templateFile>
		<outputDirectory>./</outputDirectory>
		<outputFile>README.adoc</outputFile>
	</configuration>
</plugin>
```

### Plugin Configuration

| Property | Description |
|----------|-------------|
|`templateDirectory`|Directory to find template asiidocs (.adoc). Defaults to `src/docs`|
|`templateFile`|Root file to compile into asciidoc. Defaults to `README-template.adoc`|
|`outputDirectory`|Directory to write compiled asciidoc. Defaults to `./`|
|`outputFile`|Output of building from `templateFile`. Defaults to `templateFile` without `-template`.|

