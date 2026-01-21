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
| [Biojava](https://github.com/biojava/biojava) | [7.2.4](https://github.com/biojava/biojava/releases/tag/biojava-7.2.4) | 10 | Calculate the properties of a protein |
| [checkstyle](https://github.com/checkstyle/checkstyle) | [10.23.0](https://github.com/checkstyle/checkstyle/tree/checkstyle-10.23.0) | 34 | Lint a Java file |
| [GraphHopper](https://github.com/graphhopper/graphhopper) | [11.0](https://github.com/graphhopper/graphhopper/releases/tag/11.0) | 4 | Test |

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

For getting the count of depedencies for each Maven project, run the following command:
```
mvn org.apache.maven.plugins:maven-dependency-plugin:3.9.0:collect | grep -E ':(runtime|compile|provided|system)$' | sed 's/\[INFO\]//' | cut -d ':' -f 1,2,4 | sort | uniq | wc -l
```

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
```
commons-io:commons-io:2.18.0
commons-logging:commons-logging:1.3.4
info.picocli:picocli:4.7.6
org.apache.ant:ant:1.10.15
org.apache.ant:ant-launcher:1.10.15
org.apache.lucene:lucene-analyzers-common:8.11.4
org.apache.lucene:lucene-core:8.11.4
org.apache.pdfbox,pdfbox-examples,3.0.4
org.apache.pdfbox:fontbox:3.0.4
org.apache.pdfbox:jbig2-imageio:3.0.4
org.apache.pdfbox:pdfbox:3.0.4
org.apache.pdfbox:pdfbox-debugger:3.0.4
org.apache.pdfbox:pdfbox-io:3.0.4
org.apache.pdfbox:pdfbox-tools:3.0.4
org.apache.pdfbox:preflight:3.0.4
org.apache.pdfbox:xmpbox:3.0.4
org.bouncycastle:bcpkix-jdk18on:1.80
org.bouncycastle:bcprov-jdk18on:1.80
org.bouncycastle:bcutil-jdk18on:1.80
>>> 19
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
```
info.picocli:picocli:4.7.6
info.picocli:picocli-codegen:4.7.6
io.github.hakky54:sslcontext-kickstart:9.1.0
org.slf4j:slf4j-api:2.0.17
org.slf4j:slf4j-simple:2.0.17
>>> 5
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
```
com.fasterxml.jackson.core:jackson-core:2.18.2
com.fasterxml.jackson.jr:jackson-jr-objects:2.18.2
com.github.package-url:packageurl-java:1.5.0
com.github.stefanbirkner:system-lambda:1.2.1
info.picocli:picocli:4.7.6
>>> 5
```

##### Biojava
```
❯ ./compute_build_overhead.sh biojava
Building baseline to measure initial JAR size...
Size of the JAR /mnt/hdd2/amansha/classport/classport-experiments/biojava-7.2.4/biojava-aa-prop/target/AAProperties-jar-with-dependencies.jar before embedding: 14159906 bytes
Running 10 iterations to measure build times...
Iteration 1/10...
  Run 1: Baseline=16.353 s (exit=0), Plugin=17.694 s (exit=0), Overhead=8.20033000%
Iteration 2/10...
  Run 2: Baseline=16.408 s (exit=0), Plugin=17.757 s (exit=0), Overhead=8.22159900%
Iteration 3/10...
  Run 3: Baseline=17.442 s (exit=0), Plugin=18.141 s (exit=0), Overhead=4.00756700%
Iteration 4/10...
  Run 4: Baseline=16.750 s (exit=0), Plugin=17.778 s (exit=0), Overhead=6.13731300%
Iteration 5/10...
  Run 5: Baseline=16.252 s (exit=0), Plugin=17.939 s (exit=0), Overhead=10.38026000%
Iteration 6/10...
  Run 6: Baseline=16.272 s (exit=0), Plugin=17.022 s (exit=0), Overhead=4.60914400%
Iteration 7/10...
  Run 7: Baseline=16.106 s (exit=0), Plugin=17.033 s (exit=0), Overhead=5.75561900%
Iteration 8/10...
  Run 8: Baseline=16.157 s (exit=0), Plugin=17.557 s (exit=0), Overhead=8.66497400%
Iteration 9/10...
  Run 9: Baseline=16.343 s (exit=0), Plugin=17.918 s (exit=0), Overhead=9.63715300%
Iteration 10/10...
  Run 10: Baseline=16.895 s (exit=0), Plugin=17.655 s (exit=0), Overhead=4.49837200%
Measuring size of the JAR after embedding...
-------------------------------
Results across 10 runs:
Average baseline build time: 16.49780000s
Average plugin execution time: 17.64940000s
Average time overhead: 1.15160000s
Average percentage time overhead: 7.01123310%
Median percentage time overhead: 7.16882150%
-------------------------------
Size of JAR before embedding: 14159906 bytes
Size of JAR after embedding: 16593708 bytes
Size overhead: 2433802 bytes
Percentage size overhead: 17.18798100%
-------------------------------
```
```
com.fasterxml.jackson.core:jackson-annotations:2.13.4
com.fasterxml.jackson.core:jackson-core:2.13.4
com.fasterxml.jackson.core:jackson-databind:2.13.4.2
com.google.code.findbugs:jsr305:3.0.2
com.google.code.gson:gson:2.10
com.google.errorprone:error_prone_annotations:2.11.0
com.google.guava:failureaccess:1.0.1
com.google.guava:guava:31.1-jre
com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
com.google.j2objc:j2objc-annotations:1.3
commons-beanutils:commons-beanutils:1.8.0
commons-codec:commons-codec:1.5
commons-collections:commons-collections:3.2.1
commons-lang:commons-lang:2.4
commons-lang:commons-lang:2.5
commons-logging:commons-logging:1.1.1
com.sun.activation:jakarta.activation:1.2.2
com.sun.istack:istack-commons-runtime:3.0.12
jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
java3d:vecmath:1.3.1
javax.activation:javax.activation-api:1.2.0
javax.annotation:javax.annotation-api:1.3.2
javax.xml.bind:jaxb-api:2.3.1
net.sf.ezmorph:ezmorph:1.0.6
net.sf.json-lib:json-lib:jdk15
net.sourceforge.jmol:jmol:13.0.14
net.sourceforge.jmol:jmol:14.31.10
openchart:openchart:1.4.2
org.apache.commons:commons-math:2.2
org.apache.logging.log4j:log4j-api:2.17.2
org.apache.logging.log4j:log4j-core:2.17.2
org.apache.logging.log4j:log4j-slf4j-impl:2.17.2
org.biojava:biojava-alignment:7.0.2
org.biojava:biojava-core:7.0.2
org.biojava:biojava-structure:7.0.2
org.biojava:biojava-structure-gui:7.0.2
org.biojava:jcolorbrewer:5.2
org.biojava.thirdparty:forester:1.039
org.checkerframework:checker-qual:3.12.0
org.glassfish.jaxb:jaxb-runtime:2.3.5
org.glassfish.jaxb:txw2:2.3.5
org.jgrapht:jgrapht-core:1.4.0
org.jheaps:jheaps:0.11
org.msgpack:jackson-dataformat-msgpack:0.8.24
org.msgpack:msgpack-core:0.8.24
org.rcsb:ciftools-java:5.0.1
org.rcsb:mmtf-api:1.0.11
org.rcsb:mmtf-codec:1.0.11
org.rcsb:mmtf-serialization:1.0.11
org.slf4j:slf4j-api:1.7.30
>>> 50
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
```
com.google.code.findbugs:jsr305:3.0.2
com.google.errorprone:error_prone_annotations:2.36.0
com.google.guava:failureaccess:1.0.3
com.google.guava:guava:33.4.6-jre
com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
com.google.j2objc:j2objc-annotations:3.0.0
commons-beanutils:commons-beanutils:1.10.1
commons-codec:commons-codec:1.17.0
commons-collections:commons-collections:3.2.2
commons-logging:commons-logging:1.2
info.picocli:picocli:4.7.6
net.sf.saxon:Saxon-HE:12.5
org.antlr:antlr4-runtime:4.13.2
org.apache.ant:ant:1.10.15
org.apache.ant:ant-launcher:1.10.15
org.apache.commons:commons-lang3:3.8.1
org.apache.commons:commons-text:1.3
org.apache.httpcomponents.client5:httpclient5:5.1.3
org.apache.httpcomponents.core5:httpcore5:5.1.3
org.apache.httpcomponents.core5:httpcore5-h2:5.1.3
org.apache.httpcomponents:httpclient:4.5.13
org.apache.httpcomponents:httpcore:4.4.14
org.apache.maven.doxia:doxia-core:1.12.0
org.apache.maven.doxia:doxia-logging-api:1.12.0
org.apache.maven.doxia:doxia-module-xdoc:1.12.0
org.apache.maven.doxia:doxia-sink-api:1.12.0
org.apache.xbean:xbean-reflect:3.7
org.checkerframework:checker-qual:3.49.1
org.codehaus.plexus:plexus-classworlds:2.6.0
org.codehaus.plexus:plexus-component-annotations:2.1.0
org.codehaus.plexus:plexus-container-default:2.1.0
org.codehaus.plexus:plexus-utils:3.3.0
org.javassist:javassist:3.28.0-GA
org.jspecify:jspecify:1.0.0
org.reflections:reflections:0.10.2
org.slf4j:slf4j-api:1.7.32
org.xmlresolver:xmlresolver:5.2.2
>>> 37
```


##### Graphhopper
```
❯ ./compute_build_overhead.sh graphhopper
Building baseline to measure initial JAR size...
Size of the JAR /mnt/hdd2/amansha/classport/classport-experiments/graphhopper-11.0/web/target/graphhopper-web-11.0-SNAPSHOT.jar before embedding: 47345070 bytes
Running 10 iterations to measure build times...
Iteration 1/10...
  Run 1: Baseline=39.989 s (exit=0), Plugin=46.761 s (exit=0), Overhead=16.93465700%
Iteration 2/10...
  Run 2: Baseline=40.335 s (exit=0), Plugin=47.143 s (exit=0), Overhead=16.87864100%
Iteration 3/10...
  Run 3: Baseline=40.508 s (exit=0), Plugin=46.986 s (exit=0), Overhead=15.99190200%
Iteration 4/10...
  Run 4: Baseline=41.272 s (exit=0), Plugin=47.022 s (exit=0), Overhead=13.93196300%
Iteration 5/10...
  Run 5: Baseline=40.538 s (exit=0), Plugin=47.564 s (exit=0), Overhead=17.33188600%
Iteration 6/10...
  Run 6: Baseline=40.281 s (exit=0), Plugin=47.292 s (exit=0), Overhead=17.40522800%
Iteration 7/10...
  Run 7: Baseline=39.999 s (exit=0), Plugin=48.145 s (exit=0), Overhead=20.36550900%
Iteration 8/10...
  Run 8: Baseline=40.989 s (exit=0), Plugin=46.698 s (exit=0), Overhead=13.92812700%
Iteration 9/10...
  Run 9: Baseline=40.058 s (exit=0), Plugin=46.579 s (exit=0), Overhead=16.27889500%
Iteration 10/10...
  Run 10: Baseline=41.511 s (exit=0), Plugin=46.690 s (exit=0), Overhead=12.47621100%
Measuring size of the JAR after embedding...
-------------------------------
Results across 10 runs:
Average baseline build time: 40.54800000s
Average plugin execution time: 47.08800000s
Average time overhead: 6.54000000s
Average percentage time overhead: 16.15230190%
Median percentage time overhead: 16.57876800%
-------------------------------
Size of JAR before embedding: 47345070 bytes
Size of JAR after embedding: 53482482 bytes
Size overhead: 6137412 bytes
Percentage size overhead: 12.96314900%
-------------------------------
```
```
ch.qos.logback.access:logback-access-common:2.0.6
ch.qos.logback.access:logback-access-jetty11:2.0.6
ch.qos.logback:logback-classic:1.5.18
ch.qos.logback:logback-core:1.5.18
com.carrotsearch:hppc:0.8.1
com.fasterxml:classmate:1.7.0
com.fasterxml.jackson.core:jackson-annotations:2.19.2
com.fasterxml.jackson.core:jackson-core:2.19.2
com.fasterxml.jackson.core:jackson-databind:2.19.2
com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.19.2
com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.2
com.fasterxml.jackson.datatype:jackson-datatype-guava:2.19.2
com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2
com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2
com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-base:2.19.2
com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider:2.19.2
com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-xml-provider:2.19.2
com.fasterxml.jackson.module:jackson-module-blackbird:2.19.2
com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations:2.19.2
com.fasterxml.jackson.module:jackson-module-parameter-names:2.19.2
com.fasterxml.woodstox:woodstox-core:7.1.1
com.github.ben-manes.caffeine:caffeine:3.2.2
com.google.errorprone:error_prone_annotations:2.31.0
com.google.guava:failureaccess:1.0.3
com.google.guava:guava:33.4.8-jre
com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
com.google.j2objc:j2objc-annotations:3.0.0
com.google.protobuf:protobuf-java:3.12.2
com.graphhopper.external:jackson-datatype-jts:2.19.2
com.graphhopper:graphhopper-core:11.0-SNAPSHOT
com.graphhopper:graphhopper-map-matching:11.0-SNAPSHOT
com.graphhopper:graphhopper-nav:11.0-SNAPSHOT
com.graphhopper:graphhopper-reader-gtfs:11.0-SNAPSHOT
com.graphhopper:graphhopper-web-api:11.0-SNAPSHOT
com.graphhopper:graphhopper-web-bundle:11.0-SNAPSHOT
com.helger:profiler:1.1.1
commons-io:commons-io:2.14.0
com.squareup.okhttp3:okhttp:4.11.0
com.squareup.okio:okio:3.2.0
com.squareup.okio:okio-jvm:3.2.0
com.sun.activation:jakarta.activation:2.0.1
de.westnordost:osm-legal-default-speeds-jvm:1.4
io.dropwizard:dropwizard-assets:4.0.16
io.dropwizard:dropwizard-client:4.0.16
io.dropwizard:dropwizard-configuration:4.0.16
io.dropwizard:dropwizard-core:4.0.16
io.dropwizard:dropwizard-health:4.0.16
io.dropwizard:dropwizard-jackson:4.0.16
io.dropwizard:dropwizard-jersey:4.0.16
io.dropwizard:dropwizard-jetty:4.0.16
io.dropwizard:dropwizard-lifecycle:4.0.16
io.dropwizard:dropwizard-logging:4.0.16
io.dropwizard:dropwizard-metrics:4.0.16
io.dropwizard:dropwizard-request-logging:4.0.16
io.dropwizard:dropwizard-servlets:4.0.16
io.dropwizard:dropwizard-util:4.0.16
io.dropwizard:dropwizard-validation:4.0.16
io.dropwizard.logback:logback-throttling-appender:1.5.1
io.dropwizard.metrics:metrics-annotation:4.2.34
io.dropwizard.metrics:metrics-core:4.2.34
io.dropwizard.metrics:metrics-healthchecks:4.2.34
io.dropwizard.metrics:metrics-httpclient5:4.2.34
io.dropwizard.metrics:metrics-jakarta-servlets:4.2.34
io.dropwizard.metrics:metrics-jersey3:4.2.34
io.dropwizard.metrics:metrics-jetty11:4.2.34
io.dropwizard.metrics:metrics-jmx:4.2.34
io.dropwizard.metrics:metrics-json:4.2.34
io.dropwizard.metrics:metrics-jvm:4.2.34
io.dropwizard.metrics:metrics-logback:4.2.34
jakarta.activation:jakarta.activation-api:2.0.1
jakarta.annotation:jakarta.annotation-api:2.0.0
jakarta.el:jakarta.el-api:4.0.0
jakarta.inject:jakarta.inject-api:2.0.1
jakarta.servlet:jakarta.servlet-api:5.0.0
jakarta.validation:jakarta.validation-api:3.0.2
jakarta.ws.rs:jakarta.ws.rs-api:3.0.0
jakarta.xml.bind:jakarta.xml.bind-api:3.0.1
net.sourceforge.argparse4j:argparse4j:0.9.0
net.sourceforge.javacsv:javacsv:2.0
org.apache.commons:commons-compress:1.26.0
org.apache.commons:commons-lang3:3.18.0
org.apache.commons:commons-text:1.14.0
org.apache.httpcomponents.client5:httpclient5:5.5
org.apache.httpcomponents.core5:httpcore5:5.3.4
org.apache.httpcomponents.core5:httpcore5-h2:5.3.4
org.apache.xmlgraphics:xmlgraphics-commons:2.7
org.checkerframework:checker-qual:3.49.5
org.codehaus.janino:commons-compiler:3.1.9
org.codehaus.janino:janino:3.1.9
org.codehaus.woodstox:stax2-api:4.2.2
org.eclipse.jetty:jetty-http:11.0.26
org.eclipse.jetty:jetty-io:11.0.26
org.eclipse.jetty:jetty-security:11.0.26
org.eclipse.jetty:jetty-server:11.0.26
org.eclipse.jetty:jetty-servlet:11.0.26
org.eclipse.jetty:jetty-servlets:11.0.26
org.eclipse.jetty:jetty-util:11.0.26
org.eclipse.jetty.toolchain.setuid:jetty-setuid-java:1.0.4
org.glassfish.hk2.external:aopalliance-repackaged:3.0.6
org.glassfish.hk2:hk2-api:3.0.6
org.glassfish.hk2:hk2-locator:3.0.6
org.glassfish.hk2:hk2-utils:3.0.6
org.glassfish.hk2:osgi-resource-locator:1.0.3
org.glassfish:jakarta.el:4.0.2
org.glassfish.jersey.containers:jersey-container-servlet:3.0.18
org.glassfish.jersey.containers:jersey-container-servlet-core:3.0.18
org.glassfish.jersey.core:jersey-client:3.0.18
org.glassfish.jersey.core:jersey-common:3.0.18
org.glassfish.jersey.core:jersey-server:3.0.18
org.glassfish.jersey.ext:jersey-bean-validation:3.0.18
org.glassfish.jersey.ext:jersey-metainf-services:3.0.18
org.glassfish.jersey.inject:jersey-hk2:3.0.18
org.hibernate.validator:hibernate-validator:7.0.5.Final
org.javassist:javassist:3.30.2-GA
org.jboss.logging:jboss-logging:3.6.1.Final
org.jetbrains:annotations:13.0
org.jetbrains.kotlin:kotlin-stdlib:1.6.20
org.jetbrains.kotlin:kotlin-stdlib-common:1.6.20
org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.20
org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.20
org.jspecify:jspecify:1.0.0
org.locationtech.jts:jts-core:1.20.0
org.mapdb:mapdb:1.0.8
org.mobilitydata:gtfs-realtime-bindings:0.0.8
org.openstreetmap.osmosis:osmosis-osm-binary:0.48.3
org.slf4j:jcl-over-slf4j:2.0.17
org.slf4j:jul-to-slf4j:2.0.17
org.slf4j:log4j-over-slf4j:2.0.17
org.slf4j:slf4j-api:2.0.17
org.webjars:jquery:2.2.3
org.webjars:leaflet:1.5.1
org.webjars:momentjs:2.24.0
org.webjars.npm:csscolorparser:1.0.3
org.webjars.npm:earcut:2.2.4
org.webjars.npm:geojson-vt:3.2.1
org.webjars.npm:get-stream:6.0.1
org.webjars.npm:gl-matrix:4.0.0-beta.1
org.webjars.npm:grid-index:1.1.0
org.webjars.npm:ieee754:1.2.1
org.webjars.npm:js-tokens:4.0.0
org.webjars.npm:kdbush:3.0.0
org.webjars.npm:loose-envify:1.4.0
org.webjars.npm:mapbox__geojson-rewind:0.5.2
org.webjars.npm:mapbox__geojson-types:1.0.2
org.webjars.npm:mapbox-gl:1.13.0
org.webjars.npm:mapbox__jsonlint-lines-primitives:2.0.2
org.webjars.npm:mapbox__mapbox-gl-supported:1.5.0
org.webjars.npm:mapbox__point-geometry:0.1.0
org.webjars.npm:mapbox__tiny-sdf:1.1.1
org.webjars.npm:mapbox__unitbezier:0.0.0
org.webjars.npm:mapbox__vector-tile:1.3.1
org.webjars.npm:mapbox__whoots-js:3.1.0
org.webjars.npm:minimist:1.2.8
org.webjars.npm:murmurhash-js:1.0.0
org.webjars.npm:object-assign:4.1.1
org.webjars.npm:papaparse:5.2.0
org.webjars.npm:pbf:3.3.0
org.webjars.npm:potpack:1.0.2
org.webjars.npm:prop-types:15.8.1
org.webjars.npm:protocol-buffers-schema:3.6.0
org.webjars.npm:quickselect:2.0.0
org.webjars.npm:react:16.10.2
org.webjars.npm:react-dom:16.10.2
org.webjars.npm:react-is:17.0.0-rc.3
org.webjars.npm:resolve-protobuf-schema:2.1.0
org.webjars.npm:rw:1.3.3
org.webjars.npm:scheduler:0.16.2
org.webjars.npm:supercluster:7.1.5
org.webjars.npm:tinyqueue:2.0.3
org.webjars.npm:vt-pbf:3.1.3
org.webjars:vue:2.6.12
org.yaml:snakeyaml:2.4
>>> 172
```

#### RQ2 Experiments

#### Results for introspection

##### PDFBox

```
commons-io,commons-io,2.18.0
commons-logging,commons-logging,1.3.4
info.picocli,picocli,4.7.6
org.apache.pdfbox,fontbox,3.0.4
org.apache.pdfbox,pdfbox,3.0.4
org.apache.pdfbox,pdfbox-examples,3.0.4
org.apache.pdfbox,pdfbox-io,3.0.4
org.apache.pdfbox,pdfbox-tools,3.0.4
org.apache.pdfbox,preflight,3.0.4
org.apache.pdfbox,xmpbox,3.0.4
org.bouncycastle,bcpkix-jdk18on,1.80
org.bouncycastle,bcprov-jdk18on,1.80
org.bouncycastle,bcutil-jdk18on,1.80
```


##### MCS
```
com.fasterxml.jackson.core,jackson-core,2.18.2
com.fasterxml.jackson.jr,jackson-jr-objects,2.18.2
info.picocli,picocli,4.7.6
it.mulders,mcs,0.7.3
```

##### Certificate-ripper
```
info.picocli,picocli,4.7.6
io.github.hakky54,certificate-ripper,1.0.0
io.github.hakky54,sslcontext-kickstart,9.1.0
org.slf4j,slf4j-api,2.0.17
```

##### Checkstyle
```
com.google.code.findbugs,jsr305,3.0.2
com.google.guava,guava,33.4.6-jre
com.puppycrawl.tools,checkstyle,10.23.0
commons-beanutils,commons-beanutils,1.10.1
commons-collections,commons-collections,3.2.2
commons-logging,commons-logging,1.2
info.picocli,picocli,4.7.6
net.sf.saxon,Saxon-HE,12.5
org.antlr,antlr4-runtime,4.13.2
org.apache.ant,ant,1.10.15
org.apache.commons,commons-lang3,3.8.1
org.apache.commons,commons-text,1.3
org.apache.maven.doxia,doxia-core,1.12.0
org.apache.maven.doxia,doxia-logging-api,1.12.0
org.apache.maven.doxia,doxia-module-xdoc,1.12.0
org.apache.maven.doxia,doxia-sink-api,1.12.0
org.apache.xbean,xbean-reflect,3.7
org.codehaus.plexus,plexus-classworlds,2.6.0
org.codehaus.plexus,plexus-container-default,2.1.0
org.codehaus.plexus,plexus-utils,3.3.0
org.reflections,reflections,0.10.2
org.slf4j,slf4j-api,1.7.32
org.xmlresolver,xmlresolver,5.2.2
```

##### GraphHopper
```
ch.qos.logback,logback-classic,1.5.18
ch.qos.logback,logback-core,1.5.18
ch.qos.logback.access,logback-access-common,2.0.6
ch.qos.logback.access,logback-access-jetty11,2.0.6
com.carrotsearch,hppc,0.8.1
com.fasterxml,classmate,1.7.0
com.fasterxml.jackson.core,jackson-annotations,2.19.2
com.fasterxml.jackson.core,jackson-core,2.19.2
com.fasterxml.jackson.core,jackson-databind,2.19.2
com.fasterxml.jackson.dataformat,jackson-dataformat-xml,2.19.2
com.fasterxml.jackson.dataformat,jackson-dataformat-yaml,2.19.2
com.fasterxml.jackson.datatype,jackson-datatype-guava,2.19.2
com.fasterxml.jackson.datatype,jackson-datatype-jdk8,2.19.2
com.fasterxml.jackson.datatype,jackson-datatype-jsr310,2.19.2
com.fasterxml.jackson.jakarta.rs,jackson-jakarta-rs-base,2.19.2
com.fasterxml.jackson.jakarta.rs,jackson-jakarta-rs-json-provider,2.19.2
com.fasterxml.jackson.jakarta.rs,jackson-jakarta-rs-xml-provider,2.19.2
com.fasterxml.jackson.module,jackson-module-blackbird,2.19.2
com.fasterxml.jackson.module,jackson-module-jakarta-xmlbind-annotations,2.19.2
com.fasterxml.jackson.module,jackson-module-parameter-names,2.19.2
com.fasterxml.woodstox,woodstox-core,7.1.1
com.github.ben-manes.caffeine,caffeine,3.2.2
com.google.guava,guava,33.4.8-jre
com.google.protobuf,protobuf-java,3.12.2
com.graphhopper,directions-api-client-hc,11.0-SNAPSHOT
com.graphhopper,graphhopper-core,11.0-SNAPSHOT
com.graphhopper,graphhopper-example,11.0-SNAPSHOT
com.graphhopper,graphhopper-map-matching,11.0-SNAPSHOT
com.graphhopper,graphhopper-nav,11.0-SNAPSHOT
com.graphhopper,graphhopper-reader-gtfs,11.0-SNAPSHOT
com.graphhopper,graphhopper-web,11.0-SNAPSHOT
com.graphhopper,graphhopper-web-api,11.0-SNAPSHOT
com.graphhopper,graphhopper-web-bundle,11.0-SNAPSHOT
com.graphhopper.external,jackson-datatype-jts,2.19.2
com.squareup.okhttp3,okhttp,4.11.0
com.squareup.okio,okio-jvm,3.2.0
commons-io,commons-io,2.14.0
de.westnordost,osm-legal-default-speeds-jvm,1.4
group,artefact,version
io.dropwizard,dropwizard-assets,4.0.16
io.dropwizard,dropwizard-client,4.0.16
io.dropwizard,dropwizard-configuration,4.0.16
io.dropwizard,dropwizard-core,4.0.16
io.dropwizard,dropwizard-health,4.0.16
io.dropwizard,dropwizard-jackson,4.0.16
io.dropwizard,dropwizard-jersey,4.0.16
io.dropwizard,dropwizard-jetty,4.0.16
io.dropwizard,dropwizard-lifecycle,4.0.16
io.dropwizard,dropwizard-logging,4.0.16
io.dropwizard,dropwizard-metrics,4.0.16
io.dropwizard,dropwizard-request-logging,4.0.16
io.dropwizard,dropwizard-servlets,4.0.16
io.dropwizard,dropwizard-util,4.0.16
io.dropwizard,dropwizard-validation,4.0.16
io.dropwizard.metrics,metrics-annotation,4.2.34
io.dropwizard.metrics,metrics-core,4.2.34
io.dropwizard.metrics,metrics-healthchecks,4.2.34
io.dropwizard.metrics,metrics-jakarta-servlets,4.2.34
io.dropwizard.metrics,metrics-jersey3,4.2.34
io.dropwizard.metrics,metrics-jetty11,4.2.34
io.dropwizard.metrics,metrics-jvm,4.2.34
io.dropwizard.metrics,metrics-logback,4.2.34
jakarta.el,jakarta.el-api,4.0.0
jakarta.servlet,jakarta.servlet-api,5.0.0
jakarta.validation,jakarta.validation-api,3.0.2
jakarta.ws.rs,jakarta.ws.rs-api,3.0.0
net.sourceforge.argparse4j,argparse4j,0.9.0
net.sourceforge.javacsv,javacsv,2.0
org.apache.httpcomponents.client5,httpclient5,5.5
org.apache.httpcomponents.core5,httpcore5,5.3.4
org.apache.httpcomponents.core5,httpcore5-h2,5.3.4
org.codehaus.janino,commons-compiler,3.1.9
org.codehaus.janino,janino,3.1.9
org.codehaus.woodstox,stax2-api,4.2.2
org.eclipse.jetty,jetty-http,11.0.26
org.eclipse.jetty,jetty-io,11.0.26
org.eclipse.jetty,jetty-server,11.0.26
org.eclipse.jetty,jetty-servlet,11.0.26
org.eclipse.jetty,jetty-util,11.0.26
org.eclipse.jetty.toolchain.setuid,jetty-setuid-java,1.0.4
org.glassfish,jakarta.el,4.0.2
org.glassfish.hk2,hk2-api,3.0.6
org.glassfish.hk2,hk2-locator,3.0.6
org.glassfish.hk2,hk2-utils,3.0.6
org.glassfish.hk2,osgi-resource-locator,1.0.3
org.glassfish.jersey.containers,jersey-container-servlet,3.0.18
org.glassfish.jersey.containers,jersey-container-servlet-core,3.0.18
org.glassfish.jersey.core,jersey-client,3.0.18
org.glassfish.jersey.core,jersey-common,3.0.18
org.glassfish.jersey.core,jersey-server,3.0.18
org.glassfish.jersey.ext,jersey-bean-validation,3.0.18
org.glassfish.jersey.ext,jersey-metainf-services,3.0.18
org.glassfish.jersey.inject,jersey-hk2,3.0.18
org.hibernate.validator,hibernate-validator,7.0.5.Final
org.javassist,javassist,3.30.2-GA
org.jboss.logging,jboss-logging,3.6.1.Final
org.jetbrains.kotlin,kotlin-stdlib,1.6.20
org.locationtech.jts,jts-core,1.20.0
org.mapdb,mapdb,1.0.8
org.mobilitydata,gtfs-realtime-bindings,0.0.8
org.openstreetmap.osmosis,osmosis-osm-binary,0.48.3
org.slf4j,jul-to-slf4j,2.0.17
org.slf4j,slf4j-api,2.0.17
org.yaml,snakeyaml,2.4
>>> 104
```

##### Biojava
```
com.fasterxml.jackson.core,jackson-annotations,2.13.4
com.fasterxml.jackson.core,jackson-core,2.13.4
com.fasterxml.jackson.core,jackson-databind,2.13.4.2
com.google.guava,guava,31.1-jre
com.sun.istack,istack-commons-runtime,3.0.12
commons-lang,commons-lang,2.4
java3d,vecmath,1.3.1
javax.xml.bind,jaxb-api,2.3.1
org.apache.logging.log4j,log4j-api,2.17.2
org.apache.logging.log4j,log4j-core,2.17.2
org.apache.logging.log4j,log4j-slf4j-impl,2.17.2
org.biojava,biojava-alignment,7.0.2
org.biojava,biojava-core,7.0.2
org.biojava.thirdparty,forester,1.039
org.glassfish.jaxb,jaxb-runtime,2.3.5
org.glassfish.jaxb,txw2,2.3.5
org.jgrapht,jgrapht-core,1.4.0
org.msgpack,jackson-dataformat-msgpack,0.8.24
org.msgpack,msgpack-core,0.8.24
org.rcsb,ciftools-java,5.0.1
org.rcsb,mmtf-codec,1.0.11
org.rcsb,mmtf-serialization,1.0.11
org.slf4j,slf4j-api,1.7.30
```


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


