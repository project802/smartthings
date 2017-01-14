# Ubiquiti UniFi Video NVR for Samsung SmartThings
This is a SmartApp and device handler that will discover cameras from your NVR and create child devices for each camera.

Current capabilities of the camera device handler:
  * Implements motion sensor (if the camera is set to record on motion)
  * Implements image capture for single image display within details screen
  * Displays connection state in details screen 
  
  
## Installation
1. Log into the IDE (http://graph.api.smartthings.com) and create a SmartApp and Device Handler from the code in this respository.  For a tutorial on the IDE, see http://docs.smartthings.com/en/latest/tools-and-ide/editor-and-simulator.html 
2.  Publish the SmartApp and device handler to yourself (http://docs.smartthings.com/en/latest/publishing/index.html)
3.  To use the app, you must have an active API key from a user with proper permissions.  
  * In the NVR software click on "Users", select a user (probably your super administrator) then select "API Access".
  * Make sure that "Allow API Usage" is enabled.
  * Generate an API key (if there isn't one already) and note it down.
4.  Install the UniFi NVR SmartApp from your mobile device by going to "Automations", "Add a SmartApp" and finally "My Apps", where it should be visible.
  * Enter your NVR IP address.
  * Keep port to 7080 (this is the default HTTP port.  Do not use 7443.).
  * Enter the API key from step 3.
  * Tap "Done"
5.  Camera devices should start to appear in your device list ("My Home" -> "Things")

## Application Notes
The NVR software will use port 7080 for HTTP transactions out of the box.  Please ensure that your hub and NVR are on a trusted network.  Do not use port 7443 as SmartThings does not currently support HTTPS for hub actions on the LAN.

The API for the NVR is not published by Ubiquiti so this integration is subject to break, however unlikely.  

Your SmartThings hub running this SmartApp must be on the same subnet as the NVR software.  This is a restriction of the hub that it will not perform HTTP GET commands outside the subnet.

## Compatibility
I have tested this against NVR v3.5.2 running on a hardware NVR and a UVC G3 Dome.
