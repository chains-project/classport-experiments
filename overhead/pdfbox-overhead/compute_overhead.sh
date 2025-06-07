#!/bin/bash

# This script computes the overhead of the classport embedding process for PDFBox.
# It assumes that the classport files are already present in the specified directory.
mvn clean package -Dmaven.repo.local=../../pdfbox-3.0.4/all-classport-files
java -jar target/pdfbox-overhead-1.0-SNAPSHOT.jar -rff results.json -rf json -prof stack