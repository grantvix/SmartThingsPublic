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
    
	initialize()
}

def initialize() {
	log.debug "Initialized."

	// Reset state by unsubscribing to all event, unscheduling all tasks, and resetting state
	unsubscribe()
    unschedule()
	state.active = false

	subscribe(button, "button.pushed", buttonPressed)
    subscribe(light_switch[0], "switch.off", switchedOff)
}

def switchedOff(evt) {
	log.debug "Switch turned off."
}

def buttonPressed(evt) {
	log.debug "Button: ${evt} ${state.active}"

	state.active = !state.active

	if (state.active == true) {
    	log.debug "Setting inside to outside."
        
        matchWindow()
		runEvery1Minute(matchWindow)
    } else {
    	log.debug "Reverting back to local control."
    	
        log.debug "Hue: ${state.hue}"
        log.debug "Saturation: ${state.saturation}"
        log.debug "Level: ${state.level}"
        log.debug "Hex: ${state.hex}"
        log.debug "Color Temperature: ${state.color}"
    
    	// Stop polling
    	unschedule()
        
        def _color = [hue: 62, saturation: 17]
        
        // Set all lights to back to saved state
        light_switch.each { n -> 
                                // Setting color will turn the lights back on
                                if (state.level != 0) {
                                	n.setColor(_color)
                                    n.setColorTemperature(3003)
                                }
                                n.setLevel(100)
                          }
    }
}

def matchWindow() {
    log.debug "Lux Level: ${light_sensor.currentIlluminance}"
    
    def newLevel = (light_sensor.currentIlluminance/800 * 100).toInteger()
    if (newLevel > 100) {
    	newLevel = 100
    }
   
   	// Turn off is someone turns off the lights or if the sensor hits 0
    if (newLevel == 0) {
    	state.active = true
    }
    
    log.debug "New light level: ${newLevel}%"
    
    // Set lights to daylight and match them to the light sensor
    def daylightColor = [hue: 62, saturation: 16]
    light_switch.each { n -> 
                        	// Don't flash the lights by accident
                            if (newLevel != 0) {
                            	n.setColor(daylightColor)
    	                    	n.setColorTemperature(5000)
                            }
                            n.setLevel(newLevel)
                      }
}