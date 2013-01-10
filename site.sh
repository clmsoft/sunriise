#!/bin/sh

mvn -DgenerateReports=false -Dmaven.test.skip=true site $*
