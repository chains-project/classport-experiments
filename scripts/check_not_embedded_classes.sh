#!/bin/bash

# Check if the input file is provided as an argument
if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <input_file>"
    exit 1
fi

# Input file from the argument
input_file="$1"

# Define core Java packages
core_packages=("java/" "javax/" "jdk/" "sun/"   "com/sun/"
  "com/oracle/"
  "org/ietf/"
  "org/jcp/"
  "org/omg/"
  "org/w3c/"
  "org/xml/"
  "org/jcp/" "com/apple/" "apple/")

# Read the input file line by line
while IFS= read -r class_name; do
    is_core=0
    # Check if the class belongs to any core package
    for pkg in "${core_packages[@]}"; do
        if [[ "$class_name" == "$pkg"* ]]; then
            is_core=1
            break
        fi
    done

    # If the class is not core, print it to the console
    if [[ $is_core -eq 0 ]]; then
        echo "$class_name"
    fi
done < ../output/"$input_file"