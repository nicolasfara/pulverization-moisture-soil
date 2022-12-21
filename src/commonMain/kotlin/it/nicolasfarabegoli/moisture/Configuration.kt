package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.core.ActuatorsComponent
import it.nicolasfarabegoli.pulverization.core.BehaviourComponent
import it.nicolasfarabegoli.pulverization.core.SensorsComponent
import it.nicolasfarabegoli.pulverization.core.StateComponent
import it.nicolasfarabegoli.pulverization.dsl.Device
import it.nicolasfarabegoli.pulverization.dsl.Edge
import it.nicolasfarabegoli.pulverization.dsl.pulverizationConfig

val configuration = pulverizationConfig {
    logicalDevice("moisture") {
        SensorsComponent deployableOn Device
        ActuatorsComponent deployableOn Device
        StateComponent and BehaviourComponent deployableOn Edge
    }
}
