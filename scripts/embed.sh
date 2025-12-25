#!/bin/bash

# Usage: ./embed_project.sh <project_name>
# Example: ./embed_project.sh jacop

# Check if the required argument is provided
if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <project_name>"
    echo "Supported projects: mcs, ripper, batik, checkstyle, zxing, and pdfbox."
    exit 1
fi

# Input argument
PROJECT_NAME=$1

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Define project-specific configurations
case $PROJECT_NAME in
    jacop)
        PROJECT_DIR="../jacop-4.10.0"
        ;;
    mcs)
        PROJECT_DIR="../mcs-0.7.3"
        ;;
    ttorrent)
        PROJECT_DIR="../ttorrent-ttorrent-1.5"
        ;;
    ripper)
        PROJECT_DIR="../certificate-ripper-2.4.1"
        ;;  
    batik)
        PROJECT_DIR="../batikwrapper"
        ;;
    h2)
        PROJECT_DIR="../h2wrapper"
        ;;
    checkstyle)
        PROJECT_DIR="../checkstyle-checkstyle-10.23.0"
        ;;
    zxing)
        PROJECT_DIR="../zxing-wrapper"
        ;;
    pdfbox)
        PROJECT_DIR="../pdfbox-3.0.4"
        ;;
    *)
        echo "Error: Unsupported project '$PROJECT_NAME'"
        echo "Supported projects: jacop, mcs, batik, ripper, h2, checkstyle, zxing"
        exit 1
        ;;
esac

case $PROJECT_NAME in
    pdfbox)
        POM_FILE="app/pom.xml"
        ;;
    *)
        POM_FILE="pom.xml"
        ;;
esac


# Define extra mvn args conditionally
EXTRA_MVN_ARGS=()
if [[ "$PROJECT_NAME" == "checkstyle" ]]; then
    EXTRA_MVN_ARGS+=(-P assembly)  
fi

# Clean the classport-files directory
echo "Cleaning classport-files..."
./clean.sh -cf 
# Navigate to the project directory
echo "Navigating to project directory: $PROJECT_DIR"
cd "$PROJECT_DIR" || { echo "Project directory not found: $PROJECT_DIR"; exit 1; }


# Measure size of the jar without embedding
echo "Measuring size of the JAR without embedding..."
mvn "${EXTRA_MVN_ARGS[@]}" clean package -DskipTests
case $PROJECT_NAME in
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
        APP_JAR="target/checkstyle-10.23.0-all.jar"
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
if [[ "$OSTYPE" == "darwin"* ]]; then
    STAT_CMD=(stat -f%z)
else
    STAT_CMD=(stat -c%s)
fi

BEFORE_SIZE=$("${STAT_CMD[@]}" "$APP_JAR")
echo "Size of the JAR $(realpath $APP_JAR) before embedding: $BEFORE_SIZE bytes"


# Run the embedding process with clean package
echo "Running classport-maven-plugin to embed metadata and package..."
mvn "${EXTRA_MVN_ARGS[@]}" clean package -DskipTests -Pembed

echo "Embedding and packaging completed for project: $PROJECT_NAME"

# Measure size of the JAR after embedding
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

echo "Size of the JAR $(realpath $APP_JAR) after embedding: $AFTER_SIZE bytes"

# Ensure BEFORE_SIZE is set
if [[ -z "$BEFORE_SIZE" || "$BEFORE_SIZE" -eq 0 ]]; then
    echo "Error: BEFORE_SIZE is not set or is zero. Cannot compute size overhead."
    exit 1
fi

# Calculate the size difference
SIZE_DIFF=$((AFTER_SIZE - BEFORE_SIZE))
echo "Size difference after embedding: $SIZE_DIFF bytes"

# Check if the size difference is greater than 0
if [[ $SIZE_DIFF -gt 0 ]]; then
    echo "Embedding process increased the JAR size."
else
    echo "Embedding process did not increase the JAR size."
fi

# Compute the size overhead
SIZE_OVERHEAD=$SIZE_DIFF

# Compute the percentage overhead
PERCENTAGE_OVERHEAD=$(echo "scale=2; ($SIZE_OVERHEAD / $BEFORE_SIZE) * 100" | bc)

# Output the results
echo "-------------------------------"
echo "Size of JAR before embedding: ${BEFORE_SIZE} bytes"
echo "Size of JAR after embedding: ${AFTER_SIZE} bytes"
echo "Size overhead: ${SIZE_OVERHEAD} bytes"
echo "Percentage overhead: ${PERCENTAGE_OVERHEAD}%"
echo "-------------------------------"