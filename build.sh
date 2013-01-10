#!/bin/sh

mvn -Dmaven.test.skip=true clean source:jar package assembly:single site
