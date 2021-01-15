#!/usr/bin/env bash
[ $# -ne 3 ] && echo "synthetic-refresh.sh sentinel access-token client-key" && exit 1
mvn -q -Dsentinel="$1" -Daccess-token="$2" -Dclient-key="$3" test -Psynthetic-refresh -P'!standard'
