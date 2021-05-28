# patient-generated-data-synthetic

Synthetic data for local and development environments.
This project also builds a SQL Server Docker image
for local development, and an H2 database for use by integration tests.

## Local Development

`local-db.sh`

Use `docker container ls` to verify that the `pgd-db` image is running.

The resulting SQL Server instance is available at `localhost:1633`,
username `SA` and password `<YourStrong!Passw0rd>`.

To resolve
`error reading liquibase-core.jar; zip END header not found`,
execute the `initialize-build.sh` script or download the artifact from
[Maven Central](https://search.maven.org/artifact/org.liquibase/liquibase-core/4.3.5/jar)
and install manually:

```
mvn install:install-file \
  -Dfile=/path/to/liquibase-core-4.3.5.jar \
  -DgroupId=org.liquibase \
  -DartifactId=liquibase-core \
  -Dversion=4.3.5 \
  -Dpackaging=jar
```

