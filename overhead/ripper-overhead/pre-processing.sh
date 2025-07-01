#!/bin/bash
set -e  # Exit on error

SRC="../../certificate-ripper-2.4.1/target/crip.jar"
DEST_DIR="../../certificate-ripper-2.4.1/classport-files/io/github/hakky54/crip/1.0.0"
DEST_JAR="$DEST_DIR/crip-1.0.0.jar"

mkdir -p "$DEST_DIR"
cp "$SRC" "$DEST_JAR"
echo "Copied $SRC to $DEST_JAR"