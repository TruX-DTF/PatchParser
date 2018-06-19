#!/bin/bash

cd gumtree
mvn compile
mvn install -Dmaven.test.skip=true

cd ../Parser
mvn compile
mvn dependency:copy-dependencies
mvn package
mv -f target/Parser-0.0.1-SNAPSHOT.jar target/dependency/

java -cp "target/dependency/*" -Xmx2g edu.lu.uni.serval.Main
java -cp "target/dependency/*" -Xmx2g edu.lu.uni.serval.Main2

