#!/bin/bash
./pre-processing.sh
mvn clean package -U -Dmaven.repo.local=../../certificate-ripper-2.4.1/classport-files

java -jar target/ripper-overhead-1.0-SNAPSHOT.jar -rff results.json -rf json