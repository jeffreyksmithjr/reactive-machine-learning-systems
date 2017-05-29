#!/bin/sh

cd $PROJECT_HOME

git pull

sbt compile

sbt test

sbt assembly

rsync -a $PROJECT_HOME/target/scala-2.11/fruit-picker-assembly-1.0.jar \
    username@artifact_server:~/jars/fruit-picker/$VERSION_NUMBER
