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

#### Results

##### PDFBox
```
❯ ./compute_build_overhead.sh pdfbox
Building baseline to measure initial JAR size...
Size of the JAR /mnt/hdd2/amansha/classport/classport-experiments/pdfbox-3.0.4/app/target/pdfbox-app-3.0.4.jar before embedding: 13464234 bytes
Running 10 iterations to measure build times...
Iteration 1/10...
  Run 1: Baseline=0m21.392s (exit=0), Plugin=0m23.809s (exit=0), Overhead=11.29861600%
Iteration 2/10...
  Run 2: Baseline=0m21.632s (exit=0), Plugin=0m23.833s (exit=0), Overhead=10.17474100%
Iteration 3/10...
  Run 3: Baseline=0m21.278s (exit=0), Plugin=0m23.857s (exit=0), Overhead=12.12050000%
Iteration 4/10...
  Run 4: Baseline=0m21.523s (exit=0), Plugin=0m24.039s (exit=0), Overhead=11.68982000%
Iteration 5/10...
  Run 5: Baseline=0m20.904s (exit=0), Plugin=0m23.582s (exit=0), Overhead=12.81094500%
Iteration 6/10...
  Run 6: Baseline=0m21.334s (exit=0), Plugin=0m23.428s (exit=0), Overhead=9.81531800%
Iteration 7/10...
  Run 7: Baseline=0m21.206s (exit=0), Plugin=0m23.769s (exit=0), Overhead=12.08620200%
Iteration 8/10...
  Run 8: Baseline=0m21.507s (exit=0), Plugin=0m23.950s (exit=0), Overhead=11.35909200%
Iteration 9/10...
  Run 9: Baseline=0m21.105s (exit=0), Plugin=0m24.315s (exit=0), Overhead=15.20966500%
Iteration 10/10...
  Run 10: Baseline=0m22.033s (exit=0), Plugin=0m23.364s (exit=0), Overhead=6.04093800%
Measuring size of the JAR after embedding...
-------------------------------
Results across 10 runs:
Average baseline build time: 21.39140000s
Average plugin execution time: 23.79460000s
Average time overhead: 2.40320000s
Average percentage time overhead: 11.26058370%
Median percentage time overhead: 11.52445600%
-------------------------------
Size of JAR before embedding: 13464234 bytes
Size of JAR after embedding: 15325565 bytes
Size overhead: 1861331 bytes
Percentage size overhead: 13.82426200%

```

##### Certificate-ripper
```
❯ ./compute_build_overhead.sh ripper
Building baseline to measure initial JAR size...
Size of the JAR /mnt/hdd2/amansha/classport/classport-experiments/certificate-ripper-2.4.1/target/crip.jar before embedding: 729827 bytes
Running 10 iterations to measure build times...
Iteration 1/10...
  Run 1: Baseline=0m3.995s (exit=0), Plugin=0m4.110s (exit=0), Overhead=2.87859800%
Iteration 2/10...
  Run 2: Baseline=0m3.988s (exit=0), Plugin=0m3.978s (exit=0), Overhead=-.25075200%
Iteration 3/10...
  Run 3: Baseline=0m3.959s (exit=0), Plugin=0m4.214s (exit=0), Overhead=6.44102000%
Iteration 4/10...
  Run 4: Baseline=0m3.768s (exit=0), Plugin=0m4.038s (exit=0), Overhead=7.16560500%
Iteration 5/10...
  Run 5: Baseline=0m3.913s (exit=0), Plugin=0m3.946s (exit=0), Overhead=.84334200%
Iteration 6/10...
  Run 6: Baseline=0m3.697s (exit=0), Plugin=0m4.271s (exit=0), Overhead=15.52610200%
Iteration 7/10...
  Run 7: Baseline=0m3.990s (exit=0), Plugin=0m4.374s (exit=0), Overhead=9.62406000%
Iteration 8/10...
  Run 8: Baseline=0m3.869s (exit=0), Plugin=0m4.189s (exit=0), Overhead=8.27087100%
Iteration 9/10...
  Run 9: Baseline=0m3.675s (exit=0), Plugin=0m4.148s (exit=0), Overhead=12.87074800%
Iteration 10/10...
  Run 10: Baseline=0m3.966s (exit=0), Plugin=0m3.870s (exit=0), Overhead=-2.42057400%
Measuring size of the JAR after embedding...
-------------------------------
Results across 10 runs:
Average baseline build time: 3.88200000s
Average plugin execution time: 4.11380000s
Average time overhead: .23180000s
Average percentage time overhead: 6.09490200%
Median percentage time overhead: 6.80331250%
-------------------------------
Size of JAR before embedding: 729827 bytes
Size of JAR after embedding: 825955 bytes
Size overhead: 96128 bytes
Percentage size overhead: 13.17134000%
-------------------------------
```
##### MCS
```
❯ ./compute_build_overhead.sh mcs
Building baseline to measure initial JAR size...
Size of the JAR /mnt/hdd2/amansha/classport/classport-experiments/mcs-0.7.3/target/mcs-0.7.3.jar before embedding: 1198754 bytes
Running 10 iterations to measure build times...
Iteration 1/10...
  Run 1: Baseline=0m4.508s (exit=0), Plugin=0m4.713s (exit=0), Overhead=4.54747100%
Iteration 2/10...
  Run 2: Baseline=0m4.257s (exit=0), Plugin=0m4.809s (exit=0), Overhead=12.96687800%
Iteration 3/10...
  Run 3: Baseline=0m4.631s (exit=0), Plugin=0m4.890s (exit=0), Overhead=5.59274400%
Iteration 4/10...
  Run 4: Baseline=0m4.185s (exit=0), Plugin=0m4.641s (exit=0), Overhead=10.89605700%
Iteration 5/10...
  Run 5: Baseline=0m4.588s (exit=0), Plugin=0m4.771s (exit=0), Overhead=3.98866600%
Iteration 6/10...
  Run 6: Baseline=0m4.150s (exit=0), Plugin=0m5.190s (exit=0), Overhead=25.06024000%
Iteration 7/10...
  Run 7: Baseline=0m4.626s (exit=0), Plugin=0m4.920s (exit=0), Overhead=6.35538200%
Iteration 8/10...
  Run 8: Baseline=0m4.545s (exit=0), Plugin=0m4.502s (exit=0), Overhead=-.94609400%
Iteration 9/10...
  Run 9: Baseline=0m4.422s (exit=0), Plugin=0m4.815s (exit=0), Overhead=8.88738100%
Iteration 10/10...
  Run 10: Baseline=0m4.320s (exit=0), Plugin=0m4.705s (exit=0), Overhead=8.91203700%
Measuring size of the JAR after embedding...
-------------------------------
Results across 10 runs:
Average baseline build time: 4.42320000s
Average plugin execution time: 4.79560000s
Average time overhead: .37240000s
Average percentage time overhead: 8.62607620%
Median percentage time overhead: 7.62138150%
-------------------------------
Size of JAR before embedding: 1198754 bytes
Size of JAR after embedding: 1317154 bytes
Size overhead: 118400 bytes
Percentage size overhead: 9.87692200%
-------------------------------
```

##### Batik
```
❯ ./compute_build_overhead.sh batik
Building baseline to measure initial JAR size...
Size of the JAR /mnt/hdd2/amansha/classport/classport-experiments/batikwrapper/target/batikwrapper-1.0-SNAPSHOT.jar before embedding: 5685033 bytes
Running 10 iterations to measure build times...
Iteration 1/10...
  Run 1: Baseline=0m2.907s (exit=0), Plugin=0m3.891s (exit=0), Overhead=33.84932900%
Iteration 2/10...
  Run 2: Baseline=0m2.984s (exit=0), Plugin=0m4.147s (exit=0), Overhead=38.97453000%
Iteration 3/10...
  Run 3: Baseline=0m2.921s (exit=0), Plugin=0m3.802s (exit=0), Overhead=30.16090300%
Iteration 4/10...
  Run 4: Baseline=0m2.963s (exit=0), Plugin=0m3.993s (exit=0), Overhead=34.76206500%
Iteration 5/10...
  Run 5: Baseline=0m2.990s (exit=0), Plugin=0m3.811s (exit=0), Overhead=27.45819300%
Iteration 6/10...
  Run 6: Baseline=0m3.123s (exit=0), Plugin=0m3.593s (exit=0), Overhead=15.04963100%
Iteration 7/10...
  Run 7: Baseline=0m2.876s (exit=0), Plugin=0m3.910s (exit=0), Overhead=35.95271200%
Iteration 8/10...
  Run 8: Baseline=0m3.030s (exit=0), Plugin=0m3.886s (exit=0), Overhead=28.25082500%
Iteration 9/10...
  Run 9: Baseline=0m2.892s (exit=0), Plugin=0m3.825s (exit=0), Overhead=32.26141000%
Iteration 10/10...
  Run 10: Baseline=0m2.952s (exit=0), Plugin=0m3.774s (exit=0), Overhead=27.84552800%
Measuring size of the JAR after embedding...
-------------------------------
Results across 10 runs:
Average baseline build time: 2.96380000s
Average plugin execution time: 3.86320000s
Average time overhead: .89940000s
Average percentage time overhead: 30.45651260%
Median percentage time overhead: 31.21115650%
-------------------------------
Size of JAR before embedding: 5685033 bytes
Size of JAR after embedding: 7362637 bytes
Size overhead: 1677604 bytes
Percentage size overhead: 29.50913300%
-------------------------------
```

##### Checkstyle
```
❯ ./compute_build_overhead.sh checkstyle
Building baseline to measure initial JAR size...
Size of the JAR /mnt/hdd2/amansha/classport/classport-experiments/checkstyle-checkstyle-10.23.0/target/checkstyle-10.23.0-all.jar before embedding: 19599577 bytes
Running 10 iterations to measure build times...
Iteration 1/10...
  Run 1: Baseline=0m24.372s (exit=0), Plugin=0m27.534s (exit=0), Overhead=12.97390400%
Iteration 2/10...
  Run 2: Baseline=0m24.518s (exit=0), Plugin=0m27.290s (exit=0), Overhead=11.30597900%
Iteration 3/10...
  Run 3: Baseline=0m24.355s (exit=0), Plugin=0m26.816s (exit=0), Overhead=10.10470100%
Iteration 4/10...
  Run 4: Baseline=0m24.302s (exit=0), Plugin=0m27.089s (exit=0), Overhead=11.46819100%
Iteration 5/10...
  Run 5: Baseline=0m24.799s (exit=0), Plugin=0m27.067s (exit=0), Overhead=9.14553000%
Iteration 6/10...
  Run 6: Baseline=0m24.267s (exit=0), Plugin=0m27.192s (exit=0), Overhead=12.05340500%
Iteration 7/10...
  Run 7: Baseline=0m24.052s (exit=0), Plugin=0m26.820s (exit=0), Overhead=11.50839800%
Iteration 8/10...
  Run 8: Baseline=0m24.626s (exit=0), Plugin=0m27.206s (exit=0), Overhead=10.47673100%
Iteration 9/10...
  Run 9: Baseline=0m24.482s (exit=0), Plugin=0m27.841s (exit=0), Overhead=13.72028400%
Iteration 10/10...
  Run 10: Baseline=0m23.943s (exit=0), Plugin=0m26.286s (exit=0), Overhead=9.78574100%
Measuring size of the JAR after embedding...
-------------------------------
Results across 10 runs:
Average baseline build time: 24.37160000s
Average plugin execution time: 27.11410000s
Average time overhead: 2.74250000s
Average percentage time overhead: 11.25428640%
Median percentage time overhead: 11.38708500%
-------------------------------
Size of JAR before embedding: 19599577 bytes
Size of JAR after embedding: 22970567 bytes
Size overhead: 3370990 bytes
Percentage size overhead: 17.19929900%
-------------------------------
```



##### ZXing
```
❯ ./compute_build_overhead.sh zxing
Building baseline to measure initial JAR size...
Size of the JAR /mnt/hdd2/amansha/classport/classport-experiments/zxing-wrapper/target/zxing-workload-1.0-jar-with-dependencies.jar before embedding: 1356347 bytes
Running 10 iterations to measure build times...
Iteration 1/10...
  Run 1: Baseline=0m3.062s (exit=0), Plugin=0m3.265s (exit=0), Overhead=6.62965300%
Iteration 2/10...
  Run 2: Baseline=0m3.144s (exit=0), Plugin=0m3.510s (exit=0), Overhead=11.64122100%
Iteration 3/10...
  Run 3: Baseline=0m3.205s (exit=0), Plugin=0m3.184s (exit=0), Overhead=-.65522600%
Iteration 4/10...
  Run 4: Baseline=0m3.055s (exit=0), Plugin=0m3.506s (exit=0), Overhead=14.76268400%
Iteration 5/10...
  Run 5: Baseline=0m3.218s (exit=0), Plugin=0m3.077s (exit=0), Overhead=-4.38160300%
Iteration 6/10...
  Run 6: Baseline=0m3.181s (exit=0), Plugin=0m3.319s (exit=0), Overhead=4.33825800%
Iteration 7/10...
  Run 7: Baseline=0m3.173s (exit=0), Plugin=0m3.408s (exit=0), Overhead=7.40624000%
Iteration 8/10...
  Run 8: Baseline=0m3.478s (exit=0), Plugin=0m3.266s (exit=0), Overhead=-6.09545700%
Iteration 9/10...
  Run 9: Baseline=0m3.240s (exit=0), Plugin=0m3.209s (exit=0), Overhead=-.95679000%
Iteration 10/10...
  Run 10: Baseline=0m3.289s (exit=0), Plugin=0m3.240s (exit=0), Overhead=-1.48981400%
Measuring size of the JAR after embedding...
-------------------------------
Results across 10 runs:
Average baseline build time: 3.20450000s
Average plugin execution time: 3.29840000s
Average time overhead: .09390000s
Average percentage time overhead: 3.11991660%
Median percentage time overhead: 1.84151600%
-------------------------------
Size of JAR before embedding: 1356347 bytes
Size of JAR after embedding: 1508051 bytes
Size overhead: 151704 bytes
Percentage size overhead: 11.18474800%
-------------------------------
```

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


