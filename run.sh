#!/bin/bash

cd gumtree
mvn compile
mvn install -Dmaven.test.skip=true

cd ../Parser
mvn compile
mvn dependency:copy-dependencies
mv -f target/Parser-0.0.1-SNAPSHOT.jar target/dependency/
cd ..

java -cp "Parser/target/dependency/*" -Xmx1024g edu.lu.uni.serval.Main
java -cp "Parser/target/dependency/*" -Xmx1024g edu.lu.uni.serval.Main2

