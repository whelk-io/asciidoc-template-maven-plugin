# Contributing to io.whelk

## Install in Local .m2

`./mvnw clean install`

## Publish to Maven Central

`./mvnw clean`

`./mvnw --settings settings.xml release:prepare -Pmaven-central`

`./mvnw --settings settings.xml release:perform -Pmaven-central`

## Settings.xml

````xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          https://maven.apache.org/xsd/settings-1.0.0.xsd">

    <servers>
        <server>
            <id>ossrh</id>
            <username>{sonatype.username}</username>
            <password>{sonatype.password}</password>
        </server>
    </servers>

    <profiles>
        <profile>
            <id>ossrh</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <gpg.passphrase>{gpg.passphrase}</gpg.passphrase>
            </properties>
        </profile>
    </profiles>

</settings>

````