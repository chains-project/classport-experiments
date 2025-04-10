#!/bin/bash

cd ../checkstyle-checkstyle-10.23.0

mvn io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed

mvn package -Dmaven.repo.local=classport-files -Passembly,no-validations