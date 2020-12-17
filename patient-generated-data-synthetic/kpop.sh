#!/usr/bin/env bash
[ $# -ne 2 ] && echo "kpop.sh environment acces-token" && exit 1
mvn -q -Dsentinel="$1" -Daccess-token="$2" test -Pkpop -P'!standard'
