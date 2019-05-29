/**
 *  Match outdoor lights
 *
 *  Copyright 2019 GRANT VIX
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
 */
definition(
    name: "Make Outside Inside",
    namespace: "grantvix",
    author: "GRANT VIX",
    description: "Will read lumens outside and match for inside lights.",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light21-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light21-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light21-icn@2x.png")

preferences {
    section("Illuminance Measurement") {
        input "light_sensor", "capability.illuminanceMeasurement", required: true, title: "What is the illuminance sensor?"
    }
    section("Switch Level") {
        input "light_switch", "capability.switchLevel", required: true, title: "What lights to control?", multiple: true
    }
    section("Button") {
    	input "button", "capability.button", required: true, title: "Button used to turn on and off."
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "Initialized."

	state.active = false

	subscribe(button, "button.pushed", buttonPressed)
}

def buttonPressed(evt) {
	log.debug "Button: ${evt} ${state.active}"

	state.active = !state.active

	if (state.active == true) {
    	log.debug "Setting outside to inside."
        
        matchWindow()
		runEvery1Minute(matchWindow)
    } else {
    	log.debug "Reverting back to local control."
    
    	// Stop polling
    	unsubscribe(matchWindow)
        
        // Set all lights to 100%
        light_switch.each { n -> n.setLevel(100) }
    }
}

def matchWindow() {
    log.debug "Lux Level: ${light_sensor.currentIlluminance}"
    
    def newLevel = (light_sensor.currentIlluminance/800 * 100).toInteger()
    if (newLevel > 100) {
    	newLevel = 100
    }
    
    log.debug "New light level: ${newLevel}%"
    
    // Set lights to daylight and match them to the light sensor
    light_switch.each { n -> 
                        	n.setLevel(newLevel)
                            
                            // If level is 0, will turn off the lights. Setting hue will cause the lights to come back on
                            if (newLevel != 0) {
                            	n.setHue(62)
                        		n.setSaturation(16)
                            }
                      }
	// light_switch.each { n -> log.debug "Light Color: ${n.currentColor}" }
}