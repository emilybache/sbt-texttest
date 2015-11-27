#!/bin/bash

cd myapp
echo sbt "$@"

sbt -Dsbt.log.noformat=true "$@" exit
exit $?