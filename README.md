# Experiments

This repo contains the experiments on the [Classport](https://github.com/chains-project/classport) tool.

## Projects

The table contains the list of the projects used in the experiments, with the description of the modification done to be executed, the workload, and the version.

| Project Name | Description of Modification | Workload | Version |
|--------------|-----------------------------|----------|---------|
| Pdfbox    | -          | Extract text from a pdf     | 3.0.4     |
| Graphhopper    | change version from SNAPSHOT to 9     | Compute a path with an intermediate node   | 9.1     |
| certificate-ripper    | -          | Get the certificate of the chains website    | 2.4.1     |
| checkstyle    | -          | Use /sun_checks.xml on the agent module    | 10.23.0     |



## How to reproduce

The [scripts](/classport-experiments/scripts/) folder contains the scripts necessary to run the experiments.

* embed
* run the agent

### Requirements
To run the experiments, it is required to use this repository inside the [Classport repository](https://github.com/chains-project/classport).
