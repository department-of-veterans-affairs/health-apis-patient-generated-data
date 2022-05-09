#! /usr/bin/env bash

REPO=$(cd $(dirname $0) && pwd)
PROFILE=dev
MARKER=$(date +%s)

makeConfig() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  [ -f "$target" ] && mv -v $target $target.$MARKER
  grep -E '(.*= *unset)' "$REPO/$project/src/main/resources/application.properties" \
    > "$target"
}

configValue() {
  local project="$1"
  local profile="$2"
  local key="$3"
  local value="$4"
  local target="$REPO/$project/config/application-${profile}.properties"
  local escapedValue=$(echo $value | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')
  sed -i "s/^$key=.*/$key=$escapedValue/" $target
}

checkForUnsetValues() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  echo "checking $target"
  grep -E '(.*= *unset)' "$target"
  [ $? == 0 ] && echo "Failed to populate all unset values" && exit 1
  diff -q $target $target.$MARKER
  [ $? == 0 ] && rm -v $target.$MARKER
}

makeConfig patient-generated-data $PROFILE
configValue patient-generated-data $PROFILE authorization.client-ids '{"P73R4CUD4":"pteracuda"}'
configValue patient-generated-data $PROFILE authorization.static-access-token 'pterastatic'
configValue patient-generated-data $PROFILE internal.client-key 'pteracuda'
configValue patient-generated-data $PROFILE metadata.endpoint-authorize 'http://fake.com/authorize'
configValue patient-generated-data $PROFILE metadata.endpoint-management 'http://fake.com/manage'
configValue patient-generated-data $PROFILE metadata.endpoint-revocation 'http://fake.com/revoke'
configValue patient-generated-data $PROFILE metadata.endpoint-token 'http://fake.com/token'
configValue patient-generated-data $PROFILE public-r4-base-path 'r4'
configValue patient-generated-data $PROFILE public-url 'http://localhost:8096'
configValue patient-generated-data $PROFILE sandbox-data-management.enabled 'true'
configValue patient-generated-data $PROFILE spring.datasource.password '<YourStrong!Passw0rd>'
configValue patient-generated-data $PROFILE spring.datasource.url 'jdbc:sqlserver://localhost:1633;database=pgd;sendStringParametersAsUnicode=false'
configValue patient-generated-data $PROFILE spring.datasource.username 'SA'
configValue patient-generated-data $PROFILE web-exception-key '-sharktopus-v-pteracuda-'
checkForUnsetValues patient-generated-data $PROFILE
