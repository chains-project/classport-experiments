#!/bin/bash

# Usage: ./compute_runtime_overhead.sh <program_name>
# Ensure required argument is provided
if [ $# -lt 1 ]; then
  echo "Usage: $0 <program_name> "
  echo "Supported programs: pdfbox, mcs, ripper, batik, checkstyle, zxing"
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
    PROJECT_DIR="$SCRIPT_DIR/../batikwrapper"
    ;;
  checkstyle)
    PROJECT_DIR="$SCRIPT_DIR/../checkstyle-checkstyle-10.23.0"
    ;;
  zxing)
    PROJECT_DIR="$SCRIPT_DIR/../zxing-wrapper"
    ;;
  *)
    echo "Error: Unsupported program '$PROGRAM'"
    echo "Supported programs: pdfbox, mcs, ripper, batik, checkstyle, zxing"
    exit 1
    ;;
esac

EXTRA_MAVEN_ARGS=()
case $PROGRAM in
    checkstyle)
        EXTRA_MAVEN_ARGS+=("-Passembly")    
        ;;
esac

cd "$PROJECT_DIR" || exit 1

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
echo "Running $NUM_RUNS iterations to measure test execution times..."

for i in $(seq 1 $NUM_RUNS); do
    echo "Iteration $i/$NUM_RUNS..."
    
    # Clean before baseline test
    echo "  Cleaning project..."
    mvn clean "${EXTRA_MAVEN_ARGS[@]}" > /dev/null 2>&1
    
    # Measure baseline test execution time and capture exit code
    echo "  Running baseline tests (mvn test)..."
    BASELINE_OUTPUT=$(mvn test "${EXTRA_MAVEN_ARGS[@]}" 2>&1)
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
    
    # Prepare for introspect by running clean process-classes with embed profile
    echo "  Preparing for introspect (mvn clean process-classes -Pembed)..."
    mvn clean process-classes -Pembed "${EXTRA_MAVEN_ARGS[@]}" > /dev/null 2>&1
    
    # Measure plugin test execution time and capture exit code
    echo "  Running tests with introspect (mvn test -Pintrospect)..."
    PLUGIN_OUTPUT=$(mvn test -Pintrospect "${EXTRA_MAVEN_ARGS[@]}" 2>&1)
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

echo "-------------------------------"
echo "Results across $NUM_RUNS runs:"
echo "Average baseline test execution time: ${AVG_BASELINE_SECONDS}s"
echo "Average plugin test execution time: ${AVG_PLUGIN_SECONDS}s"
echo "Average time overhead: ${AVG_TIME_OVERHEAD}s"
echo "Average percentage time overhead: ${AVG_PERCENTAGE_OVERHEAD}%"
echo "Median percentage time overhead: ${MEDIAN_PERCENTAGE_OVERHEAD}%"
echo "-------------------------------"

