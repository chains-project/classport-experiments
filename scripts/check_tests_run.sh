#!/bin/bash
set -e

# Check if the required argument is provided
if [[ $# -ne 1 ]]; then
    echo "Usage: $0 <project_name>"
    echo "Supported projects: mcs, ripper, checkstyle, pdfbox"
    exit 1
fi

# Input argument
PROJECT_NAME=$1

# Define project-specific configurations
case $PROJECT_NAME in
    mcs)
        PROJECT_DIR="../mcs-0.7.3"
        ;;
    ripper)
        PROJECT_DIR="../certificate-ripper-2.4.1"
        ;;  
    checkstyle)
        PROJECT_DIR="../checkstyle-checkstyle-10.23.0"
        ;;
    pdfbox)
        PROJECT_DIR="../pdfbox-3.0.4"
        ;;
    zxing)
        PROJECT_DIR="../zxing-wrapper"
        ;;
    *)
        echo "Error: Unsupported project '$PROJECT_NAME'"
        echo "Supported projects: jacop, mcs, batik, ripper, h2, checkstyle, zxing, pdfbox"
        exit 1
        ;;
esac

# Clean the classport-files directory
echo "Cleaning classport-files..."
./clean.sh -cf 

# Navigate to the project directory
echo "Navigating to project directory: $PROJECT_DIR"
cd "$PROJECT_DIR" || { echo "Project directory not found: $PROJECT_DIR"; exit 1; }

echo "Running tests BEFORE embedding..."
mvn clean test | tee test-before.log
# cp -r target/surefire-reports baseline-surefire-reports

mvn clean

echo "Running Classport plugin..."
mvn compile io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed

if [ "$PROJECT_NAME" = "pdfbox" ]; then
    # Merge the classport-files
    cd ../scripts
    ./post_process_local_repo.sh pdfbox
    cd "$PROJECT_DIR" || { echo "Project directory not found: $PROJECT_DIR"; exit 1; }
    echo "Running tests AFTER embedding..."
    mvn test -Dmaven.repo.local=all-classport-files | tee test-after.log
else
    echo "Running tests AFTER embedding..."
    mvn test -Dmaven.repo.local=classport-files | tee test-after.log
    # cp -r target/surefire-reports classport-surefire-reports
fi


echo "Running tests AFTER embedding..."
mvn test -Dmaven.repo.local=classport-files | tee test-after.log
# cp -r target/surefire-reports classport-surefire-reports

echo "Comparing results..."
grep "Tests run:" test-before.log > summary-before.txt
grep "Tests run:" test-after.log > summary-after.txt

echo "===== BEFORE ====="
cat summary-before.txt
echo "===== AFTER ====="
cat summary-after.txt
echo "===== DIFF ====="
diff summary-before.txt summary-after.txt || echo "No differences found."

# Clean
# rm summary-before.txt
# rm summary-after.txt
# rm test-before.log
# rm test-after.log
