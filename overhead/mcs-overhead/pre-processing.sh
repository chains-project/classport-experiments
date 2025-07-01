#!/bin/bash
set -e  # Exit on error

SRC="../../mcs-0.7.3/target/mcs-0.7.3.jar"
DEST_DIR="../../mcs-0.7.3/classport-files/it/mulders/mcs/0.7.3"
DEST_JAR="$DEST_DIR/mcs-0.7.3.jar"

mkdir -p "$DEST_DIR"
cp "$SRC" "$DEST_JAR"
echo "Copied $SRC to $DEST_JAR"