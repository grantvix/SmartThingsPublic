/**
 *  Camera Motion Sensor
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
    name: "Alexa Water Trigger",
    namespace: "grantvix",
    author: "GRANT VIX",
    description: "Will toggle a switch when water detected from water sensors.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Water Sensor") {
        input "water_sensor", "capability.waterSensor", required: true, title: "What waster sensor?"
    }
    section("Switch") {
        input "virtual_switch", "capability.switch", required: true, title: "What switch to trigger?"
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

    subscribe(water_sensor, "water.wet", waterWetHandler)
    subscribe(water_sensor, "water.dry", waterDryHandler)
}

def waterWetHandler(evt) {
    log.debug "waterWetHandler called: $evt"
    virtual_switch.on()
}

def waterDryHandler(evt) {
    log.debug "waterDryHandler called: $evt"
    virtual_switch.off()
}