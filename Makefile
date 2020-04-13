# Get the Rodin Licence
rodin_licence = ../RodinLicence
$(rodin_licence):
	@echo "Rodin Licence does not exist, git clone required"
	git clone "https://github.com/eventB-Soton/RodinLicence.git" $(rodin_licence)

build: | $(rodin_licence)
# Use Maven to build
	mvn clean verify

clean: | $(rodin_licence)
# Use Maven to build
	mvn clean


