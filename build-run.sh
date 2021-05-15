#!/bin/bash
# Build via maven and shade and run via java

mvn clean compile package -q
if [[ $? -eq 0 ]] ;
  then
    echo "Running program..."
    java -classpath target/KMeans-1.0-SNAPSHOT.jar io.javasmithy.App $1 $2 $3 $4
  else
    echo $?
    echo "Build Failed, no run."
fi