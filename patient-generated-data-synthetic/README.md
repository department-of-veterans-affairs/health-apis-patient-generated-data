# patient-generated-data-synthetic

Synthetic data for local and development environments.
This project also builds a SQL Server Docker image
for local development, and an H2 database for use by integration tests.

## Local Development

`local-db.sh`

Use `docker container ls` to verify that the `pgd-db` image is running.

The resulting SQL Server instance is available at `localhost:1633`,
username `SA` and password `<YourStrong!Passw0rd>`.
