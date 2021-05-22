# smartthings

This is a collection of device handlers, SmartApps and other things relating to the Samsung SmartThings platform.  They can be installed using the Git Hub integration or manually.  

For Git Hub integration, see https://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html

## UniFi Protect (UNMAINTAINED)
SmartApp and Device Type Handler for Ubiquiti UniFi Protect software/cameras.  See UniFi_Protect.md for more information.

## UniFi NVR (UNMAINTAINED)
SmartApp and Device Type Handler for Ubiquiti UniFi NVR software/cameras.  See Unifi_NVR.md for more information.  Now deprecated since Ubiquiti has slowed NVR development to focus on Protect.

## SmartStreams
SmartApp for collecting events and sending them to a single API endpoint.  Useful for doing your own data collection for more advanced processing or use it as a jumping out point.

## Dark Sky Weather (DEPRECATED)
Device Type Handler for bringing Dark Sky weather data into SmartThings for temperature, humidity, illuminance estimation and water (if there is precipitation).  Now deprecated since Apple has acquired Dark Sky and the API will terminate at the end of 2021.

## NUT Client
Device Type Handler for a NUT server, by means of a proxy.  Exposes events for battery, power, power source and line voltage.
