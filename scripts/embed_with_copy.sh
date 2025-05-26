#!/bin/bash

# Usage: ./embed_with_copy.sh <project_directory> <program> <executable-module>
# Example: ./embed_with_copy.sh pdfbox-3.0.4 pdfbox app

# Check if the required arguments are provided
if [[ $# -ne 3 ]]; then
    echo "Usage: $0 <project_directory> <program> <executable-module"   
    exit 1
fi

# Input arguments
PROJECT_DIR="$1"
CLASSPORT_FILES_DIR="all-classport-files"
PROGRAM="$2"
EXECUTABLE_MODULE="$3"

# Clean the classport-files directory
# clean classport-files
./clean.sh -cf

# Navigate to the project directory
echo "Navigating to project directory: $PROJECT_DIR"
cd "../$PROJECT_DIR" || { echo "Project directory not found: $PROJECT_DIR"; exit 1; }

# Clean the project
echo "Cleaning the project..."
mvn clean

# Measure size of the jar without embedding
echo "Measuring size of the JAR without embedding..."
mvn package 
case $PROGRAM in
  pdfbox)
    APP_JAR="target/pdfbox-app-3.0.4.jar"
    ;;
*)
    echo "Error: Unsupported program '$PROGRAM'"
    echo "Supported programs: pdfbox"
    exit 1
    ;;
esac
BEFORE_SIZE=$(stat -f%z "$EXECUTABLE_MODULE/$APP_JAR")
echo "Size of the JAR before embedding: $BEFORE_SIZE bytes"

# Clean the project
echo "Cleaning the project..."
mvn clean

# Run the classport-maven-plugin to embed metadata
echo "Running classport-maven-plugin to embed metadata..."
mvn compile io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed

# Merge the classport-files (if applicable)
if [[ -f "../scripts/post_process_local_repo.sh" ]]; then
    echo "Merging classport-files..."
    cd ../scripts || exit 1
    ./post_process_local_repo.sh "$(basename "$PROGRAM")"
    cd "../$PROJECT_DIR" || exit 1
fi

# Process each module in the project
echo "Processing modules in the project..."
for module in $(find . -name "target" -type d | grep "/target$" | sed 's|/target||'); do
    echo "Processing module: $module"

    # Define paths
    target_classes="$module/target/classes"
    instrumented_classes="$module/instrumented-classes"

    # Ensure the instrumented-classes directory exists
    mkdir -p "$instrumented_classes"

    # Copy all .class files from target/classes to instrumented-classes
    if [[ -d "$target_classes" ]]; then
        echo "Copying .class files from $target_classes to $instrumented_classes"
        cp -r "$target_classes/"* "$instrumented_classes/"
        cd "$module"
        mvn package -Dmaven.repo.local=../all-classport-files -DskipTests
        cd ..
    else
        echo "No classes found in $target_classes"
        continue
    fi
done

# Loop through all modules again to update the JAR files
for module in $(find . -name "target" -type d | grep "/target$" | sed 's|/target||'); do
    echo "Updating JAR file for module: $module"
    instrumented_classes="$module/instrumented-classes"

    # Find the JAR file in the target directory
    jar_file=$(find "$module/target" -maxdepth 1 -name "*.jar" | head -n 1)

    # Update the JAR file with the instrumented classes
    if [[ -f "$jar_file" ]]; then
        echo "Updating JAR file: $jar_file"
        jar uf "$jar_file" -C "$instrumented_classes" .

        # Replace the old JAR file in the classport-files directory
        all_classport_jar=$(find "$CLASSPORT_FILES_DIR" -name "$(basename "$jar_file")" | head -n 1)
        if [[ -f "$all_classport_jar" ]]; then
            echo "Replacing $all_classport_jar with the updated JAR file"
            cp "$jar_file" "$all_classport_jar"
        else
            echo "No matching JAR file found in $CLASSPORT_FILES_DIR for $jar_file"
        fi
    else
        echo "No JAR file found in $module/target"
    fi

    # Remove the instrumented-classes folder
    if [[ -d "$instrumented_classes" ]]; then
        echo "Removing instrumented-classes folder: $instrumented_classes"
        rm -rf "$instrumented_classes"
    fi
done

cd $EXECUTABLE_MODULE
mvn package -Dmaven.repo.local=../all-classport-files -DskipTests 

# Measure size of the jar after embedding
echo "Measuring size of the JAR after embedding..."
AFTER_SIZE=$(stat -f%z "$APP_JAR")

# Compute the size overhead
SIZE_OVERHEAD=$(echo "$AFTER_SIZE - $BEFORE_SIZE" | bc)

# Compute the percentage overhead
PERCENTAGE_OVERHEAD=$(echo "scale=2; ($SIZE_OVERHEAD / $BEFORE_SIZE) * 100" | bc)

# Output the results
echo "-------------------------------"
echo "Size of JAR before embedding: ${BEFORE_SIZE} bytes"
echo "Size of JAR after embedding: ${AFTER_SIZE} bytes"
echo "Size overhead: ${SIZE_OVERHEAD} bytes"
echo "Percentage overhead: ${PERCENTAGE_OVERHEAD}%"
echo "-------------------------------"


echo "All modules processed."