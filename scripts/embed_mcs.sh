#!/bin/bash

cd ../mcs-0.7.3

mvn io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed

mvn package -Dmaven.repo.local=classport-files