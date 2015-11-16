#!/bin/bash

echo "testing Linear Regression workflow"

# Die on any error:
set -e

DIR="${BASH_SOURCE%/*}"; if [ ! -d "$DIR" ]; then DIR="$PWD"; fi; . "$DIR/../../base.sh"

echo "#create a dataset"
path=$(pwd)
curl -X POST -b cookies  https://localhost:9443/api/datasets -H "Authorization: Basic YWRtaW46YWRtaW4=" -H "Content-Type: multipart/form-data" -F datasetName='abalone-linear-regression-dataset' -F version='1.0.0' -F description='Abalone Dataset' -F sourceType='file' -F destination='file' -F dataFormat='CSV' -F containsHeader='true' -F file=@'/'$path'/abalone.csv' -k
sleep 5

# creating a project
echo "#creating a project"
curl -X POST -d @'create-project' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/projects -k
sleep 2

#getting the project
echo "#getting the project"
project=$(curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/projects/wso2-ml-linear-regression-sample-project -k)
sleep 2

#update the json file with retrieved values
projectId=$(echo "$project"|jq '.id')
datasetId=$(echo "$project"|jq '.datasetId')
${SED} -i 's/^\("projectId":"\)[^"]*/\1'$projectId/ create-analysis;
sleep 2

#creating an analysis
echo "creating an analysis"
curl -X POST -d @'create-analysis' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses -k
sleep 2

#getting analysis id
echo "getting analysis id"
analysis=$(curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/projects/${projectId}/analyses/wso2-ml-linear-regression-sample-analysis -k)
sleep 2

analysisId=$(echo "$analysis"|jq '.id')

#setting model configs
echo "#setting model configs"
curl -X POST -d @'create-model-config' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/${analysisId}/configurations -k -v
sleep 2

echo "#adding default features with customized options"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/${analysisId}/features/defaults -k -v -d @'customized-features'
sleep 2

echo "#setting default hyper params"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/analyses/${analysisId}/hyperParams/defaults -k -v
sleep 2

echo "#getting dataset version"
datasetVersions=$(curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/datasets/${datasetId}/versions -k)
sleep 2

#update the json file
datasetVersionId=$(echo "$datasetVersions"|jq '.[0] .id')
${SED} -i 's/^\("analysisId":"\)[^"]*/\1'$analysisId/ create-model;
sleep 2
${SED} -i 's/^\("versionSetId":"\)[^"]*/\1'$datasetVersionId/ create-model;
sleep 2

echo "#create model"
model=$(curl -X POST -d @'create-model' -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models -k)
sleep 2

echo "#getting model"
modelName=$(echo "$model"|jq -r '.name')
model=$(curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/${modelName} -k)
sleep 2
modelId=$(echo "$model"|jq '.id')

echo "#building the model"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/${modelId} -k -v
sleep 30

echo "#predict using model"
curl -X POST -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/${modelId}/predict -k -v -d @'prediction-test'

echo "#exporting model to pmml"
curl -H "Content-Type: application/json" -H "Authorization: Basic YWRtaW46YWRtaW4=" -v https://localhost:9443/api/models/${modelId}/export?mode=pmml -k


