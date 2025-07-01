#!/bin/bash

# This script computes the overhead of the classport embedding process for checkstyle.
# It assumes that the classport files are already present in the specified directory.
mvn clean 
./pre-processing.sh
mvn -P assembly package -Dmaven.repo.local=../../checkstyle-checkstyle-10.23.0/classport-files 
java -jar target/checkstyle-overhead-1.0-SNAPSHOT.jar -rff results.json -rf json