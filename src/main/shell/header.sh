#!/usr/bin/env bash

set -e

if test -f "$KALA_TEMPLATE_JAVA_HOME/bin/java"; then
  _JAVA="$KALA_TEMPLATE_JAVA_HOME/bin/java"
elif test -f "$JAVA_HOME/bin/java"; then
  _JAVA="$JAVA_HOME/bin/java"
else
  _JAVA="java"
fi

if test -n "$KALA_TEMPLATE_JAVA_OPTS"; then
  _JAVA_OPTS="$KALA_TEMPLATE_JAVA_OPTS"
else
  _JAVA_OPTS="-Xmx1g"
fi

exec "$_JAVA" $_JAVA_OPTS -cp "$0" 'kala.template.TemplateEngine' "$@"
