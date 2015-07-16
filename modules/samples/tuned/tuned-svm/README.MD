WSO2 Machine Learner - Sample Model Generation Flow
===================================================

Algorithm: SVM
==============

Tuned hyper-parameter values
======================

Iterations : 100
Learning_Rate : 0.01
Reg_Parameter : 0.001
Reg_Type : L1
SGD_Data_Fraction : 1

Before Running
==============

* Start WSO2 Machine Learner product.
* Make sure jq is installed.

How to Run?
===========

* Run the model-generation.sh script file.

Outcome
=======

* Running the above script will result in a generation of SVM model for the provided data-set.

* Model will be stored in the file system at {ML_HOME}/models directory.
