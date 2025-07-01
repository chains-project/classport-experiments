#!/bin/bash

# This script computes the overhead of the classport embedding process for mcs.
# It assumes that the classport files are already present in the specified directory.
./pre-processing.sh
mvn clean package -Dmaven.repo.local=../../mcs-0.7.3/classport-files
java -jar target/mcs-overhead-1.0-SNAPSHOT.jar -rff results.json -rf json