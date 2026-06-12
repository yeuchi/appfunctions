/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.appfunctions.agent.ui.screens.agentdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appfunctions.agent.data.SettingsRepository
import com.example.appfunctions.agent.domain.appfunction.GetConnectedAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel for the Connected Apps screen. */
@HiltViewModel
class ConnectedAppsViewModel
    @Inject
    constructor(
        private val getConnectedAppsUseCase: GetConnectedAppsUseCase,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val uiState: StateFlow<ConnectedAppsUiState> =
            getConnectedAppsUseCase()
                .map { connectedApps -> ConnectedAppsUiState(connectedApps = connectedApps) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = ConnectedAppsUiState(),
                )

        fun setAppConnected(
            packageName: String,
            connected: Boolean,
        ) {
            viewModelScope.launch { settingsRepository.setAppConnected(packageName, connected) }
        }
    }
