#!/bin/bash

# Usage: ./compute_build_overhead.sh <program_name>
# Ensure required argument is provided
if [ $# -lt 1 ]; then
  echo "Usage: $0 <program_name> "
  echo "Supported programs: pdfbox, mcs, ripper, batik, checkstyle, graphhopper, biojava"
  exit 1
fi

# Program name passed as an argument
PROGRAM=$1

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

case $PROGRAM in
  pdfbox)
    PROJECT_DIR="$SCRIPT_DIR/../pdfbox-3.0.4"
    ;;
  ripper)
    PROJECT_DIR="$SCRIPT_DIR/../certificate-ripper-2.4.1"
    ;;
  mcs)
    PROJECT_DIR="$SCRIPT_DIR/../mcs-0.7.3"
    ;;
  batik)
    PROJECT_DIR="$SCRIPT_DIR/../batik-1.19"
    ;;
  checkstyle)
    PROJECT_DIR="$SCRIPT_DIR/../checkstyle-checkstyle-10.23.0"
    ;;
  biojava)
    PROJECT_DIR="$SCRIPT_DIR/../biojava-7.2.4"
    ;;
#   jacop)
#     PROJECT_DIR="$SCRIPT_DIR/../jacop-4.10.0/target/jacop-4.10.0.jar"
#     ;;

#   ttorrent)
#     PROJECT_DIR="$SCRIPT_DIR/../ttorrent-ttorrent-1.5/cli/target/ttorrent-cli-1.5-shaded.jar"
#     ;;
  graphhopper)
    PROJECT_DIR="$SCRIPT_DIR/../graphhopper-11.0"
    ;;
#   commons)
#     PROJECT_DIR="$SCRIPT_DIR/../commons-validator-1.9.0-src/target/commons-validator-1.9.0.jar"
#     ;;
  *)
    echo "Error: Unsupported program '$PROGRAM'"
    echo "Supported programs: pdfbox, checkstyle, graphhopper, mcs, ripper, batik, biojava"
    exit 1
    ;;
esac

EXTRA_MAVEN_ARGS=()
case $PROGRAM in
    jacop)
        APP_JAR="target/jacop-4.10.0.jar"
        ;;
    mcs)
        APP_JAR="target/mcs-0.7.3.jar"
        ;;
    ttorrent)
        APP_JAR="cli/target/ttorrent-cli-1.5-shaded.jar"
        ;;
    ripper)
        APP_JAR="target/crip.jar"
        ;;
    batik)
        APP_JAR="batik-all/target/batik-all-1.19.jar"
        ;;
    h2)
        APP_JAR="target/h2wrapper-1.0-SNAPSHOT.jar"
        ;;
    checkstyle)
        APP_JAR="target/checkstyle-10.23.0-all.jar"
        EXTRA_MAVEN_ARGS+=("-Passembly")    
        ;;
    pdfbox)
        APP_JAR="app/target/pdfbox-app-3.0.4.jar"
        ;;
    graphhopper)
        APP_JAR="web/target/graphhopper-web-11.0-SNAPSHOT.jar"
        ;;
    biojava)
        APP_JAR="biojava-aa-prop/target/AAProperties-jar-with-dependencies.jar"
        ;;
    *)
        echo "Error: Unsupported project '$PROJECT_NAME'"
        echo "Supported projects: jacop, mcs, ttorrent, h2, checkstyle, batik, ripper, graphhopper, biojava"
        exit 1
        ;;  
esac

cd "$PROJECT_DIR" || exit 1

# Measure size only once (before embedding)
if [[ "$OSTYPE" == "darwin"* ]]; then
    STAT_CMD=(stat -f%z)
else
    STAT_CMD=(stat -c%s)
fi

# First, build once to ensure JAR exists for size measurement
echo "Building baseline to measure initial JAR size..."
mvn clean package -DskipTests "${EXTRA_MAVEN_ARGS[@]}" > /dev/null 2>&1

BEFORE_SIZE=$("${STAT_CMD[@]}" "$APP_JAR")
echo "Size of the JAR $(realpath $APP_JAR) before embedding: $BEFORE_SIZE bytes"

# Arrays to store times from 10 runs
declare -a BASELINE_TIMES
declare -a PLUGIN_TIMES
declare -a BASELINE_SECONDS_ARRAY
declare -a PLUGIN_SECONDS_ARRAY
declare -a PERCENTAGE_OVERHEADS
declare -a BASELINE_EXIT_CODES
declare -a PLUGIN_EXIT_CODES

# Run time measurements 10 times
NUM_RUNS=10
echo "Running $NUM_RUNS iterations to measure build times..."

for i in $(seq 1 $NUM_RUNS); do
    echo "Iteration $i/$NUM_RUNS..."
    
    # Measure baseline build time and capture exit code
    BASELINE_OUTPUT=$(mvn clean package -DskipTests "${EXTRA_MAVEN_ARGS[@]}" 2>&1)
    BASELINE_EXIT_CODE=$?
    BASELINE_TIME=$(echo "$BASELINE_OUTPUT" | grep "\[INFO\] Total time:" | sed 's/.*\[INFO\] Total time: *//' | sed 's/ *$//')
    BASELINE_TIMES+=("$BASELINE_TIME")
    BASELINE_EXIT_CODES+=("$BASELINE_EXIT_CODE")
    
    # Convert Maven time format to seconds (handles "MM:SS min" or "XX.XXX s" formats)
    BASELINE_SECONDS=$(echo "$BASELINE_TIME" | awk '{
        if ($0 ~ /min$/) {
            # Format: "MM:SS min"
            split($1, parts, ":");
            minutes = parts[1];
            seconds = parts[2];
            printf "%.3f", minutes * 60 + seconds;
        } else if ($0 ~ /s$/) {
            # Format: "XX.XXX s"
            gsub(/ s$/, "", $0);
            printf "%.3f", $0;
        }
    }')
    BASELINE_SECONDS_ARRAY+=("$BASELINE_SECONDS")
    
    # Measure plugin execution time and capture exit code
    PLUGIN_OUTPUT=$(mvn clean package -DskipTests -Pembed "${EXTRA_MAVEN_ARGS[@]}" 2>&1)
    PLUGIN_EXIT_CODE=$?
    PLUGIN_TIME=$(echo "$PLUGIN_OUTPUT" | grep "\[INFO\] Total time:" | sed 's/.*\[INFO\] Total time: *//' | sed 's/ *$//')
    PLUGIN_TIMES+=("$PLUGIN_TIME")
    PLUGIN_EXIT_CODES+=("$PLUGIN_EXIT_CODE")
    
    # Convert Maven time format to seconds (handles "MM:SS min" or "XX.XXX s" formats)
    PLUGIN_SECONDS=$(echo "$PLUGIN_TIME" | awk '{
        if ($0 ~ /min$/) {
            # Format: "MM:SS min"
            split($1, parts, ":");
            minutes = parts[1];
            seconds = parts[2];
            printf "%.3f", minutes * 60 + seconds;
        } else if ($0 ~ /s$/) {
            # Format: "XX.XXX s"
            gsub(/ s$/, "", $0);
            printf "%.3f", $0;
        }
    }')
    PLUGIN_SECONDS_ARRAY+=("$PLUGIN_SECONDS")
    
    # Compute percentage overhead for this run
    TIME_OVERHEAD=$(echo "$PLUGIN_SECONDS - $BASELINE_SECONDS" | bc)
    PERCENTAGE_OVERHEAD=$(echo "scale=8; ($TIME_OVERHEAD / $BASELINE_SECONDS) * 100" | bc)
    PERCENTAGE_OVERHEADS+=("$PERCENTAGE_OVERHEAD")
    
    echo "  Run $i: Baseline=${BASELINE_TIME} (exit=$BASELINE_EXIT_CODE), Plugin=${PLUGIN_TIME} (exit=$PLUGIN_EXIT_CODE), Overhead=${PERCENTAGE_OVERHEAD}%"
done

# Measure size after embedding (only once, after last run)
echo "Measuring size of the JAR after embedding..."
if [[ ! -f "$APP_JAR" ]]; then
    echo "Error: JAR file not found at $APP_JAR"
    exit 1
fi

AFTER_SIZE=$("${STAT_CMD[@]}" "$APP_JAR")

# Calculate average times and overheads
TOTAL_BASELINE_SECONDS=0
TOTAL_PLUGIN_SECONDS=0
TOTAL_PERCENTAGE_OVERHEAD=0

for i in $(seq 0 $((NUM_RUNS - 1))); do
    TOTAL_BASELINE_SECONDS=$(echo "$TOTAL_BASELINE_SECONDS + ${BASELINE_SECONDS_ARRAY[$i]}" | bc)
    TOTAL_PLUGIN_SECONDS=$(echo "$TOTAL_PLUGIN_SECONDS + ${PLUGIN_SECONDS_ARRAY[$i]}" | bc)
    TOTAL_PERCENTAGE_OVERHEAD=$(echo "$TOTAL_PERCENTAGE_OVERHEAD + ${PERCENTAGE_OVERHEADS[$i]}" | bc)
done

AVG_BASELINE_SECONDS=$(echo "scale=8; $TOTAL_BASELINE_SECONDS / $NUM_RUNS" | bc)
AVG_PLUGIN_SECONDS=$(echo "scale=8; $TOTAL_PLUGIN_SECONDS / $NUM_RUNS" | bc)
AVG_PERCENTAGE_OVERHEAD=$(echo "scale=8; $TOTAL_PERCENTAGE_OVERHEAD / $NUM_RUNS" | bc)

AVG_TIME_OVERHEAD=$(echo "$AVG_PLUGIN_SECONDS - $AVG_BASELINE_SECONDS" | bc)

# Calculate median percentage overhead
# Sort the array and find the middle value(s)
IFS=$'\n' SORTED_OVERHEADS=($(sort -n <<<"${PERCENTAGE_OVERHEADS[*]}"))
unset IFS

if [ $(($NUM_RUNS % 2)) -eq 0 ]; then
    # Even number of runs: average of two middle values
    MID1=$((NUM_RUNS / 2 - 1))
    MID2=$((NUM_RUNS / 2))
    MEDIAN_PERCENTAGE_OVERHEAD=$(echo "scale=8; (${SORTED_OVERHEADS[$MID1]} + ${SORTED_OVERHEADS[$MID2]}) / 2" | bc)
else
    # Odd number of runs: middle value
    MID=$((NUM_RUNS / 2))
    MEDIAN_PERCENTAGE_OVERHEAD=${SORTED_OVERHEADS[$MID]}
fi

SIZE_OVERHEAD=$((AFTER_SIZE - BEFORE_SIZE))
PERCENTAGE_SIZE_OVERHEAD=$(echo "scale=8; ($SIZE_OVERHEAD / $BEFORE_SIZE) * 100" | bc)

echo "-------------------------------"
echo "Results across $NUM_RUNS runs:"
echo "Average baseline build time: ${AVG_BASELINE_SECONDS}s"
echo "Average plugin execution time: ${AVG_PLUGIN_SECONDS}s"
echo "Average time overhead: ${AVG_TIME_OVERHEAD}s"
echo "Average percentage time overhead: ${AVG_PERCENTAGE_OVERHEAD}%"
echo "Median percentage time overhead: ${MEDIAN_PERCENTAGE_OVERHEAD}%"
echo "-------------------------------"
echo "Size of JAR before embedding: ${BEFORE_SIZE} bytes"
echo "Size of JAR after embedding: ${AFTER_SIZE} bytes"
echo "Size overhead: ${SIZE_OVERHEAD} bytes"
echo "Percentage size overhead: ${PERCENTAGE_SIZE_OVERHEAD}%"
echo "-------------------------------"
