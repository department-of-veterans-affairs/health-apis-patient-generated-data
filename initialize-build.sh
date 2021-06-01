#!/usr/bin/env bash

# Resolve:
#  `error reading liquibase-core.jar; zip END header not found`,

# For some reason, liquibase fails to download with Maven.
# When Maven checks the available remote repositories, it will check Health
# API Nexus repository. The VA network will reject the HTTP request with
# a webpage that indicates you should submit a Service Now ticket. Since,
# the response is still a 200, Maven thinks it succeeded downloading
# the jar or pom. The build will fail later when it tries to load the jar.

#
# We can explicitly get dependency if we disable the Health API Nexus server.
# To do that, we'll create a fake project that no ties to Health API.
#

FAKE_PROJECT=$(dirname $(readlink -f $0))/target/initialize-build
if [ ! -d $FAKE_PROJECT ]; then mkdir -p $FAKE_PROJECT; fi
cd $FAKE_PROJECT
cat >pom.xml<<EOF
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>gov.va.workaround</groupId>
  <artifactId>intercepted-artifacts</artifactId>
  <version>1.0</version>
</project>
EOF

mvn dependency:purge-local-repository -DmanualInclude="org.liquibase:liquibase-core:4.3.5"

mvn dependency:get -B \
  -P'!gov.va.api.health' \
  -Dartifact=org.liquibase:liquibase-core:4.3.5:jar
