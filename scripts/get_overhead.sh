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

EXAMPLE="cpviz.CPvizSudoku"
PROJECT_NAME=$PROGRAM
OUTPUT_DIR="$SCRIPT_DIR/../output"
RUNS=10
JAVA="java"
SAMPLE_TORRENT="$SCRIPT_DIR/../resources/sample.torrent"
AGENT_JAR="$SCRIPT_DIR/../../classport-instr-agent/target/classport-instr-agent-0.1.0-SNAPSHOT.jar"

# Define mappings for programs
case $PROGRAM in
  pdfbox)
    APP_JAR="$SCRIPT_DIR/../pdfbox-3.0.4/app/target/pdfbox-app-3.0.4.jar"
    JAVA_COMMAND="export:text -i=$SCRIPT_DIR/../resources/test.pdf"
    ;;
  ripper)
    APP_JAR="$SCRIPT_DIR/../certificate-ripper-2.4.1/target/crip.jar"
    JAVA_COMMAND="print --url=https://chains.proj.kth.se/"
    ;;
  checkstyle)
    APP_JAR="$SCRIPT_DIR/../checkstyle-checkstyle-10.23.0/target/checkstyle-10.23.0-all.jar"
    JAVA_COMMAND="-c /sun_checks.xml ../../classport-instr-agent"
    ;;
  jacop)
    APP_JAR="$SCRIPT_DIR/../jacop-4.10.0/target/jacop-4.10.0.jar"
    JAVA_COMMAND="RunExample ${EXAMPLE}"
    ;;
  mcs)
    APP_JAR="$SCRIPT_DIR/../mcs-0.7.3/target/mcs-0.7.3.jar"
    JAVA_COMMAND="search search"
    ;;
  ttorrent)
    APP_JAR="$SCRIPT_DIR/../ttorrent-ttorrent-1.5/cli/target/ttorrent-cli-1.5-shaded.jar"
    JAVA_COMMAND="-o "$OUTPUT_DIR" --seed 0 "$SAMPLE_TORRENT""
    ;;
  graph)
    APP_JAR="$SCRIPT_DIR/../graphhopper/graphhopper/web/target/graphhopper-web-9.1.jar"
    JAVA_COMMAND="server $SCRIPT_DIR/../graphhopper/config-example.yml"
    ;;
  *)
    echo "Error: Unsupported program '$PROGRAM'"
    echo "Supported programs: pdfbox, certificate-ripper, checkstyle, jacop, mcs, ttorrent, graph"
    exit 1
    ;;
esac


# Ensure locale uses dot as decimal separator
export LC_NUMERIC=C

measure_time() {
  MODE=$1
  TOTAL=0

  echo "Running $MODE..." >&2

  for i in $(seq 1 $RUNS); do
    if [ "$MODE" = "baseline" ]; then
      START=$(date +%s%N)
      $JAVA -jar $APP_JAR $JAVA_COMMAND >/dev/null 2>&1
      if [ "$PROGRAM" = "ttorrent" ]; then
        # Remove the sample.txt file if it exists
        rm -f "$OUTPUT_DIR/sample.txt"
      fi
      END=$(date +%s%N)
    else
      START=$(date +%s%N)
      $JAVA -javaagent:$AGENT_JAR=$PROJECT_NAME,$OUTPUT_DIR -jar $APP_JAR $JAVA_COMMAND >/dev/null 2>&1
      if [ "$PROGRAM" = "ttorrent" ]; then
        # Remove the sample.txt file if it exists
        rm -f "$OUTPUT_DIR/sample.txt"
      fi
      END=$(date +%s%N)
    fi
    TIME=$(echo "scale=3; ($END - $START) / 1000000000" | bc)
    echo "  Run $i: $TIME s" >&2
    TOTAL=$(echo "$TOTAL + $TIME" | bc)
  done

  AVG=$(echo "scale=3; $TOTAL / $RUNS" | bc | awk '{printf "%.3f\n", $0}')
  echo "Average for $MODE: $AVG s" >&2  # Send this output to stderr for debugging
  echo $AVG  # Return only the numeric average
}

# Run and store averages
AVG_BASELINE=$(measure_time baseline)
AVG_AGENT=$(measure_time agent)

# Debug: Print the averages
echo "DEBUG: AVG_BASELINE=$AVG_BASELINE" >&2
echo "DEBUG: AVG_AGENT=$AVG_AGENT" >&2

# Validate numeric values
if ! [[ $AVG_BASELINE =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
  echo "Error: AVG_BASELINE is not a valid number: $AVG_BASELINE"
  exit 1
fi
if ! [[ $AVG_AGENT =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
  echo "Error: AVG_AGENT is not a valid number: $AVG_AGENT"
  exit 1
fi

# Calculate overhead
if [[ $(echo "$AVG_BASELINE > 0" | bc) -eq 1 ]]; then
  OVERHEAD=$(echo "scale=2; (($AVG_AGENT - $AVG_BASELINE) / $AVG_BASELINE) * 100" | bc)
  echo "Baseline Average: ${AVG_BASELINE}s"
  echo "Agent Average: ${AVG_AGENT}s"
  echo "Overhead: ${OVERHEAD}%"
else
  echo "Error: Baseline average is zero or invalid, cannot calculate overhead."
fi

./clean.sh --output