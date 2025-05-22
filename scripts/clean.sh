#!/bin/bash

# Get the directory of the script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Function to remove all "classport-files" folders
clean_classport_files() {
    echo "Removing all 'classport-files' folders..."
    find "$SCRIPT_DIR/../" -type d -name "classport-files" -exec rm -rf {} +
    echo "'classport-files' folders removed."
}

# Function to remove all "all-classport-files" folders
clean_all_classport_files() {
    echo "Removing all 'all-classport-files' folders..."
    find "$SCRIPT_DIR/../" -type d -name "all-classport-files" -exec rm -rf {} +
    echo "'all-classport-files' folders removed."
}

# Function to clean the project (do mvn clean)
clean_project() {
    PROJECT_NAME=$1
    if [ -z "$PROJECT_NAME" ]; then
        echo "Error: No project name provided."
        echo "Usage: $0 --project <project-name>"
        exit 1
    fi

    echo "Cleaning the project: $PROJECT_NAME..."
    PROJECT_DIR="$SCRIPT_DIR/../$PROJECT_NAME"

    if [ -d "$PROJECT_DIR" ]; then
        cd "$PROJECT_DIR" || exit
        mvn clean
        echo "Project '$PROJECT_NAME' cleaned."
    else
        echo "Error: Project directory '$PROJECT_DIR' does not exist."
        exit 1
    fi
}

# Function to remove the content of the "output" folder
clean_output_folder() {
    echo "Cleaning the 'output' folder..."
    if [ -d "$SCRIPT_DIR/../output" ]; then
        rm -rf "$SCRIPT_DIR/../output"/*
        echo "'output' folder cleaned."
    else
        echo "'output' folder does not exist."
    fi
}

# Function to remove the content of the "test" folder
clean_test_folder() {
    echo "Cleaning the 'test-on-output' folder..."
    if [ -d "$SCRIPT_DIR/../test-on-output" ]; then
        rm -rf "$SCRIPT_DIR/../test-on-output"/*
        echo "'test-on-output' folder cleaned."
    else
        echo "'test-on-output' folder does not exist."
    fi
}

# Display usage information
usage() {
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  --project <name>    Clean the specified project"
    echo "  -cf                Clean all 'classport-files' folders"
    echo "  -project           Clean the project"
    echo "  --all-cf           Clean 'all-classport-files' folders"
    echo "  --output            Clean the 'output' folder"
    echo "  --test              Clean the 'test' folder"
    echo "  --all               Clean everything"
    echo "  --help              Display this help message"
}

# Parse command-line arguments
if [ $# -eq 0 ]; then
    usage
    exit 1
fi

while [[ $# -gt 0 ]]; do
    case $1 in
        --project)
            if [ -n "$2" ]; then
                clean_project "$2"
                shift 2
            else
                echo "Error: --project requires a project name."
                exit 1
            fi
            ;;
        --all-cf)
            clean_all_classport_files
            shift
            ;;
        -cf)
            clean_classport_files
            shift
            ;;
        --output)
            clean_output_folder
            shift
            ;;
        --test)
            clean_test_folder
            shift
            ;;
        --all)
            clean_all
            shift
            ;;
        --help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done