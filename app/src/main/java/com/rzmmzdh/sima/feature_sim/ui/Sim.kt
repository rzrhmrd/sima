package com.rzmmzdh.sima.feature_sim.ui

data class Sim(
    val subId: Int = 0,
    var slotNumber: Int = 0,
    val carrierName: String = "Carrier",
    var signalStrength: Int? = 0
)
