# Experiments - Testing runtime dependency introspection in Java with Classport

This repository contains the experiments to evaluate [Classport](https://github.com/chains-project/classport) tool.
A tool for **runtime dependencies introspection** in Java.
Each experiment includes scripts to automatically build, run, and collect performance data.

## Repository structure

The repository is structured as follows:

* 6 Maven Java projects (listed in the table below)
* [overhead](/overhead/) folder contains the Java Microbenchmark Harness ([JMH](https://github.com/openjdk/jmh)) implementation for each project to comput the runtime overhead
* [scripts](/scripts/) folder contains the script necessarry to run the experiments
* [resources](/resources/) folder contains resources needed by the application under test to perform their workload

| Project | Version | Deps. | Workload |
|--------|---------|-------|----------|
| [PDFBox-app](https://github.com/apache/pdfbox) | [3.0.4](https://github.com/apache/pdfbox/tree/3.0.4) | 12 | Extract text from a PDF file |
| [Certificate-ripper](https://github.com/Hakky54/certificate-ripper) | [2.4.1](https://github.com/Hakky54/certificate-ripper/tree/2.4.1) | 5 | Print the certificate of the [CHAINS website](https://chains.proj.kth.se/) |
| [mcs](https://github.com/mthmulders/mcs) | [0.7.3](https://github.com/mthmulders/mcs/tree/v0.7.3) | 4 | Lookup dependency coordinates in Maven Central |
| [batik](https://github.com/apache/xmlgraphics-batik) | [1.17](https://github.com/apache/xmlgraphics-batik/tree/1_17) | 6 | Convert an SVG to PNG |
| [checkstyle](https://github.com/checkstyle/checkstyle) | [10.23.0](https://github.com/checkstyle/checkstyle/tree/checkstyle-10.23.0) | 34 | Lint a Java file |
| [zxing](https://github.com/zxing/zxing) | [3.5.3](https://github.com/zxing/zxing/tree/zxing-3.5.3) | 4 | Decode 4 QR codes |

## Getting started
### Requirements
* Java 17+
* Maven 3.8+

### Setup
1. Clone the [Classport](https://github.com/chains-project/classport) repo:
```bash 
git clone --recurse-submodules git@github.com:chains-project/classport.git 
```

Check if the classport-experiments repo is not empty.
If it is, do this:

```bash 
cd classport

# Initialize and update all submodules
git submodule update --init --recursive
```

2. Install Classport
```bash
cd classport 
mvn install -DskipTests
```

### How to run the experiments

The objective of the experiments is to test the two main feature of Classport: embedding dependency information into Java class files and instrospecting runtime dependencies during execution.

The experiments map the research questions of the paper:

RQ1. To what extent can Classport effectively embed dependencies into Java binary artifacts?

RQ2. To what extent does Classport support runtime inspection of dependencies?

#### RQ1 Experiments
Before running the experiments:

```bash
cd classport-experiments/scripts
./embed.sh <name-of-the-project>
```

where *name-of-the-project* can be: mcs, ripper, batik, checkstyle, zxing, or pdfbox.

##### Class completeness

Check the class completeness:

```bash
./check_if_deps_embedded.sh <name-of-the-project>
```
where *name-of-the-project* can be: pdfbox, mcs, ripper, batik, checkstyle, or zxing.

The output reports the number of embedded and not embedded classes. It also reports the list of the not embedded files to show that there are no classes but other files, such as images, that are not executed by the JVM and are not part of our study.

##### Dependency completeness
```bash
./check_if_deps_embedded.sh <name-of-the-project> --deps
```

The output lists the embedded dependencies and the ground truth.

##### Build time overhead
```bash
./compute_build_overhead.sh <name-of-the-project>
```

Where *name-of-the-project* can be: pdfbox, mcs, ripper, batik, checkstyle, or zxing.

For build time overhead information check the output in the console.

##### Disk overhead
```bash
cd classport-experiments/scripts
./embed.sh <name-of-the-project>
```

where *name-of-the-project* can be: pdfbox, mcs, ripper, batik, checkstyle, or zxing.

For disk overhead information check the output in the console.

#### RQ2 Experiments
This experiments must be run on the embedded projects, so firts embed them.


##### Dynamic correctness 
Run the project with the workload in the table.
The resuls is a csv file in the output folder (in scripts) with the list of detected dependencies.

```bash
# pdfbox
./introspect_pdfbox.sh

# mcs
./introspect_mcs.sh

# ripper
./introspect_ripper.sh

# batik
./introspect_wrapper.sh batik ../batikwrapper/src/main/resources

# checkstyle
./introspect_checkstyle.sh

# zxing
./introspect_wrapper.sh zxing ../zxing-wrapper/barcodes
```

##### Overhead
```bash
cd overhead

# pdfbox
cd pdfbox-overhead
./compute_overhead.sh

# mcs
cd mcs-overhead 
./compute_overhead.sh

# ripper
cd ripper-overhead 
./compute_overhead.sh

# batik
cd batik-overhead 
./compute_overhead.sh

# checkstyle
cd checkstyle-overhead
./compute_overhead.sh

# zxing
cd zxing-overhead 
./compute_overhead.sh
```

In the project folder a result.json file is created with all the information about the execution time with and without Classport.


