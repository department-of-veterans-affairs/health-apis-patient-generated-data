# patient-generated-data

Main application.

## Local Development

### Config

`../make-configs.sh`

Use `less config/application-dev.properties` to verify application properties for local development

A local database instance with PGD schema is required; see
[patient-generated-data-synthetic](../patient-generated-data-synthetic)

### Build

To resolve `error reading liquibase-core.jar; zip END header not found`,
execute `../initialize-build.sh`

Full build:

`mvn clean install`

Build without additional formatting, code coverage enforcement, static code analysis, integration tests, etc:

`mvn -P'!standard' package`

Start application:

`java -Dspring.profiles.active=dev -jar target/patient-generated-data-${VERSION}.jar`
