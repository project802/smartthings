/**
 *  Aeon HEM1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Aeon Home Energy Meter gen-1 (US)
 *
 *  Updates:
 *  -------
 *  02-15-2016 : Removed posting to the Activity Feed in the phone app and event log.
 *  02-17-2016 : Fixed preferences for kWh cost from string to number.
 *  02-20-2016 : Enabled battery reporting (parameter 103, value 1), and documented the parameters better.
 *  02-21-2016 : Made certain configuration parameters changeable via device preferences instead of having to tweak code all the time.
 *  02-22-2016 : Fixed kWh cost entry in Preferences not allowing decimals.
 *  02-27-2016 : Changed date formats to be MM-dd-yyyy h:mm a.
 *  02-29-2016 : Changed reportType variable from 0 to 1.
 *  03-11-2016 : Due to ST's v2.1.0 app totally hosing up SECONDARY_CONTROL, implemented a workaround to display that info in a separate tile.
 *  03-19-2016 : Added clarity for preferences.
 *  03-21-2016 : Fixed issue when resetting energy would also reset watts.
 *  03-25-2016 : Removed the \n from the two tiles for resetting watts and energy due to rendering issues on iOS
 *  07-07-2016 : Check for wildly large watts value coming from the HEM and do not process them.  Firmware updates should have resolved this.
 *  08-10-2016 : Check for 0 or negative watts value coming from the HEM and do not process them.  Firmware updates should have resolved this.
 *  08-21-2016 : Created separate tiles to reset min and max instead of having a single tile for both values.  Changed many tiles to different sizes.
 *  08-27-2016 : Modified the device handler for my liking, primarly for looks and feel.
 *  09-16-2016 : During the check for 0 or negative values, use the last power value (state.powerValue) instead of just a hard coded value.
 *  10-17-2016 : Cleaned up code.
 *  10-19-2016 : Provided comments in the code for iOS users to edit so that the rendering of text for certain tiles to work right.  Changed default icon.
 *  10-19-2016 : Added a new parameter in Preferences so that a user can specify the high limit for a watts value instead of hard coding a value.  Related to the change on 7-7-2016.
 *  11-22-2016 : Added resetMeter section that calls the other resets (min, max, energy/cost).  This is for a SmartApp that resets the meter automatically at the 1st day of month.
 *  01-08-2017 : Added parameter 12 and set it to 1.  Accumulates kWh energy when Battery Powered.
 *  01-08-2017 : Cleaned up code in the resetMeter section.
 *  01-08-2017 : Added code for Health Check capabilities/functions, and cleaned up code in the resetMeter section.
 *  01-18-2017 : Removed code no longer needed, and added another parameter in Preference to enable or disable the display of values in the Recently tab and device's event log (not Live Logs).  Enabling may be required for some SmartApps.
 *  01-20-2017 : Removed the check for 0w, but still don't allow negative values.  Also removed all rounding, which now displays 3 positions right of the decimal.
 *  02-11-2017 : Removed commands no longer needed.  Documented what each attribute is used for.  Put battery info into the main tile instead of a separate tile.
 *  02-12-2017 : Combined the battery and no-battery version into a single DTH, cleaned up code, and general improvements.
 *  02-13-2017 : Cleaned up code for battery message being displayed. If someone decides to display battery % while not having batteries installed Health Check will catch that and push low battery notifications until the user disables the display.
 *  03-11-2017 : Changed from valueTile to standardTile for a few tiles since ST's mobile app v2.3.x changed something between the two.
 *  03-26-2017 : Added a new device Preference that allows for selecting how many decimal positions should be used to display for WATTS and kWh.  Min/max values still use 3 positions, as well as what's stored for the actual meter reading that's seen in the IDE for Power and what's sent to SmartApps.
 *  03-29-2017 : Made changes to account for ST v2.3.1 bugs with text rendering.
 *  
 */

import java.text.DecimalFormat

metadata {
    definition (name: "My Aeon Home Energy Monitor Gen1", namespace: "jscgs350", author: "jscgs350") 
{
    capability "Energy Meter"
    capability "Power Meter"
    capability "Configuration"
    capability "Sensor"
    capability "Refresh"
    capability "Polling"
    capability "Battery"
    capability "Power Source"
    
    attribute "currentKWH", "string" 		// Used to show current kWh since last reset
    attribute "currentWATTS", "string"  	// Used to show current watts being used on the main tile
    attribute "minWATTS", "string"   		// Used to store/display minimum watts used since last reset
    attribute "maxWATTS", "string"   		// Used to store/display maximum watts used since last reset
    attribute "resetMessage", "string"  	// Used for messages of what was reset (min, max, energy, or all values)
    attribute "kwhCosts", "string"  		// Used to show energy costs since last reset
    attribute "powerStatus", "string"       // Used to indicate USB or battery power status

    command "resetkwh"
    command "resetmin"
    command "resetmax"
    command "resetMeter"
    
    fingerprint deviceId: "0x2101", inClusters: " 0x70,0x31,0x72,0x86,0x32,0x80,0x85,0x60"

}
// tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"currentWATTS", type: "generic", width: 6, height: 4, decoration: "flat"){
			tileAttribute ("device.currentWATTS", key: "PRIMARY_CONTROL") {
				attributeState "default", action: "refresh", label: '${currentValue}W', icon: "https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/device-activity-tile@2x.png", backgroundColor: "#79b821"
			}
            tileAttribute ("device.powerStatus", key: "SECONDARY_CONTROL") {
           		attributeState "powerStatus", label:'${currentValue}', icon:"https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/Battery-Charge-icon.png"
            }
		}    
        standardTile("iconTile", "iconTile", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", icon:"https://raw.githubusercontent.com/constjs/jcdevhandlers/master/img/device-activity-tile@2x.png"
		}
        valueTile("statusText", "statusText", inactiveLabel: false, decoration: "flat", width: 5, height: 1) {
			state "statusText", label:'${currentValue}', backgroundColor:"#ffffff"
		}        
        valueTile("resetMessage", "device.resetMessage", width: 5, height: 1, inactiveLabel: false, decoration: "flat") {
            state("default", label: '${currentValue}', backgroundColor:"#ffffff")
        }
        valueTile("currentKWH", "device.currentKWH", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state("default", label: '${currentValue}kWh', backgroundColor:"#ffffff")
        }
        valueTile("kwhCosts", "device.kwhCosts", width: 3, height: 1, inactiveLabel: false, decoration: "flat") {
            state("default", label: 'Cost ${currentValue}', backgroundColor:"#ffffff")
        }
        standardTile("resetmin", "device.resetmin", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:'Reset Min', action:"resetmin", icon:"st.secondary.refresh-icon"
        }
        standardTile("resetmax", "device.resetmax", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:'Reset Max', action:"resetmax", icon:"st.secondary.refresh-icon"
        }        
        standardTile("resetkwh", "device.resetkwh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Reset Energy', action:"resetkwh", icon:"st.secondary.refresh-icon"
		}
    	standardTile("refresh", "device.refresh", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "default", label:'Refresh', action:"refresh", icon:"st.secondary.refresh-icon"
    	}
    	standardTile("configure", "device.configure", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "configure", label:'', action:"configure", icon:"st.secondary.configure"
    	}

        main (["currentWATTS"])
        details(["currentWATTS", "iconTile", "statusText", "iconTile", "resetMessage", "currentKWH", "kwhCosts", "resetmin", "resetmax", "resetkwh", "refresh", "configure"])
        }

        preferences {
        	input "displayEvents", "bool", title: "Display all events in the Recently tab and the device's event log?", defaultValue: false, required: true, displayDuringSetup: true
        	input "displayPowerStatus", "bool", title: "Display power status on main tile?", defaultValue: true, required: true, displayDuringSetup: true
            input "kWhCost", "decimal", title : "Energy Cost (\$/kWh)", description : "Enter the cost of your energy per kWh (or 0 to disable):", defaultValue: 0.16, required: true, displayDuringSetup: true
            input "wattsLimit", "number", title : "Erroneous Power Reporting Limit (W)", description : "Ignore power reports above this limit.", defaultValue: 20000, required: true, displayDuringSetup: true                
            input "reportOnLoadChange", "bool", title : "Report Using Load Change?", description : "If on, sensor reports when load changes.  If off, sensor reports at fixed time intervals.", defaultValue: true, required: true, displayDuringSetup: true
            input "wattsChanged", "number", title : "Load Reporting: Power Delta (W)", description : "Send when load adjusts by this many watts", defaultValue: 50, range: "0..60000", required: true, displayDuringSetup: true
            input "wattsPercent", "number", title : "Load Reporting: Percentage Delta (%)", description : "Send when load adjusts by this many percent", defaultValue: 10, range: "0..100", required: true, displayDuringSetup: true
            input "secondsWatts", "number", title : "Time Reporting: Power Interval (s)", description : "Send power data every X seconds", defaultValue: 15, range: "0..65000", required: true, displayDuringSetup: true
            input "secondsKwh", "number", title : "Time Reporting: Energy Interval (s)", description : "Send energy data every X seconds", defaultValue: 60, range: "0..65000", required: true, displayDuringSetup: true
            input "secondsBattery", "number", title : "Battery Status Interval (s)", description : "Send battery data every X seconds", defaultValue: 900, range: "0..65000", required: true, displayDuringSetup: true
            input "decimalPositions", "number", title : "Power & Energy Reporting Decimal Precision", description : "Decimal positions for watts and kWh", defaultValue: 3, range: "0..3", required: true, displayDuringSetup: true
        }
}

def updated() {
    log.debug "updated"
	
    // Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    
    log.debug "updated (kWhCost: ${kWhCost}, wattsLimit: ${wattsLimit}, reportOnLoadChange: ${reportOnLoadChange}, wattsChanged: ${wattsChanged}, wattsPercent: ${wattsPercent}, secondsWatts: ${secondsWatts}, secondsKwh: ${secondsKwh}, secondsBattery: ${secondsBattery}, decimalPositions: ${decimalPositions})"
    
    response(configure())
}

def parse(String description) {
    //log.debug "Parse received ${description}"
    def result = []
    def cmd = zwave.parse( description, [0x31: 1, 0x32: 1, 0x60: 3, 0x80: 1] )
    //log.debug "Parse returned ${cmd}"
    
    if( cmd )
    {
        result = zwaveEvent( cmd )
    }
    
    def statusTextmsg = "Min was ${device.currentState('minWATTS')?.value}.\nMax was ${device.currentState('maxWATTS')?.value}."
    result << createEvent( "name": "statusText", "value": statusTextmsg, displayed : false )
    
    if( displayPowerStatus )
    {
        def powerSource = device.currentState('powerSource')?.value
        def battery = device.currentState('battery')?.value
        
        battery = battery ? battery : "unknown"
        
        if( powerSource == "battery" )
        {
            result << createEvent( name : "powerStatus", value : "Battery ${battery}%", displayed : false )
        }
        else if( powerSource == "mains" )
        {
            result << createEvent( name : "powerStatus", value : "USB Power, battery ${battery}%", displayed : false )
        }
        else
        {
            result << createEvent( name : "powerStatus", value : "Power state unknown, battery ${battery}%", displayed : false )
        }
    }
    else
    {
        result << createEvent( name : "powerStatus", value : "", displayed : false )
    }
    
    //log.debug result
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
    def dispValue
    def newValue
    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)
    
    def result = []
    
    if (cmd.meterType == 33) {
        if (cmd.scale == 0) {
            newValue = cmd.scaledMeterValue
            if (newValue != state.energyValue) {
                dispValue = String.format("%3.${decimalPositions}f", newValue )
                
                result << createEvent( name: "currentKWH", value: dispValue as String, unit: "", displayed: false )
                
                state.energyValue = newValue
                
                BigDecimal costDecimal = newValue * ( kWhCost as BigDecimal)
                
                def costDisplay = new DecimalFormat("\$##,###.##").format(costDecimal)
                
                result << createEvent( name: "kwhCosts", value: "${costDisplay}", unit: "", displayed: false )
                result << createEvent( name: "energy", value: newValue, unit: "kWh", displayed: displayEvents )
            }
        } else if (cmd.scale == 1) {
            newValue = cmd.scaledMeterValue
            if (newValue != state.energyValue) {
                dispValue = newValue+"kVAh"
                state.energyValue = newValue
                
                result << createEvent( name: "currentKWH", value: dispValue as String, unit: "", displayed: false )
                result << createEvent( name: "energy", value: newValue, unit: "kVAh", displayed: displayEvents )
            }
        }
        else if (cmd.scale==2) {                
			newValue = cmd.scaledMeterValue								// Remove all rounding
            if (newValue < 0) {newValue = state.powerValue}				// Don't want to see negative numbers as a valid minimum value (something isn't right with the meter) so use the last known good meter reading
			if (newValue < wattsLimit) {								// don't handle any wildly large readings due to firmware issues	
	            if (newValue != state.powerValue) {						// Only process a meter reading if it isn't the same as the last one
                    dispValue = String.format("%3.${decimalPositions}f", newValue )
                    
	                result << createEvent( name: "currentWATTS", value: dispValue as String, unit: "", displayed: false )
                    
	                if (newValue < state.powerLow) {
	                    dispValue = newValue+"w"+" on "+timeString
                        state.powerLow = newValue
                        
	                    result << createEvent( name: "minWATTS", value: dispValue as String, unit: "", displayed: false )
	                }
                    
	                if (newValue > state.powerHigh) {
	                    dispValue = newValue+"w"+" on "+timeString
                        state.powerHigh = newValue
                        
	                    result << createEvent( name: "maxWATTS", value: dispValue as String, unit: "", displayed: false )
	                }
                    
	                state.powerValue = newValue
                    
                    result << createEvent( name: "power", value: newValue, unit: "W", displayed: displayEvents )
	            }
			}            
        }
    }
    
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd)
{
    def map = [ 
        name : "battery", 
        unit : "%", 
        displayed : displayEvents,
        value : (cmd.batteryLevel == 0xFF) ? 1 : cmd.batteryLevel
    ];
    
    // Fetch power source
    sendHubCommand( new physicalgraph.device.HubAction(zwave.configurationV1.configurationGet(parameterNumber: 20).format()) )
    
    return createEvent( map )
}

def zwaveEvent(physicalgraph.zwave.Command cmd)
{
    def result = []
    
    if( cmd.parameterNumber == 20 )
    {
        def powerSource = (cmd.scaledConfigurationValue == 0) ? "battery" : "mains"
        
        def map = [
            name : "powerSource",
            value : powerSource,
            description : "${device.displayName} is on ${powerSource}"
        ]
        
        result << createEvent( map )
    }
    else
    {
        log.error "Unhandled event ${cmd}"
    }
    
    return result
}

def refresh()
{
    delayBetween([
        zwave.meterV2.meterGet(scale: 0).format(),
        zwave.meterV2.meterGet(scale: 2).format()
	])
}

def poll()
{
    refresh()
}

// PING is used by Device-Watch in attempt to reach the Device
def ping() 
{
	refresh()
}

def resetkwh()
{
    _resetValues( false, false, true )
}

def resetmin()
{
    _resetValues( true, false, false )
}

def resetmax()
{
    _resetValues( false, true, false )
}

def resetMeter()
{
    _resetValues( true, true, true )
}

private _resetValues( resetMin, resetMax, resetkWh )
{
    log.debug "Resetting values min: ${resetMin}, max: ${resetMax}, kWh: ${resetkWh}"

    def timeString = new Date().format("MM-dd-yy h:mm a", location.timeZone)

    if( resetMin )
    {
        state.powerLow = 99999
        sendEvent(name: "resetMessage", value: "Watts Data Minimum Value Reset On:\n"+timeString, unit: "")
        sendEvent(name: "minWATTS", value: "", unit: "")
    }
    
    if( resetMax )
    {
        state.powerHigh = -1
        sendEvent(name: "resetMessage", value: "Watts Data Maximum Value Reset On:\n"+timeString, unit: "")    
        sendEvent(name: "maxWATTS", value: "", unit: "")
    }
    
    if( resetkWh )
    {
        sendEvent(name: "resetMessage", value: "Energy Data (kWh/Cost) Reset On:\n"+timeString, unit: "")       
        sendEvent(name: "currentKWH", value: "", unit: "")
        sendEvent(name: "kwhCosts", value: "Cost\n--", unit: "")
        delayBetween([
            zwave.meterV2.meterReset().format(),
            zwave.meterV2.meterGet(scale: 0).format(),
            zwave.meterV2.meterGet(scale: 2).format()
        ])
	}
    else
    {
        delayBetween([
            zwave.meterV2.meterGet(scale: 0).format(),
            zwave.meterV2.meterGet(scale: 2).format()
        ])
    }
}

def configure()
{
    log.debug "${device.displayName} configuring..."
    
    if( null == state.receivedOnBattery )
        state.receivedOnBattery = false
    
    if( null == state.powerHigh )
        resetmax()
    
    if( null == state.powerLow )
        resetmin()
    
    if( null == displayPowerStatus )
        displayPowerStatus = true
    log.debug "Setting displayPowerStatus to ${displayPowerStatus}."
    
    if( null == displayEvents )
        displayEvents = false
    log.debug "Setting displayEvents to ${displayEvents}."
    
    if( null == reportOnLoadChange )
        reportOnLoadChange = true
    log.debug "Setting reportOnLoadChange to ${reportOnLoadChange}."
    
    if( null == wattsChanged )
        wattsChanged = 50
    log.debug "Setting wattsChanged to ${wattsChanged}."
    
    if( null == wattsPercent )
        wattsPercent = 10
    log.debug "Setting wattsPercent to ${wattsPercent}."
    
    if( null == secondsWatts )
        secondsWatts = 15
    log.debug "Setting secondsWatts to ${secondsWatts}."
    
    if( null == secondsKwh )
        secondsKwh = 60
    log.debug "Setting secondsKwh to ${secondsKwh}."
    
    if( null == secondsBattery )
        secondsBattery = 900
    log.debug "Setting secondsBattery to ${secondsBattery}."

    if( null == decimalPositions )
        decimalPositions = 3
    log.debug "Setting decimalPositions to ${decimalPositions}."
    
    log.debug "Configured with state ${state}"

    def cmd = delayBetween([

 	// Performs a complete factory reset.  Use this all by itself and comment out all others below.  Once reset, comment this line out and uncomment the others to go back to normal
//  zwave.configurationV1.configurationSet(parameterNumber: 255, size: 4, scaledConfigurationValue: 1).format()

	// Accumulate kWh energy when Battery Powered. By default this is disabled to assist saving battery power. (0 == disable, 1 == enable)
	zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, scaledConfigurationValue: 1).format(),	    
	    
    // Send data based on a time interval (0), or based on a change in wattage (1).  0 is default and enables parameters 111, 112, and 113. 1 enables parameters 4 and 8.
    zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: reportOnLoadChange ? 1 : 0).format(),
        
    // If parameter 3 is 1, don't send unless watts have changed by 50 <default> for the whole device.   
    zwave.configurationV1.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: wattsChanged).format(),
        
    // If parameter 3 is 1, don't send unless watts have changed by 10% <default> for the whole device.        
    zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: wattsPercent).format(),

	// Defines the type of report sent for Reporting Group 1 for the whole device.  1->Battery Report, 4->Meter Report for Watt, 8->Meter Report for kWh
    zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4).format(), //watts

    // If parameter 3 is 0, report every XX Seconds (for Watts) for Reporting Group 1 for the whole device.
	zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: secondsWatts).format(),

    // Defines the type of report sent for Reporting Group 2 for the whole device.  1->Battery Report, 4->Meter Report for Watt, 8->Meter Report for kWh
    zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8).format(), //kWh

    // If parameter 3 is 0, report every XX seconds (for kWh) for Reporting Group 2 for the whole device.
	zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: secondsKwh).format(),

	// Defines the type of report sent for Reporting Group 3 for the whole device.  1->Battery Report, 4->Meter Report for Watt, 8->Meter Report for kWh
    zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 1).format(), //battery
    
    // If parameter 3 is 0, report every XX seconds (for battery) for Reporting Group 2 for the whole device.    
    zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: secondsBattery).format(),
    
    // Retreive status if it is on battery or on USB power
    zwave.configurationV1.configurationGet( parameterNumber: 20 ).format()
        
    ])

    cmd
}
