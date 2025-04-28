#!/bin/bash

cd ../guice-7.0.0

mvn clean

mvn io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed

../scripts/post_process_local_repo.sh guice

mvn package -Dmaven.repo.local=all-classport-files -Dguice.skipTests=true
