#!/usr/bin/env bash
[ $# -ne 2 ] && echo "synthetic-refresh.sh sentinel access-token" && exit 1
mvn -q -Dsentinel="$1" -Daccess-token="$2" test -Psynthetic-refresh -P'!standard'
