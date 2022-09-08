#!/usr/bin/env bash

set -euo pipefail

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR


PGD_URL="${DEPLOYMENT_TEST_PROTOCOL}://${DEPLOYMENT_TEST_HOST}"


java-tests \
  --module-name "patient-generated-data-tests" \
  --regression-test-pattern ".*IT\$" \
  --smoke-test-pattern ".*OpenApiIT\$" \
  -Daccess-token="${MAGIC_ACCESS_TOKEN}" \
  -Dclient-key="${CLIENT_KEY}" \
  -Dsentinel="${DEPLOYMENT_ENVIRONMENT}" \
  -Dsentinel.management.url="${PGD_URL}" \
  -Dsentinel.management.port="${DEPLOYMENT_TEST_PORT}" \
  -Dsentinel.r4.url="${PGD_URL}" \
  -Dsentinel.r4.port="${DEPLOYMENT_TEST_PORT}" \
  -Dsentinel.sandbox-data-r4.url="${PGD_URL}" \
  -Dsentinel.sandbox-data-r4.port="${DEPLOYMENT_TEST_PORT}" \
  $@

exit $?
