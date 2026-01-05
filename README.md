# TreeKernel-JSS2025

This repository contains the **replication package** for the paper:

> **An Empirical Evaluation of Tree Kernels for Source Code Retrieval**, to appear in *JSS*.

The instructions below describe how to reproduce all experiments and results reported in the paper.
---

## Repository Structure

- `data/` – Datasets used in the experiments and derived artifacts (such as parsed code).
- `code/` – Source code for all experiments.
- `results/` – Outputs and evaluation metrics.

---
## Getting Started
### Step 1: Clone the Repository

```bash
git clone https://github.com/your-username/treekernel-jss2025.git
cd treekernel-jss2025
```

---


## Dataset (*data/` directory)

This folder includes the **BigCloneBench** dataset and derived artifacts:

- `bcb_reduced/` – Java source files in the benchmark.
- `function_strings/` – Extracted methods/code fragments, parsed into ASTs using the [GumTree library](https://github.com/GumTreeDiff/gumtree).
- `sexpr_from_gumtree/` – ASTs in S-expression format for tree kernel consumption.
- `TestH2Database/` – Ground truth clone pairs stored in `bigclonedb_clones_alldir_8584153.txt`. Alternatively you can  download the ground truth file [here](https://drive.google.com/file/d/15N9kWtV4TMe-uxXcH0doi1Bbn3vbllbX/view?usp=sharing).
- `checkstyle_complexity_all.csv` – McCabe's Cyclomatic Complexity scores, computed using [Checkstyle](https://checkstyle.sourceforge.io/checks/metrics/cyclomaticcomplexity.html).

---

## Experimental Code (`code/` directory)

All experimental code is organized by **research question (RQ)**:

- **RQ1:** Evaluation and comparison of three tree kernels (PTK, STK, SSTK) against TF-IDF and CodeBERT baseline.
- **RQ2:** Performance across different clone types (Type-1, Type-2, and Type-3).
- **RQ3:** Performance across code fragments of varying size (LOC) and Cyclomatic Complexity.
- **RQ4:** Hybrid retrieval model combining TF-IDF and tree kernels to reduce the runtime.

---

## Requirements

### 1. Set Up Elasticsearch (Required for TF-IDF and RQ4)

Elasticsearch is required for:
- TF-IDF baseline experiments
- Hybrid retrieval model (RQ4)

- Download and install Elasticsearch from [elastic.co](https://www.elastic.co/downloads/elasticsearch).
- Start the server:

```bash
cd elasticsearch-x.x.x/bin
./elasticsearch
```

Ensure it is running before starting the relevant experiments and and remains running until completion.

### 2. Indexing Code Fragments into Elasticsearch
- Index the code fragments into Elasticsearch before running TF-IDF baseline experiments, and RQ4 hybrid model.

```bash
cd code/rq4/hybrid/src/main/java/sample/evaluation/elasticsearch
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.elasticsearch.ESFileIndexer"
```

---

## Running the Experiments

### RQ1: Tree Kernel Comparison

```bash
cd code/rq1/kelp-full
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.kelp.TKBigCloneEval"
```

---

### RQ2: Syntactic Type Analysis

```bash
cd code/rq2/kelp-full
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.kelp.TKBigCloneEval"
```
---

### RQ3: Code Complexity and Size based Evaluation

```bash
cd code/rq3/kelp-full
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.kelp.TKBigCloneEval"
```

---

### RQ4: Hybrid Model Evaluation
*(Requires Elasticsearch)*

```bash
cd code/rq4/hybrid
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.eskelp.ElasticsearchKelp"
```

---


## Baselines
### TF-IDF Baseline  
*(Requires Elasticsearch)*

```bash
cd code/{rq}/tfidf/es-code
mvn clean compile
mvn exec:java -Dexec.mainClass="sample.evaluation.elasticsearch.ElasticsearchBCEval"
```
### CodeBERT Baseline
*(Requires CodeBERT dependencies such as torch, and transformers)*
```bash
cd code/codebert
./rq1.sh
./rq2.sh
./rq3.sh
```

## Results (`results/` directory)

This directory contains all experimental outputs, including the performance metrics Precision, MRR, MAP, and runtime measurements

---

## Contact

For questions or feedback, contact the authors via the corresponding email provided in the paper.
