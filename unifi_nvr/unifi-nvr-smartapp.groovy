/**
 *  UniFi NVR SmartApp
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
 *  This SmartApp connects to the NVR software and creates a child device for each camera discovered.
 *  As the API from Ubiquiti is not officially supported, this integration could break (however unlikely).
 *  It requires the accompanying device handler for the cameras.
 *  
 *  To use the app, you must have an active API key from a user with proper permissions.  In the NVR software
 *  click on "Users", select a user (probably your super administrator) then select "API Access".  Make sure
 *  that "Allow API Usage" is enabled then copy the API key into the app settings.
 *
 *  Your SmartThings hub running this SmartApp must be on the same subnet as the NVR software.  This is a 
 *  restriction of the hub that it will not perform HTTP GET commands outside the subnet.  The hub also
 *  does not currently support HTTPS interactions so ensure that the network is trusted.
 *
 *  The NVR will use port 7080 for HTTP transactions out of the box.
 *
 *  https://github.com/project802/smartthings
 */
definition(
    name: "UniFi NVR SmartApp",
    namespace: "project802",
    author: "Chris Vincent",
    description: "UniFi NVR SmartApp",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    input name: "nvrAddress", type: "text", title: "NVR Address", description: "NVR IP address", required: true, displayDuringSetup: true, defaultValue: "10.0.0.205"
    input name: "nvrPort", type: "number", title: "NVR Port", description: "NVR HTTP port", required: true, displayDuringSetup: true, defaultValue: 7080
    input name: "apiKey", type: "text", title: "API Key", description: "API key", required: true, displayDuringSetup: true, defaultValue: "pJe9AtPTFCrtBCzd"
}

/**
 * installed() - Called by ST platform
 */
def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

/**
 * updated() - Called by ST platform
 */
def updated() {
    log.debug "Updated with settings: ${settings}"
    
    initialize()
}

/**
 * initialize() - Clear state and poll the bootstrap API and the result is handled by nvr_bootstrapPollCallback
 */
def initialize() {
    log.debug "initialize()"
    
    state.nvrTarget = "${settings.nvrAddress}:${settings.nvrPort}"
    state.apiKey = "${settings.apiKey}"
    log.debug "NVR API is located at ${state.nvrTarget}"

    sendHubCommand( new physicalgraph.device.HubAction("""GET /api/2.0/bootstrap?apiKey=${settings.apiKey} HTTP/1.1\r\n Accept: application/json\r\nHOST: ${state.nvrTarget}\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${state.nvrTarget}", [callback: nvr_bootstrapPollCallback]))
}

/**
 * nvr_bootstrapPollCallback() - Callback from HubAction with result from GET
 */
def nvr_bootstrapPollCallback( physicalgraph.device.HubResponse hubResponse )
{
    log.debug "bootstrapResponseHandler()"
	
    // This could use some error checking
    hubResponse.json.data.cameras.each { camera ->
    	def appChildDevice
        
    	def dni = "${camera.mac[0]}"
        log.debug "dni: ${dni}"

        def child = getChildDevice( dni )
        
        if( !child )
        {
        	log.debug "adding child: ${camera.name[0]}, ${camera.model[0]}, ${camera.uuid[0]}, ${camera._id[0]}"
        	addChildDevice( "project802", "UniFi NVR Camera", dni, location.hubs[0].id, [
            	"label" : camera.name[0] + " (" + camera.model[0] + ")",
            	"data": [
                    "uuid" : camera.uuid[0],
                    "name" : camera.name[0],
                    "id" : camera._id[0]
                    ]
                ])
                    
        }
    }
}

/**
 * _getApiKey() - Here for the purpose of children
 */
def _getApiKey()
{
    return state.apiKey
}

/**
 * _getNvrTarget() - Here for the purpose of children
 */
def _getNvrTarget()
{
    return state.nvrTarget
}
