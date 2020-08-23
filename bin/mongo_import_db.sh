#!/bin/bash

cd $1
unzip -o '*.zip'
ls -l

allfilenames=`ls ./*.json`

for eachfile in $allfilenames;
do
   filename=$(basename "$eachfile")
   dbname=$(basename -s .json "$eachfile") 
   echo $filename, $dbname
   if grep -q "HS_" <<< "$filename"; then
  		mongoimport --db geneVocab_HomoSapiens --drop --collection $dbname --file $filename
   fi
   if grep -q "MM_" <<< "$filename"; then
  		mongoimport --db geneVocab_MusMusculus --drop --collection $dbname --file $filename
   fi

done
