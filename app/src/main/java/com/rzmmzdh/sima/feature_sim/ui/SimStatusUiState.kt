package com.rzmmzdh.sima.feature_sim.ui

import com.rzmmzdh.sima.feature_sim.model.Sim

data class SimStatusUiState(val data: List<Sim> = emptyList(), val isLoading: Boolean = false)