#!/bin/bash

cd ../commons-validator-1.9.0-src

mvn io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed

mvn package -Dmaven.repo.local=classport-files -Drat.skip=true 