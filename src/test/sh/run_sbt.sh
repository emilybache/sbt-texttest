#!/bin/bash

if [ -d "dependency" ]; then
  cd dependency
  sbt publish-local
  cd ..
fi

cd myapp
echo sbt "$@"

sbt -Dsbt.log.noformat=true "$@" exit
exit $?