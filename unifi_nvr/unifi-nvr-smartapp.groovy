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
 *  For more information, see https://github.com/project802/smartthings/unifi_nvr
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
    log.info "UniFi NVR: installed with settings: ${settings}"

    initialize()
}

/**
 * updated() - Called by ST platform
 */
def updated() {
    log.info "UniFi NVR: updated with settings: ${settings}"
    
    initialize()
}

/**
 * initialize() - Called by the ST platform
 */
def initialize() {
    nvr_initialize()
}

/**
 * nvr_initialize() - Clear state and poll the bootstrap API and the result is handled by nvr_bootstrapPollCallback
 */
def nvr_initialize()
{
    state.nvrName = "Unknown"
    
    state.nvrTarget = "${settings.nvrAddress}:${settings.nvrPort}"
    state.apiKey = "${settings.apiKey}"
    log.info "nvr_initialize: NVR API is located at ${state.nvrTarget}"

    sendHubCommand( new physicalgraph.device.HubAction("""GET /api/2.0/bootstrap?apiKey=${settings.apiKey} HTTP/1.1\r\n Accept: application/json\r\nHOST: ${state.nvrTarget}\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${state.nvrTarget}", [callback: nvr_bootstrapPollCallback]))
}

/**
 * nvr_bootstrapPollCallback() - Callback from HubAction with result from GET
 */
def nvr_bootstrapPollCallback( physicalgraph.device.HubResponse hubResponse )
{
	def data = hubResponse.json?.data
    
    if( !data || !data.isLoggedIn )
    {
    	log.error "nvr_bootstrapPollCallback: unable to get data from NVR!"
        return
    }
    
    if( data.isLoggedIn[0] != true )
    {
    	log.error "nvr_bootstrapPollCallback: unable to log in!  Please check API key."
        return
    }
    
    state.nvrName = data.nvrName[0]
    log.info "nvr_bootstrapPollCallback: response from ${state.nvrName}"
    
    if( data.cameras?.size < 1 )
    {
    	log.warn "nvr_bootstrapPollCallback: no cameras found!"
    	return
    }
    
    log.info "nvr_bootstrapPollCallback: found ${data.cameras.size} camera(s)"
    
    data.cameras.each { camera ->
        def dni = "${camera.mac[0]}"

        if( getChildDevice(dni) )
        {
            log.info "nvr_bootstrapPollCallback: already have child ${dni}"
        }
        else
        {
            log.info "nvr_bootstrapPollCallback: adding child ${dni}, ${camera.name[0]}, ${camera.model[0]}, ${camera.uuid[0]}, ${camera._id[0]}"
            try
            {
                addChildDevice( "project802", "UniFi NVR Camera", dni, location.hubs[0].id, [
                    "label" : camera.name[0] + " (" + camera.model[0] + ")",
                    "data": [
                        "uuid" : camera.uuid[0],
                        "name" : camera.name[0],
                        "id" : camera._id[0]
                        ]
                    ])
             }
             catch( exception )
             {

                 log.error "nvr_bootstrapPollCallback: adding child ${dni} failed, continuing..."
             }
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
