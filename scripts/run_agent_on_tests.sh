#!/bin/bash

# Ensure required argument is provided
if [ $# -lt 1 ]; then
  echo "Usage: $0 <program_name>"
  echo "Supported programs: pdfbox, ripper, checkstyle, jacop, mcs, ttorrent, graph"
  exit 1
fi

# Program name passed as an argument
PROGRAM=$1

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLASS_PORT_AGENT="$SCRIPT_DIR/../../classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar"
PROJECT_NAME=$PROGRAM
OUTPUT_DIR="$SCRIPT_DIR/../output"

case $PROGRAM in
  pdfbox)
    cd ../pdfbox-3.0.4
    mvn test -DargLine="-javaagent:"$CLASS_PORT_AGENT"="$PROJECT_NAME","$OUTPUT_DIR" ${argLine}"
    ;;
  ripper)
    cd ../certificate-ripper-2.4.1
    mvn test -DargLine="-javaagent:"$CLASS_PORT_AGENT"="$PROJECT_NAME","$OUTPUT_DIR" ${argLine}"
    ;;
  checkstyle)
    cd ../checkstyle-checkstyle-10.23.0
    mvn test -DargLine="-javaagent:"$CLASS_PORT_AGENT"="$PROJECT_NAME","$OUTPUT_DIR" ${argLine}"
    ;;
  jacop)
    cd ../jacop-4.10.0
    mvn test -DargLine="-javaagent:"$CLASS_PORT_AGENT"="$PROJECT_NAME","$OUTPUT_DIR" ${argLine}"
    ;;
  mcs)
    cd ../mcs-0.7.3
    mvn test -DargLine="-javaagent:"$CLASS_PORT_AGENT"="$PROJECT_NAME","$OUTPUT_DIR" ${argLine}"
    ;;
  ttorrent)
    cd ../ttorrent-ttorrent-1.5
    mvn test -DargLine="-javaagent:"$CLASS_PORT_AGENT"="$PROJECT_NAME","$OUTPUT_DIR" ${argLine}"
    ;;
  graph)
    cd ../graphhopper/graphhopper/web
    mvn test -DargLine="-javaagent:"$CLASS_PORT_AGENT"="$PROJECT_NAME","$OUTPUT_DIR" ${argLine}"
    ;;
  *)
    echo "Error: Unsupported program '$PROGRAM'"
    echo "Supported programs: pdfbox, certificate-ripper, checkstyle, jacop, mcs, ttorrent, graph"
    exit 1
    ;;
esac