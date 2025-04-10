#!/bin/bash
# cut -d',' -f1 ../output/output.csv | tail -n +2 | sort | uniq > test.txt

# Check if the input file is provided as an argument
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <name_of_csv_to_check>"
  exit 1
fi

# Assign the argument to a variable
OUTPUT_CSV="$1"

# Check if the forlder test exists, if not create it
if [ ! -d "../output/test-on-output" ]; then
  mkdir ../output/test-on-output
fi

# Process the file
cut -d',' -f5 ../output/"$OUTPUT_CSV" | tail -n +2 | sort | uniq > ../output/test-on-output/"$OUTPUT_CSV"_test.txt