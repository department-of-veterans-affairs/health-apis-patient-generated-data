# patient-generated-data

Main application.

## Local Development

### Config

`../make-configs.sh`

Use `less config/application-dev.properties` to verify application properties for local development.

### Build

To run full build:

`mvn clean install`

To run build without additional formatting, code coverage enforcement, static code analysis, integration tests, etc., disable the `standard` profile:

`mvn -P'!standard' package`

Start Java app:

`java -Dspring.profiles.active=dev -jar target/patient-generated-data-${VERSION}.jar`
