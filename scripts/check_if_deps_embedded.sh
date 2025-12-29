#!/bin/bash

# Usage: ./check_if_deps_embedded.sh <program_name> [--deps]
# Ensure required argument is provided
if [ $# -lt 1 ]; then
  echo "Usage: $0 <program_name> [--deps] "
  echo "Supported programs: pdfbox, ripper, checkstyle, jacop, mcs, ttorrent, graph, batik, h2, zxing"
  exit 1
fi

# Program name passed as an argument
PROGRAM=$1

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANALYSER_JAR_COMPLETENESS="$SCRIPT_DIR/../../classport-analyser/target/completeness-analyser.jar"
ANALYSER_JAR_CORRECTNESS="$SCRIPT_DIR/../../classport-analyser/target/correctness-analyzer.jar"

# Check if the classport-analyser jar file exists
if [ ! -f "$ANALYSER_JAR_COMPLETENESS" ]; then
  echo "Error: classport-analyser jar file not found at $ANALYSER_JAR_COMPLETENESS"
  exit 1
fi

# Check if the classport-analyser jar file exists for correctness
if [ ! -f "$ANALYSER_JAR_CORRECTNESS" ]; then
  echo "Error: classport-analyser jar file not found at $ANALYSER_JAR_CORRECTNESS"
  exit 1
fi

# Define mappings for programs
case $PROGRAM in
  pdfbox)
    APP_JAR="$SCRIPT_DIR/../pdfbox-3.0.4/app/target/pdfbox-app-3.0.4.jar"
    ;;
  ripper)
    APP_JAR="$SCRIPT_DIR/../certificate-ripper-2.4.1/target/crip.jar"
    ;;
  checkstyle)
    APP_JAR="$SCRIPT_DIR/../checkstyle-checkstyle-10.23.0/target/checkstyle-10.23.0-all.jar"
    ;;
  jacop)
    APP_JAR="$SCRIPT_DIR/../jacop-4.10.0/target/jacop-4.10.0.jar"
    ;;
  mcs)
    APP_JAR="$SCRIPT_DIR/../mcs-0.7.3/target/mcs-0.7.3.jar"
    ;;
  ttorrent)
    APP_JAR="$SCRIPT_DIR/../ttorrent-ttorrent-1.5/cli/target/ttorrent-cli-1.5-shaded.jar"
    ;;
  graph)
    APP_JAR="$SCRIPT_DIR/../graphhopper/graphhopper/web/target/graphhopper-web-9.1.jar"
    ;;
  commons)
    APP_JAR="$SCRIPT_DIR/../commons-validator-1.9.0-src/target/commons-validator-1.9.0.jar"
    ;;
  batik)
    APP_JAR="$SCRIPT_DIR/../batikwrapper/target/batikwrapper-1.0-SNAPSHOT.jar"
    ;;
  h2)
    APP_JAR="$SCRIPT_DIR/../h2wrapper/target/h2wrapper-1.0-SNAPSHOT.jar"
    ;;
  zxing)
    APP_JAR="$SCRIPT_DIR/../zxing-wrapper/target/zxing-workload-1.0-jar-with-dependencies.jar"
    ;;
  *)
    echo "Error: Unsupported program '$PROGRAM'"
    echo "Supported programs: pdfbox, certificate-ripper, checkstyle, jacop, mcs, ttorrent, graph, batik, h2, zxing"
    exit 1
    ;;
esac

if [[ "$2" == "--deps" ]]; then
    
    # Run mvn dependency:list and process the output
    MODULE_DIR=$(dirname "$APP_JAR")
    java -jar ${ANALYSER_JAR_CORRECTNESS} -printList ${APP_JAR} > $MODULE_DIR/../embedded.txt
    cd "$MODULE_DIR/../" || exit 1
    
    echo "Running correctness check..."
    # include scope gives compile, provided, and system dependencies
    # We exclude runtime and test dependencies
    mvn dependency:list -DincludeScope=compile > mvn.txt

    # Extract dependencies, format them, and sort
    awk '/^\[INFO\]/ && $2 ~ /.*:.*:jar:.*/ { split($2, a, ":"); print a[1] ":" a[2] ":jar:" a[4] }' mvn.txt | sort > mvn_sorted.txt
    mv mvn_sorted.txt mvn.txt

    # Sort the output of the analyzer
    sort embedded.txt > embedded_sorted.txt
    mv embedded_sorted.txt embedded.txt

    echo "-------------------"
    echo "List of dependencies in the embedded jar:"
    cat embedded.txt
    echo "-------------------"
    echo "List of dependencies in the mvn dependency:list:"
    cat mvn.txt
    echo "-------------------"
    echo "List of wrong dependencies:"
    comm -23 embedded.txt mvn.txt 
    echo "-------------------"

    # Delete the temporary files
    rm mvn.txt embedded.txt

    exit 0
else
    # Run the classport-analyser
    java -jar ${ANALYSER_JAR_COMPLETENESS} ${APP_JAR} 
fi

