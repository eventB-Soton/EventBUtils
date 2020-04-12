build: --rodin-licence
# Use Maven to build
	mvn clean verify
# Remove the Rodin licence
	rm -r org.rodinp.licence

clean: --rodin-licence
# Use Maven to build
	mvn clean
# Remove the Rodin licence
	rm -r org.rodinp.licence

# Get the Rodin Licence
.PHONY --rodin-licence:
	rm -rf org.rodinp.licence
	svn export https://github.com/eventB-Soton/RodinLicence/trunk/org.rodinp.licence org.rodinp.licence

