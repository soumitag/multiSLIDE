#!/bin/bash

/usr/bin/mongod &

cd /multislide/db
unzip '*.zip'
ls -l

allfilenames=`ls ./*.json`

for eachfile in $allfilenames;
do
   filename=$(basename "$eachfile")
   if grep -q "HS_" <<< "$filename"; then
  		mongoimport --db geneVocab_HomoSapiens --collection ${i/.json/} --file $i
   fi
   if grep -q "MM_" <<< "$filename"; then
  		mongoimport --db geneVocab_MusMusculus --collection ${i/.json/} --file $i
   fi

done
