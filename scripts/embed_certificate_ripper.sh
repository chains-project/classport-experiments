#!/bin/bash

cd ../certificate-ripper-2.4.1

mvn io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed

mvn package -Dmaven.repo.local=classport-files