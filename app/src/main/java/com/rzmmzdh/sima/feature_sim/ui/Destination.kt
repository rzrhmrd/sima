package com.rzmmzdh.sima.feature_sim.ui

sealed class Destination(val route: String) {
    object SimScreen : Destination("SIM_SCREEN")
}
