#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/tests
java com.sun.tools.javac.Main -d build/tests app/src/main/java/com/kvd/bubblelevel/LevelModel.java app/src/main/java/com/kvd/bubblelevel/BubbleGeometry.java tests/LevelModelTest.java
java -cp build/tests com.kvd.bubblelevel.LevelModelTest
