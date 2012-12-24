#!/bin/bash

BASEPATH=$(dirname "$0")
CLASSPATH=$(echo dep/*.jar | tr ' ' ':')

find "$BASEPATH" -name "*.java" | xargs javac -cp "$CLASSPATH"
