#!/bin/bash

mvn clean package -Dmaven.repo.local=../../zxing-wrapper/classport-files

java -jar target/zxing-overhead-1.0-SNAPSHOT.jar -rff results.json -rf json -prof stack