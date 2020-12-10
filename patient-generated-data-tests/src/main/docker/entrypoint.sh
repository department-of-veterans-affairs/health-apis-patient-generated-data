#!/usr/bin/env bash

set -euo pipefail

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

test -n "${K8S_ENVIRONMENT}"
if [ -z "${SENTINEL_ENV:-}" ]; then SENTINEL_ENV=$K8S_ENVIRONMENT; fi
if [ -z "${MAGIC_ACCESS_TOKEN:-}" ]; then MAGIC_ACCESS_TOKEN=$MAGIC_ACCESS_TOKEN; fi

java-tests \
  --module-name "patient-generated-data-tests" \
  --regression-test-pattern ".*IT\$" \
  --smoke-test-pattern ".*OpenApiIT\$" \
  -Daccess-token="$MAGIC_ACCESS_TOKEN" \
  -Dsentinel="$SENTINEL_ENV" \
  $@

exit $?
