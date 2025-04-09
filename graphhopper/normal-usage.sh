#!/bin/zsh

ABSOLUTE_PATH=$(realpath $0)
DIR_PATH=$(dirname $ABSOLUTE_PATH)
PATH_TO_JAR="graphhopper-web-9.1.jar"

check_for_exit_code() {
    if [ $1 -ne 0 ]; then
        echo "$3 " $2
        exit $1
    fi
}

java -jar ${PATH_TO_JAR} server config-example.yml


