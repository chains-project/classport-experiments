#!/bin/bash

# clean classport-files
./clean.sh --classport-files
cd ../pdfbox-3.0.4
mvn clean

mvn io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed

cd ../scripts
./post_process_local_repo.sh pdfbox
cd ../pdfbox-3.0.4

cd parent
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../io
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../fontbox
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../xmpbox
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../pdfbox
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../preflight
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../preflight-app
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../debugger
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../tools
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../app
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../debugger-app
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ../examples
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests

cd ..
mvn package -Dmaven.repo.local=all-classport-files -DskipTests