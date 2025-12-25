#!/bin/bash

# Usage: ./compute_build_overhead.sh <program_name>
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
#   jacop)
#     PROJECT_DIR="$SCRIPT_DIR/../jacop-4.10.0/target/jacop-4.10.0.jar"
#     ;;

#   ttorrent)
#     PROJECT_DIR="$SCRIPT_DIR/../ttorrent-ttorrent-1.5/cli/target/ttorrent-cli-1.5-shaded.jar"
#     ;;
#   graph)
#     PROJECT_DIR="$SCRIPT_DIR/../graphhopper/graphhopper/web/target/graphhopper-web-9.1.jar"
#     ;;
#   commons)
#     PROJECT_DIR="$SCRIPT_DIR/../commons-validator-1.9.0-src/target/commons-validator-1.9.0.jar"
#     ;;
  *)
    echo "Error: Unsupported program '$PROGRAM'"
    echo "Supported programs: pdfbox, checkstyle"
    exit 1
    ;;
esac

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
        APP_JAR="target/batikwrapper-1.0-SNAPSHOT.jar"
        ;;
    h2)
        APP_JAR="target/h2wrapper-1.0-SNAPSHOT.jar"
        ;;
    checkstyle)
        APP_JAR="target/checkstyle-10.23.0.jar"
        ;;
    zxing)
        APP_JAR="target/zxing-workload-1.0-jar-with-dependencies.jar"
        ;;
    pdfbox)
        APP_JAR="app/target/pdfbox-app-3.0.4.jar"
        ;;
    *)
        echo "Error: Unsupported project '$PROJECT_NAME'"
        echo "Supported projects: jacop, mcs, ttorrent, h2, checkstyle, batik, ripper, zxing"
        exit 1
        ;;  
esac

cd "$PROJECT_DIR" || exit 1
# Measure baseline build time
echo "Measuring baseline build time..."
BASELINE_TIME=$( { time mvn clean package -DskipTests; } 2>&1 | grep real | awk '{print $2}' )

if [[ "$OSTYPE" == "darwin"* ]]; then
    STAT_CMD=(stat -f%z)
else
    STAT_CMD=(stat -c%s)
fi

BEFORE_SIZE=$("${STAT_CMD[@]}" "$APP_JAR")
echo "Size of the JAR $(realpath $APP_JAR) before embedding: $BEFORE_SIZE bytes"

# Measure plugin execution time
echo "Measuring plugin execution time..."
PLUGIN_TIME=$( { time mvn clean package -DskipTests -Pembed; } 2>&1 | grep real | awk '{print $2}' )

echo "Measuring size of the JAR after embedding..."
if [[ ! -f "$APP_JAR" ]]; then
    echo "Error: JAR file not found at $APP_JAR"
    exit 1
fi

if [[ "$OSTYPE" == "darwin"* ]]; then
    STAT_CMD=(stat -f%z)
else
    STAT_CMD=(stat -c%s)
fi

AFTER_SIZE=$("${STAT_CMD[@]}" "$APP_JAR")


# Convert times to seconds
BASELINE_SECONDS=$(echo $BASELINE_TIME | LC_NUMERIC=C awk -F'[ms]' '{sub(/,/,".",$2); printf "%.3f", $1 * 60 + $2}')
PLUGIN_SECONDS=$(echo $PLUGIN_TIME | LC_NUMERIC=C awk -F'[ms]' '{sub(/,/,".",$2); printf "%.3f", $1 * 60 + $2}')


# Compute overhead
TIME_OVERHEAD=$(echo "$PLUGIN_SECONDS - $BASELINE_SECONDS" | bc)
PERCENTAGE_TIME_OVERHEAD=$(echo "scale=8; ($TIME_OVERHEAD / $BASELINE_SECONDS) * 100" | bc)

SIZE_OVERHEAD=$((AFTER_SIZE - BEFORE_SIZE))
PERCENTAGE_SIZE_OVERHEAD=$(echo "scale=8; ($SIZE_OVERHEAD / $BEFORE_SIZE) * 100" | bc)


echo "-------------------------------"
echo "Baseline build time: $BASELINE_TIME"
echo "Plugin execution time: $PLUGIN_TIME"
echo "Time overhead: ${TIME_OVERHEAD}s"
echo "Percentage time overhead: ${PERCENTAGE_TIME_OVERHEAD}%"
echo "-------------------------------"
echo "Size of JAR before embedding: ${BEFORE_SIZE} bytes"
echo "Size of JAR after embedding: ${AFTER_SIZE} bytes"
echo "Size overhead: ${SIZE_OVERHEAD} bytes"
echo "Percentage size overhead: ${PERCENTAGE_SIZE_OVERHEAD}%"
echo "-------------------------------"
