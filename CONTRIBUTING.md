# Contributing to io.whelk

## Install in Local .m2

`./mvnw clean install`

## Publish to Maven Central

`./mvnw clean deploy -s settings.xml -DskipTests`

## Settings.xml

````xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          https://maven.apache.org/xsd/settings-1.0.0.xsd">

    <servers>
        <server>
            <id>ossrh</id>
            <username>{ossrh.username}</username>
            <password>{ossrh.password}</password>
        </server>
    </servers>

    <activeProfiles>
        <activeProfile>github</activeProfile>
    </activeProfiles>

    <profiles>
        <profile>
            <id>github</id>
            <repositories>
                <repository>
                    <id>ossrh</id>
                    <url>https://oss.sonatype.org/service/local/staging/deploy/maven2/</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
            </repositories>
        </profile>
    </profiles>

</settings>

````

## Base64 GPG Private Key

Find private key

`gpg -K`

Convert private key to base64

`gpg --export-secret-keys {{key-id}} | base64 > private.key`
