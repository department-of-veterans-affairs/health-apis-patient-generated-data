# patient-generated-data-local-db

This project builds a SQL Server Docker image
for local development. This is the operational database for
Patient Generated Data.
An H2 database instance is also built for use by integration tests.

## Local Development

`local-db.sh`

Use `docker container ls` to verify that the `pgd-db` image is running.

The resulting SQL Server instance is available at `localhost:1633`,
username `SA` and password `<YourStrong!Passw0rd>`.

To resolve
`error reading liquibase-core.jar; zip END header not found`,
download the artifact from
[Maven Central](https://search.maven.org/artifact/org.liquibase/liquibase-core/3.8.9/jar)
and install manually:

```
mvn install:install-file \
  -Dfile=/path/to/liquibase-core-3.8.9.jar \
  -DgroupId=org.liquibase \
  -DartifactId=liquibase-core \
  -Dversion=3.8.9 \
  -Dpackaging=jar
```
