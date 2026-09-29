#!/bin/sh
# Minimal wrapper: prefer committed wrapper jar if present, else system gradle.
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
if [ -f "$DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
  exec java -jar "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
fi
exec gradle "$@"
