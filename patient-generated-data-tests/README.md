# patient-generated-data-tests

Integration tests docker image.
Tests are executed during the standard build.
Additional scripts:

- Use `local-docker-image.sh` to build and run the integration test docker image
- Use `start-processes-for-integration-tests.sh` to launch the application, as configured for local integration testing
- Use `synthetic-refresh.sh` to create or update the [synthetic records](../patient-generated-data-synthetic/README.md)
  in a development environment
