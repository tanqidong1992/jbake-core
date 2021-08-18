#!/bin/bash
cd target
cp jbake-core jbake-core-tmp -R
cd jbake-core-tmp
cp ../../src/main/resources/package/* .
mv run-foreground.sh AppRun
chmod a+x AppRun
../../tools/appimagetool-x86_64.AppImage . jbake
mv jbake ../
cd ..
rm  jbake-core-tmp -rf
