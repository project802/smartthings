# Ubiquiti UniFi Protect for Samsung SmartThings
This is a SmartApp and device handler that will discover cameras from your UniFi Protect NVR and create child devices for each camera.

Current capabilities of the camera device handler:
  * Implements motion sensor (if the camera is set to record on motion)
  * Implements image capture for single image display within details screen
  * Displays connection state in details screen 
  * Configurable to take a snapshot when motion is detected
  
  
## Installation
1. Log into the IDE (http://graph.api.smartthings.com) and create a SmartApp and Device Handler from the code in this respository.  For a tutorial on the IDE, see http://docs.smartthings.com/en/latest/tools-and-ide/editor-and-simulator.html 
2.  Publish the SmartApp and device handler to yourself (http://docs.smartthings.com/en/latest/publishing/index.html)
3.  Install the UniFi Protect SmartApp from your mobile device by going to "Automations", "Add a SmartApp" and finally "My Apps", where it should be visible.
  * Enter your Protect IP address.
  * Keep port to 7080 (this is the default HTTP port.  Do not use 7443.).
  * Enter the username and password for the local user. Note: the Ubiquiti SSO credentials will not work!
  * Tap "Done"
4.  Camera devices should start to appear in your device list ("My Home" -> "Things")

## Application Notes
The Protect software will use port 7080 for HTTP transactions out of the box.  Please ensure that your hub and NVR are on a trusted network.  Do not use port 7443 as SmartThings does not currently support HTTPS for hub actions on the LAN.

The API for Protect is not published by Ubiquiti so this integration is subject to break, however unlikely.  

Your SmartThings hub running this SmartApp must be on the same subnet as the NVR software.  This is a restriction of the hub that it will not perform HTTP transactions outside the subnet.

## Known Compatibility
Cloud Key v2 Controller 1.13.2

## Known Issues
For a list of known issues, please see the [issues page](https://github.com/project802/smartthings/issues "GitHub issues page")
