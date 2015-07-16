WSO2 Machine Learner - Sample Model Generation Flow
===================================================

Algorithm: Random Forest
========================

Tuned hyper-parameter values
============================

Num_Trees : 5
Max_Depth : 5
Max_Bins : 100
Impurity : gini
Feature_Subset_Strategy : auto
Seed :12345

Before Running
==============

* Start WSO2 Machine Learner product.
* Make sure jq is installed.

How to Run?
===========

* Run the model-generation.sh script file.

Outcome
=======

* Running the above script will result in a generation of random forest model for the provided data-set.

* Model will be stored in the file system at {ML_HOME}/models directory.
