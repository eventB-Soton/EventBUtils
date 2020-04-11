#!/bin/sh

# Get the Rodin Licence
svn export https://github.com/eventB-Soton/RodinLicence/trunk/org.rodinp.licence org.rodinp.licence
# Use Maven to build
mvn clean verify
# Remove the Rodin licence
rm -r org.rodinp.licence
