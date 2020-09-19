#!/bin/sh

## REM
classpath=out\
:/lib/scala-library.jar\
:/lib/scala-reflect.jar\
:/lib/zio_2.13-1.0.1.jar\
:/lib/scala-library-2.13.1.jar\
:/lib/zio-stacktracer_2.13-1.0.1.jar\
:/lib/izumi-reflect_2.13-1.0.0-M5.jar\
:/lib/izumi-reflect-thirdparty-boopickle-shaded_2.13-1.0.0-M5.jar
command="mkdir out
scalac -classpath $classpath -d out Main.scala
scala -classpath out:$classpath Main"
echo "$command"
## /REM

cd '/usercode'
sh -c "$command"
exit 0