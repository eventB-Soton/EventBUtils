Event-B Utilities Package
=========================
This package provides additional functionalities to manipulate Event-B
components. There are different bundles for the package can be
installed via the update site (RODIN sourceforge update site).

1. *ch.ethz.eventb.utils\_feature\_x.x.x.x.jar* The basic feature
   containing the binary build of the package.

2. *ch.ethz.eventb.utils.tests.feature\_x.x.x.x.jar* The tests feature.

3. *ch.ethz.eventb.utils.sdk\_x.x.x.x.jar* The SDK feature containing
   the source code and tests used for development.

The complete source code of the package can be downloaded as a zip
file *ch.ethz.eventb.utils\_x.x.x.zip*

Release history
=============

### Version 0.2.4 - Implement method to get invariants ###
- add method getSCInvariants() to EventBSCUtils to get the invariants
 from statically checked machine.
- Create a new Tests feature
- Change the SDK feature to generate source features.

### Version 0.2.3 - Implement method to get variables' type ###
- add method getVariableType(…) to EventBSCUtils to get the type of a
  variable from the statically checked machine

### Version 0.2.2 - Utilities related to Event-B UI ###
- Added utilitities related to Event-B UI: Event-B content provider,
  Event-B label provider, Event-B viewer filter.

### Version 0.2.1 - Publish the SDK feature ###
- Published the SDK feature of the package.
- Changed the structure of the tests plugin.
