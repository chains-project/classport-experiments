# Experiments 
This forder cantains the scripts to run the experiments.

## RQ1
*"To what extent can Classport embed dependencies
into the classfiles?"*

The experiments should test:
* the impact that the added annotations have on the correct working of the application. 

Clean and run the tests on the project **before** embedding.

`mvn clean test -fn`

Clean and run the tests on the project **after** embedding.

`mvn io.github.chains-project:classport-maven-plugin:0.1.0-SNAPSHOT:embed`

`mvn clean test -fn`

* completeness: it shows which dependency is embedded in the jar of the target program and it reports the not annotated classes. 

**This should be run after the embedding and the creation of the uberjar** (these commands also gives you the space overhead).

For multimodule:

`./embed_with_copy.sh <project_directory> <program> <executable-module>`

For non multimodule:

`./embed.sh <project_directory>`

Then:

`./check_if_deps_embedded.sh <program_name>`
* correctness: it compare the list of the embedded dependencies (GAV) with the list of GAVs retrieved running mvn dependency:list

**Same as the compelteness**

`./check_if_deps_embedded.sh <program_name> --correctness`
* Time overhead during build: it is compared the time required to do compile and generate-resources phase wrt run the embedding plugin.

`./compute_build_overhead.sh <program_name>`
* Space overhead after embedding (plugin + uberjar creation)

`./embed_with_copy.sh <project_directory> <program> <executable-module>`

ex.

`./embed_with_copy.sh pdfbox-3.0.4 pdfbox app`

## RQ2 
*"To what extent can Classport inspect runtime dependencies"*

## RQ3
*"Can Classport mitigate the dependency confusion
attack?"*