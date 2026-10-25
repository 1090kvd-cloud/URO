#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build/tests
java com.sun.tools.javac.Main -d build/tests app/src/main/java/com/kvd/bubblelevel/LevelModel.java app/src/main/java/com/kvd/bubblelevel/BubbleGeometry.java app/src/main/java/com/kvd/bubblelevel/SignalMode.java tests/LevelModelTest.java tests/SignalModeTest.java
java -cp build/tests com.kvd.bubblelevel.LevelModelTest
java -cp build/tests com.kvd.bubblelevel.SignalModeTest
