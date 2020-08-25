#!/bin/sh


############## ENVIRONMENT VARIABLES#############
##MULTISLIDE_GLASSFISH_HOME=/opt/glassfish5/glassfish
##DOMAIN_NAME=multislide
##MULTISLIDE_PYTHON_HOME=/usr/bin/python
##GLASSFISH_ARCHIVE=glassfish5
##HOSTNAME=bbb10bc13aff
##MULTISLIDE_JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
##HOME=/root
##MULTISLIDE_HOME=/usr/local/multiSLIDE
##PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
##MULTISLIDE_MONGODB_DATA=/usr/local/multiSLIDE/mongo_data/db
##INSTALL_DIR=/opt
##PWD=/usr/local
##MULTISLIDE_MONGODB_HOME=/usr/bin/mongod


printenv MULTISLIDE_HOME
printenv MULTISLIDE_MONGODB_HOME
printenv MULTISLIDE_GLASSFISH_HOME

#install_dir = ${MULTISLIDE_HOME}
#mongodb_dir = ${MULTISLIDE_MONGODB_HOME}
#glassfish_dir = ${MULTISLIDE_GLASSFISH_HOME}

echo "printing to check"
echo ${MULTISLIDE_HOME}
echo ${MULTISLIDE_MONGODB_HOME}
echo ${MULTISLIDE_GLASSFISH_HOME}

# ****** AUTOGENERATED CODE *******
#export install_dir="/Users/abhikdatta/Desktop/26 Sep 2017/slide"
#export mongodb_dir="/usr/local/Cellar/mongodb/3.4.9/bin"
#export glassfish_dir="/Users/abhikdatta/glassfish4/glassfish/bin"
# *********************************

#$mongodb_dir/mongod&
${MULTISLIDE_MONGODB_HOME}&

#"$glassfish_dir"/asadmin start-domain
${MULTISLIDE_GLASSFISH_HOME}/bin/asadmin start-domain
#"$glassfish_dir"/asadmin deploy --force=true "$install_dir"/lib/VTBox.war
${MULTISLIDE_GLASSFISH_HOME}/bin/asadmin deploy --force=true ${MULTISLIDE_HOME}/lib/msviz-engine.war
${MULTISLIDE_GLASSFISH_HOME}/bin/asadmin deploy --force=true ${MULTISLIDE_HOME}/lib/multislide.war
