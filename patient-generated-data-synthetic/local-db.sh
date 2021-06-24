#!/usr/bin/env bash

cd $(readlink -f $(dirname $0))

echo "Stopping existing database container"
docker stop "pgd-db"
echo "Removing existing database container"
docker rm "pgd-db"

docker pull docker pull cockroachdb/cockroach

echo "Creating new database container"
docker run \
  --name "pgd-db" \
  -e 'ACCEPT_EULA=Y' \
  -e "SA_PASSWORD=<YourStrong!Passw0rd>" \
  -p 1633:1433 \
  -d cockroachdb/cockroach

mvn clean install -Ppopulaterator -P'!standard' -Dexec.cleanupDaemonThreads=false
