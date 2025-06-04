#!/bin/bash

# This script computes the overhead of the classport embedding process for PDFBox.
# It assumes that the classport files are already present in the specified directory.
mvn clean package -Dmaven.repo.local=../../batikwrapper/classport-files
java -jar target/batik-overhead-1.0-SNAPSHOT.jar -rff results.json -rf json