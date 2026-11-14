package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ApexRiderDatabase
import com.example.data.local.GameRepository
import com.example.data.local.RideHistoryEntity
import com.example.data.local.UpgradeStatType
import com.example.domain.model.BikeSpec
import com.example.domain.model.HighwayMap
import com.example.domain.model.PlayerState
import com.example.domain.model.RideResult
import com.example.domain.model.SteeringMode
import com.example.domain.model.TrafficMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApexRiderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository = GameRepository(
        ApexRiderDatabase.getInstance(application).apexRiderDao()
    )

    val playerState: StateFlow<PlayerState> = repository.observePlayerState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = PlayerState()
        )

    val recentRides: StateFlow<List<RideHistoryEntity>> = repository.observeRecentRides()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun selectMap(map: HighwayMap) {
        viewModelScope.launch {
            repository.selectMap(map)
        }
    }

    fun setSteeringMode(mode: SteeringMode) {
        viewModelScope.launch {
            repository.setSteeringMode(mode)
        }
    }

    fun setTrafficMode(mode: TrafficMode) {
        viewModelScope.launch {
            repository.setTrafficMode(mode)
        }
    }

    fun selectBike(bikeId: String) {
        viewModelScope.launch {
            repository.selectBike(bikeId)
        }
    }

    fun buyBike(spec: BikeSpec) {
        viewModelScope.launch {
            val success = repository.buyBike(spec)
            if (success) {
                _toastMessage.tryEmit("${spec.name} unlocked & equipped!")
            } else {
                _toastMessage.tryEmit("Need $${spec.price} cash to unlock ${spec.name}")
            }
        }
    }

    fun upgradeStat(spec: BikeSpec, statType: UpgradeStatType) {
        viewModelScope.launch {
            val success = repository.upgradeBikeStat(spec, statType)
            if (success) {
                _toastMessage.tryEmit("${statType.name.lowercase().replaceFirstChar { it.uppercase() }} upgraded!")
            } else {
                _toastMessage.tryEmit("Not enough cash or max level reached")
            }
        }
    }

    fun recordRideResult(result: RideResult) {
        viewModelScope.launch {
            repository.recordRideResult(result)
        }
    }
}
