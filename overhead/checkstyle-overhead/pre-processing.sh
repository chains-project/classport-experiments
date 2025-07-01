#!/bin/bash
set -e  # Exit on error

SRC="../../checkstyle-checkstyle-10.23.0/target/checkstyle-10.23.0-all.jar"
DEST_DIR="../../checkstyle-checkstyle-10.23.0/classport-files/com/puppycrawl/tools/checkstyle/10.23.0"
DEST_JAR="$DEST_DIR/checkstyle-10.23.0.jar"

mkdir -p "$DEST_DIR"
cp "$SRC" "$DEST_JAR"
echo "Copied $SRC to $DEST_JAR"