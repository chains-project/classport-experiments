#!/bin/zsh

ABSOLUTE_PATH=$(realpath $0)
DIR_PATH=$(dirname $ABSOLUTE_PATH)
PATH_TO_JAR="graphhopper/web/target/graphhopper-web-9.1.jar"

check_for_exit_code() {
    if [ $1 -ne 0 ]; then
        echo "$3 " $2
        exit $1
    fi
}

java -javaagent:/Users/serena/Dottorato/KTH/classport-dev/classport/classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar -jar ${PATH_TO_JAR} server config-example.yml

#java -javaagent:/Users/serena/Dottorato/KTH/classport-dev/classport/classport-agent/target/classport-agent-0.1.0-SNAPSHOT.jar -jar ${PATH_TO_JAR} server config-example.yml
