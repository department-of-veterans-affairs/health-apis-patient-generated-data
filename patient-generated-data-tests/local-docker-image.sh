#!/usr/bin/env bash

usage() {
cat<<EOF
$0 <build|run> [run-args]

Build or run the integration test docker image.

EOF
}

build() {
  mvn clean deploy \
    -DskipTests \
    -Dexec.skip=true \
    -Dsentinel.skipLaunch=true \
    -P'!standard' \
    -Prelease \
    -Ddocker.skip.push=true \
    -Dmaven.deploy.skip=true \
    -Ddocker.username=$DOCKER_USERNAME \
    -Ddocker.password="$DOCKER_PASSWORD"
}

run() {
  THIS_MACHINE="localhost"
  docker run \
    --rm \
    --network="host" \
    -e CLIENT_KEY=pteracuda \
    -e K8S_ENVIRONMENT=${ENV:-local} \
    -e K8S_LOAD_BALANCER=${LB:-local} \
    -e MAGIC_ACCESS_TOKEN=pterastatic \
    -e SENTINEL_URL="http://host.docker.internal" \
     vasdvp/health-apis-patient-generated-data-tests:latest $@
}

main() {
  local cmd=$1
  shift
  case "$cmd" in
    r|run) run $@;;
    b|build) build;;
    br) build && run $@;;
    *) usage "Unknown command $cmd"
  esac
}

main $@
