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
 *  This device handler is required to be paired with the accompanying SmartApp.  
 *  
 *  As the API from Ubiquiti is not officially supported, this integration could break (however unlikely).
 * 
 *  https://github.com/project802/smartthings
 */
metadata {
	definition (name: "UniFi NVR Camera", namespace: "project802", author: "Chris Vincent") {
    	capability "Motion Sensor"
        capability "Sensor"
        capability "Refresh"
	}

	simulator {
		
	}

	tiles {
		standardTile("motion", "device.motion", width: 1, height: 1) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		}
        
		main "motion"
        details "motion"
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
    
    state.uuid 						= getDataValue( "uuid" )
    state.name 						= getDataValue( "name" )
    state.id 						= getDataValue( "id" )
    state.lastRecordingStartTime 	= null
    state.motion 					= "inactive"
    state.pollInterval 				= settings.pollInterval ? settings.pollInterval : 5
    
    log.debug "state: ${state}"
    
    refresh()
    
    nvr_cameraPoll()
}

/**
 * refresh() - Called by ST platform, part of "Refresh" capability
 */
def refresh()
{
	_sendMotion( state.motion )
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
    
    // Only do motion detection if the camera is configured for it
    if( data.recordingSettings.motionRecordEnabled )
    {
    	// Motion is based on a new recording being present
    	if( state.lastRecordingStartTime && (state.lastRecordingStartTime != data.lastRecordingStartTime) )
        {
            motion = "active"
        }
        
        state.lastRecordingStartTime = data.lastRecordingStartTime;
    }
    
    if( motion != state.motion )
    {
   		_sendMotion( motion )
    }
    
    state.motion = motion
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
    
    def description = (motion == "active" ? " detected motion" : " motion has stopped")
    
    def map = [ name: "motion", value: motion, descriptionText: device.displayName + description ]
    //log.debug "_sendMotion: ${map}"
    
    sendEvent( map )
}