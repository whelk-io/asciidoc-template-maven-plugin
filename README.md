# asciidoc-template-maven-plugin

GitHub support for Asciidoctor on `README.adoc` does not include the `include::` syntax. This plugin reads a templated file and inserts the related markup wherever `inlude::` is found.

[![CodeFactor](https://www.codefactor.io/repository/github/whelk-io/asciidoc-template-maven-plugin/badge/master)](https://www.codefactor.io/repository/github/whelk-io/asciidoc-template-maven-plugin/overview/master) ![deploy](https://github.com/whelk-io/asciidoc-template-maven-plugin/workflows/deploy/badge.svg?branch=master) [![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=whelk-io/asciidoc-template-maven-plugin)](https://dependabot.com)

## Usage

**given template file**

`src/docs/README.adoc`

```
= Some File

include::otherFile.adoc

= Footer
```

**and include file**

`src/docs/otherFile.adoc`
```
== Other File

Other documentation
```

**when running asciidoc-template**

`mvn asciidoc-template::build`

**then output file contains**

`./README.adoc`

```
= Some File

== Other File

Other documentation

= Footer
```

## Maven Configuration

### Basic

```xml
<plugin>
	<groupId>io.whelk.asciidoc</groupId>
	<artifactId>asciidoc-template-maven-plugin</artifactId>
</plugin>
```

### Full

```xml
<plugin>
	<groupId>io.whelk.asciidoc</groupId>
	<artifactId>asciidoc-template-maven-plugin</artifactId>
	<configuration>
		<templateDirectory>src/docs</templateDirectory>
		<templateFile>README.adoc</templateFile>
		<outputDirectory>./</outputDirectory>
		<outputFile>README.adoc</outputFile>
	</configuration>
</plugin>
```

### Plugin Configuration

| Property | Description |
|----------|-------------|
|`templateDirectory`|Directory to find template asiidocs (.adoc). Defaults to `src/docs`|
|`templateFile`|Root file to compile into asciidoc. Defaults to `README.adoc`|
|`outputDirectory`|Directory to write compiled asciidoc. Defaults to `./`|
|`outputFile`|Output of `templateFile`. Defaults to `templateFile`|


## Maven Settings

**~/.m2/settings.xml**

````xml
<settings>

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <pluginRepositories>
        <pluginRepository>
          <id>github-asciidoc-template-maven-plugin</id>
          <url>https://maven.pkg.github.com/whelk-io/asciidoc-template-maven-plugin</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github-asciidoc-template-maven-plugin</id>
      <username>GITHUB_USERNAME</username>
      <password>PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>

</settings>
````

**pom.xml**

````xml
<pluginRepositories>
	<pluginRepository>
		<id>github-asciidoc-template-maven-plugin</id>
		<url>https://maven.pkg.github.com/whelk-io/asciidoc-template-maven-plugin</url>
	</pluginRepository>
</pluginRepositories>
````
