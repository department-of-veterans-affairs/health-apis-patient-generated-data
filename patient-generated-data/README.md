# patient-generated-data

Main application.

## Local Development

### Config

`../make-configs.sh`

Use `less config/application-dev.properties` to verify application properties for local development.

### Build

Full build:

`mvn clean install`

Build without additional formatting, code coverage enforcement, static code analysis, integration tests, etc:

`mvn -P'!standard' package`

Start application:

`java -Dspring.profiles.active=dev -jar target/patient-generated-data-${VERSION}.jar`
