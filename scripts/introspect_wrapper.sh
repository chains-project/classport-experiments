#!/bin/bash

# Check if the required argument is provided
if [[ $# -ne 2 ]]; then
    echo "Usage: $0 <project_name> <res_dir>" 
    echo "Supported projects: batik, zxing"
    exit 1
fi

# Input argument
PROJECT_NAME=$1
RES_DIR=$2

# Get the directory of the script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Define paths relative to the script's directory
CLASS_PORT_AGENT="$SCRIPT_DIR/../../classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar"
OUTPUT_DIR="../output"

# Check if required files exist
if [[ ! -f "$CLASS_PORT_AGENT" ]]; then
  echo "Error: CLASS_PORT_AGENT not found at $CLASS_PORT_AGENT"
  exit 1
fi

case $PROJECT_NAME in
    batik)
        APP_JAR="$SCRIPT_DIR/../batikwrapper/target/batikwrapper-1.0-SNAPSHOT.jar"
        #check if output folder exists, if yes, delete it
        if [[ -d "$SCRIPT_DIR/../batikwrapper/src/main/resources/output" ]]; then
            rm -rf "$SCRIPT_DIR/../batikwrapper/src/main/resources/output"
        fi
        ;;
    h2)
        APP_JAR="$SCRIPT_DIR/../h2wrapper/target/h2wrapper-1.0-SNAPSHOT.jar"
        RES_DIR=""
        ;;
    zxing)
        APP_JAR="$SCRIPT_DIR/../zxing-wrapper/target/zxing-workload-1.0-jar-with-dependencies.jar"
        ;;
    *)
        echo "Error: Unsupported project '$PROJECT_NAME'"
        echo "Supported projects: jacop"
        exit 1
        ;;  
esac

if [[ ! -f "$APP_JAR" ]]; then
  echo "Error: APP_JAR not found at $APP_JAR, embed the project first."
  exit 1
fi

# Create the output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Run the command
java -javaagent:"$CLASS_PORT_AGENT"="$PROJECT_NAME","$OUTPUT_DIR",dependency -jar "$APP_JAR" "$RES_DIR"