#!/usr/bin/env bash

cd $(readlink -f $(dirname $0))

echo 'stop and remove pgd-db'
docker stop 'pgd-db' || true && docker rm 'pgd-db' || true

SQL_SERVER_IMAGE=mcr.microsoft.com/azure-sql-edge:latest
echo "using image $SQL_SERVER_IMAGE"

docker pull $SQL_SERVER_IMAGE

echo 'creating pgd-db'
docker run \
  --name 'pgd-db' \
  -e 'ACCEPT_EULA=Y' \
  -e 'SA_PASSWORD=<YourStrong!Passw0rd>' \
  -p 1633:1433 \
  -d "$SQL_SERVER_IMAGE"

mvn clean install \
  -Ppopulaterator \
  -P'!standard' \
  -Dexec.cleanupDaemonThreads=false
