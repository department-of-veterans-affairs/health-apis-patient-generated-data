#!/usr/bin/env bash

set -o pipefail

[ -z "$SENTINEL_BASE_DIR" ] && SENTINEL_BASE_DIR=/sentinel
cd $SENTINEL_BASE_DIR
MAIN_JAR=$(find -maxdepth 1 -name "data-query-tests-*.jar" -a -not -name "data-query-tests-*-tests.jar")
TESTS_JAR=$(find -maxdepth 1 -name "data-query-tests-*-tests.jar")
WEB_DRIVER_PROPERTIES="-Dwebdriver.chrome.driver=/usr/local/bin/chromedriver -Dwebdriver.chrome.headless=true"
SYSTEM_PROPERTIES=$WEB_DRIVER_PROPERTIES
SENTINEL_CRAWLER=.*MagicPatientCrawl\$

usage() {
cat <<EOF
Commands
  list-tests
  test [--trust <host>] [-Dkey=value] <name> [name] [...]
  smoke-test
  regression-test [--skip-crawler]
  crawler-test


Example
  test\
    --trust example.something.elb.amazonaws.com\
    -Dlab.client-id=12345\
    -Dlab.client-secret=ABCDEF\
    -Dlab.user-password=secret\
    .*MagicPatientCrawl\$

Docker Run Examples
  docker run --rm --init --network=host\
  --env-file qa.testvars --env K8S_LOAD_BALANCER=example.com --env K8S_ENVIRONMENT=qa\
  vasdvp/health-apis-data-query-tests:latest smoke-test

  docker run --rm --init --network=host\
  --env-file production.testvars --env K8S_LOAD_BALANCER=example.com --env K8S_ENVIRONMENT=production\
  vasdvp/health-apis-data-query-tests crawler-test

  docker run --rm --init --network=host\
  --env-file lab.testvars --env K8S_LOAD_BALANCER=example.com --env K8S_ENVIRONMENT=lab\
  vasdvp/health-apis-data-query-tests:1.0.210 regression-test -s
$1
EOF
exit 1
}

trustServer() {
  local host=$1
  curl -sk https://$host > /dev/null 2>&1
  [ $? == 6 ] && return
  echo "Trusting $host"
  local cacertsDir="$JAVA_HOME/jre/lib/security/cacerts"
  [ -f "$JAVA_HOME/lib/security/cacerts" ] && cacertsDir="$JAVA_HOME/lib/security/cacerts"
  keytool -printcert -rfc -sslserver $host > $host.pem
  keytool \
    -importcert \
    -file $host.pem \
    -alias $host \
    -keystore $cacertsDir \
    -storepass changeit \
    -noprompt
}



doTest() {
  local pattern="$@"
  [ -z "$pattern" ] && pattern=.*IT\$
  echo "Executing tests for pattern: $pattern"
  local noise="org.junit"
  noise+="|groovy.lang.Meta"
  noise+="|io.restassured.filter"
  noise+="|io.restassured.internal"
  noise+="|java.lang.reflect"
  noise+="|java.net"
  noise+="|org.apache.http"
  noise+="|org.codehaus.groovy"
  noise+="|sun.reflect"
  java \
    ${SYSTEM_PROPERTIES[@]} \
    -jar junit-platform-console-standalone.jar \
    --scan-classpath \
    -cp "$MAIN_JAR" -cp "$TESTS_JAR" \
    --include-classname=$pattern \
    --disable-ansi-colors \
    --disable-banner \
    --fail-if-no-tests \
    | grep -vE "^	at ($noise)"

  # Exit on failure otherwise let other actions run.
  [ $? != 0 ] && exit 1
}

doListTests() {
  jar -tf $TESTS_JAR \
    | grep -E '(IT|Test)\.class' \
    | sed 's/\.class//' \
    | tr / . \
    | sort
}

doSmokeTest() {
  setupForAutomation
  doTest ".*PatientIT$"
}

doRegressionTest() {
  setupForAutomation
  doTest
  doCrawlerTest
}

doCrawlerTest() {
  # If crawler test was specified and not explicitly told to skip then it's crawl time.
  if [ "$SKIP_CRAWLER" == "true" -o -z "$SENTINEL_CRAWLER" ]; then return; fi

  setupForAutomation

  # This way, the ITs use defaults when INTERNAL_API_PATH isnt set,
  # but the crawler will use the internal path always.
  [ -z "$INTERNAL_API_PATH" ] && INTERNAL_API_PATH="/data-query"
  [[ "$INTERNAL_API_PATH" =~ .*/$ ]] && INTERNAL_API_PATH=${INTERNAL_API_PATH:0:-1}
  [ -z "$DSTU2_CRAWL_PATH" ] && DSTU2_CRAWL_PATH=${INTERNAL_API_PATH}/dstu2
  [ -z "$R4_CRAWL_PATH" ] && R4_CRAWL_PATH=${INTERNAL_API_PATH}/r4

  # Crawl DSTU2
  addToSystemProperties "crawler.url.replace" "${DATA_QUERY_REPLACEMENT_URL_PREFIX}/dstu2"
  addToSystemProperties "crawler.base-url" "${DQ_URL}${DSTU2_CRAWL_PATH}"
  [ -n "$DSTU2_ALLOW_CRAWL_URLS" ] && addToSystemProperties "crawler.allow-query-url-pattern" "$DSTU2_ALLOW_CRAWL_URLS"
  doTest $SENTINEL_CRAWLER

  # Crawl R4
  addToSystemProperties "crawler.url.replace" "${DATA_QUERY_REPLACEMENT_URL_PREFIX}/r4"
  addToSystemProperties "crawler.base-url" "${DQ_URL}${R4_CRAWL_PATH}"
  [ -n "$R4_ALLOW_CRAWL_URLS" ] && addToSystemProperties "crawler.allow-query-url-pattern" "$R4_ALLOW_CRAWL_URLS"
  doTest $SENTINEL_CRAWLER
}

checkVariablesForAutomation() {
  # Check out required deployment variables and data query specific variables.
  for param in "K8S_LOAD_BALANCER" "K8S_ENVIRONMENT" "SENTINEL_ENV" "TOKEN" \
    "DATA_QUERY_REPLACEMENT_URL_PREFIX" "USER_PASSWORD" "CLIENT_ID" "CLIENT_SECRET" "PATIENT_ID"; do
    [ -z ${!param} ] && usage "Variable $param must be specified."
  done
}

addToSystemProperties() {
  SYSTEM_PROPERTIES+=" -D$1=$2"
}

setupForAutomation() {
  checkVariablesForAutomation

  trustServer $K8S_LOAD_BALANCER

  SYSTEM_PROPERTIES="-Dsentinel=$SENTINEL_ENV \
    -Daccess-token=$TOKEN \
    -Draw-token=$RAW_TOKEN \
    -Dbulk-token=$BULK_TOKEN \
    -D${K8S_ENVIRONMENT}.user-password=$USER_PASSWORD \
    -D${K8S_ENVIRONMENT}.client-id=$CLIENT_ID \
    -D${K8S_ENVIRONMENT}.client-secret=$CLIENT_SECRET \
    -Dpatient-id=$PATIENT_ID"

  [ -z "$DQ_URL" ] && DQ_URL=https://$K8S_LOAD_BALANCER

  for property in \
    "sentinel.data-query.public-url" "sentinel.internal.url" \
    "sentinel.dstu2.url" "sentinel.stu3.url" "sentinel.r4.url"
  do
    addToSystemProperties "$property" "$DQ_URL"
  done

  [ -n "$INTERNAL_API_PATH" ] && addToSystemProperties "sentinel.internal.api-path" "$INTERNAL_API_PATH"
  [ -n "$DSTU2_API_PATH" ] && addToSystemProperties "sentinel.dstu2.api-path" "$DSTU2_API_PATH"
  [ -n "$STU3_API_PATH" ] && addToSystemProperties "sentinel.stu3.api-path" "$STU3_API_PATH"
  [ -n "$R4_API_PATH" ] && addToSystemProperties "sentinel.r4.api-path" "$R4_API_PATH"

  # This is an optional, and discouraged flag.
  [ -n "$SENTINEL_CRAWLER_IGNORES" ] && addToSystemProperties "crawler.ignores" "$SENTINEL_CRAWLER_IGNORES"
}

ARGS=$(getopt -n $(basename ${0}) \
    -l "debug,help,trust:,skip-crawler" \
    -o "D:hs" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    -D) SYSTEM_PROPERTIES+=("-D$2");;
    --debug) set -x;;
    -h|--help) usage "halp! what this do?";;
    --trust) trustServer $2;;
    -s|--skip-crawler) SKIP_CRAWLER="true";;
    --) shift;break;;
  esac
  shift;
done

[ $# == 0 ] && usage "No command specified"
COMMAND=$1
shift

case "$COMMAND" in
  t|test) doTest $@;;
  lt|list-tests) doListTests;;
  s|smoke-test) doSmokeTest;;
  r|regression-test) doRegressionTest;;
  c|crawler-test) doCrawlerTest;;
  *) usage "Unknown command: $COMMAND";;
esac

exit 0
