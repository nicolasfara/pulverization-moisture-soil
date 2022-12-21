package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.core.StateRepresentation
import kotlinx.serialization.Serializable

@Serializable
sealed interface StateOps : StateRepresentation

@Serializable
object GetState : StateOps

@Serializable
data class MoistureState(val moisture: Double) : StateOps
