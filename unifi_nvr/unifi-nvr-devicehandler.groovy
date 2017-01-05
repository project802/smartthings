/**
 *  UniFi NVR Camera
 *
 *  Copyright 2016 Chris Vincent
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
 *  -----------------------------------------------------------------------------------------------------------------
 * 
 *  For more information, see https://github.com/project802/smartthings/unifi_nvr
 */
metadata {
    definition (name: "UniFi NVR Camera", namespace: "project802", author: "Chris Vincent") {
        capability "Motion Sensor"
        capability "Sensor"
        capability "Refresh"
    }
    
    simulator {
        
    }
    
    tiles( scale: 2 ) {
        standardTile("motion", "device.motion", width: 2, height: 2) {
            state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
            state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
        }
        standardTile( "connectionStatus", "device.connectionStatus", width: 2, height: 2 ) {
            state( "CONNECTED", label: "Connected", icon: "st.samsung.da.RC_ic_power", backgroundColor: "#79b821" )
            state( "DISCONNECTED", label: "Disconnected", icon: "st.samsung.da.RC_ic_power", backgroundColor: "#ffffff" )
        }
        
        main( "motion" )
        details( "motion", "connectionStatus" )
    }
    
    preferences {
        input "pollInterval", "number", title: "Poll Interval", description: "Polling interval in seconds for motion detection", defaultValue: 5
    }
}

/**
 * installed() - Called by ST platform
 */
def installed()
{
    updated()
}

/**
 * updated() - Called by ST platform
 */
def updated()
{
    // Unschedule here to remove any zombie runIn calls that the platform
    // seems to keep around even if the code changes during dev
    unschedule()
    
    state.uuid                   = getDataValue( "uuid" )
    state.name                   = getDataValue( "name" )
    state.id                     = getDataValue( "id" )
    state.lastRecordingStartTime = null
    state.motion                 = "inactive"
    state.connectionStatus       = "DISCONNECTED"
    state.pollInterval           = settings.pollInterval ? settings.pollInterval : 5
    
    log.info "${device.displayName} updated with state: ${state}"
    
    refresh()
    
    nvr_cameraPoll()
}

/**
 * refresh() - Called by ST platform, part of "Refresh" capability
 */
def refresh()
{
    _sendMotion( state.motion )
    _sendConnectionStatus( state.connectionStatus )
}

/**
 * nvr_cameraPoll()
 *
 * Once called, starts cyclic call to itself periodically.  Main loop of the device handler to make API
 * to the NVR software to see if motion has changed.  API call result is handled by nvr_cameraPollCallback().
 */
def nvr_cameraPoll() 
{
    def key = parent._getApiKey()
    def target = parent._getNvrTarget()
    
    sendHubCommand( new physicalgraph.device.HubAction("""GET /api/2.0/camera/${state.id}?apiKey=${key} HTTP/1.1\r\n Accept: application/json\r\nHOST: ${target}\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${target}", [callback: nvr_cameraPollCallback]))
    
    // Set overwrite to true instead of using unschedule(), which is expensive, to ensure no dups
    runIn( state.pollInterval, nvr_cameraPoll, [overwrite: true] )
}

/**
 * nvr_cameraPollCallback() - Callback from HubAction with result from GET
 */
def nvr_cameraPollCallback( physicalgraph.device.HubResponse hubResponse )
{
    def motion = "inactive"
    def data = hubResponse.json.data[0]
    
    //log.debug "nvr_cameraPollCallback: ${device.displayName}, ${hubResponse}"

    if( data.state != state.connectionStatus )
    {
        state.connectionStatus = data.state
        _sendConnectionStatus( "${state.connectionStatus}" )
    }
    
    // Only do motion detection if the camera is connected and configured for it
    if( (state.connectionStatus == "CONNECTED") && (data.recordingSettings?.motionRecordEnabled) )
    {
    	// Motion is based on a new recording being present
    	if( state.lastRecordingStartTime && (state.lastRecordingStartTime != data.lastRecordingStartTime) )
        {
            motion = "active"
        }
        
        state.lastRecordingStartTime = data.lastRecordingStartTime;
    }
    else
    {
        //log.warn "nvr_cameraPollCallback: ${device.displayName} camera disconnected or motion not enabled"
    }
    
    // fall-through takes care of case if camera motion was active but became disconnected before becoming inactive
    if( motion != state.motion )
    {
        state.motion = motion
        _sendMotion( motion )
    }
}

/**
 * _sendMotion() - Sends a motion event to the ST platform
 *
 * @arg motion Either "active" or "inactive"
 */
def _sendMotion( motion )
{
    if( (motion != "active") && (motion != "inactive") )
    {
    	return
    }
    
    //log.debug( "_sendMotion( ${motion} )" )
    
    def description = (motion == "active" ? " detected motion" : " motion has stopped")
    
    def map = [ 
                name: "motion",
                value: motion, 
                descriptionText: device.displayName + description
              ]
    
    sendEvent( map )
}

/**
 * _sendConnectionStatus() - Sends a connection status event to the ST platform
 *
 * @arg motion Either "CONNECTED" or "DISCONNECTED"
 */
def _sendConnectionStatus( connectionStatus )
{
    if( (connectionStatus != "CONNECTED") && (connectionStatus != "DISCONNECTED") )
    {
        return
    }
    
    //log.debug "_sendConnectionStatus( ${connectionStatus} )"
    
    def map = [
                name: "connectionStatus",
                value: connectionStatus,
                descriptionText: device.displayName + " is ${connectionStatus}"
              ]
              
    sendEvent( map )
}
