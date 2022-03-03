#!/usr/bin/env bash

cd $(readlink -f $(dirname $0))

echo "Stopping existing database container"
docker stop "pgd-db"
echo "Removing existing database container"
docker rm "pgd-db"

docker pull mcr.microsoft.com/mssql/server:2017-latest

echo "Creating new database container"
docker run \
  --name "pgd-db" \
  -e 'ACCEPT_EULA=Y' \
  -e "SA_PASSWORD=<YourStrong!Passw0rd>" \
  -p 1633:1433 \
  -d mcr.microsoft.com/mssql/server:2017-latest

mvn clean install -Ppopulaterator -P'!standard' -Dexec.cleanupDaemonThreads=false
