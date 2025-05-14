#!/bin/bash

# clean classport-files
./clean.sh --classport-files
cd ../pdfbox-3.0.4
mvn clean

mvn io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed

# Merge the classport-files
cd ../scripts
./post_process_local_repo.sh pdfbox
cd ../pdfbox-3.0.4

# Loop through all modules in the pdfbox project
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
        # Find the corresponding JAR file in the all-classport-files repository
        all_classport_jar=$(find all-classport-files -name "$(basename "$jar_file")" | head -n 1)

        # Replace the old JAR file in all-classport-files with the new one
        if [[ -f "$all_classport_jar" ]]; then
            echo "Replacing $all_classport_jar with the updated JAR file"
            cp "$jar_file" "$all_classport_jar"
        else
            echo "No matching JAR file found in all-classport-files for $jar_file"
        fi
    else
        echo "No JAR file found in $module/target"
    fi

    if [[ -d "$instrumented_classes" ]]; then
    echo "Removing instrumented-classes folder: $instrumented_classes"
    rm -rf "$instrumented_classes"
    fi
done

# Package the parent module
mvn package -Dmaven.repo.local=all-classport-files -DskipTests # instead of this, just repackage the app submodule with the all-classport-files

echo "All modules processed."