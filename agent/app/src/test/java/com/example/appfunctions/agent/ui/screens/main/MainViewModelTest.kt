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
package com.example.appfunctions.agent.ui.screens.main

import androidx.lifecycle.SavedStateHandle
import com.example.appfunctions.agent.domain.troubleshoot.CheckAppFunctionPermissionUseCase
import com.example.appfunctions.agent.domain.troubleshoot.CheckMainlineVersionUseCase
import com.example.appfunctions.agent.domain.troubleshoot.CheckOsVersionUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockCheckAppFunctionPermissionUseCase: CheckAppFunctionPermissionUseCase
    private lateinit var mockCheckOsVersionUseCase: CheckOsVersionUseCase
    private lateinit var mockCheckMainlineVersionUseCase: CheckMainlineVersionUseCase
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockCheckAppFunctionPermissionUseCase = mockk()
        mockCheckOsVersionUseCase = mockk()
        mockCheckMainlineVersionUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init checks status and shows not supported when OS not supported`() =
        runTest {
            every { mockCheckOsVersionUseCase() } returns false
            every { mockCheckMainlineVersionUseCase() } returns true

            viewModel =
                MainViewModel(
                    mockCheckAppFunctionPermissionUseCase,
                    mockCheckOsVersionUseCase,
                    mockCheckMainlineVersionUseCase,
                    SavedStateHandle(),
                )

            val state = viewModel.uiState.value
            assertEquals(AppStatus.NotSupported, state.appStatus)
            assertTrue(state.showDialog)
        }

    @Test
    fun `init checks status and shows not supported when Mainline not supported`() =
        runTest {
            every { mockCheckOsVersionUseCase() } returns true
            every { mockCheckMainlineVersionUseCase() } returns false

            viewModel =
                MainViewModel(
                    mockCheckAppFunctionPermissionUseCase,
                    mockCheckOsVersionUseCase,
                    mockCheckMainlineVersionUseCase,
                    SavedStateHandle(),
                )

            val state = viewModel.uiState.value
            assertEquals(AppStatus.NotSupported, state.appStatus)
            assertTrue(state.showDialog)
        }

    @Test
    fun `init checks status and shows permission granted when supported and permitted`() =
        runTest {
            every { mockCheckOsVersionUseCase() } returns true
            every { mockCheckMainlineVersionUseCase() } returns true
            every { mockCheckAppFunctionPermissionUseCase() } returns true

            viewModel =
                MainViewModel(
                    mockCheckAppFunctionPermissionUseCase,
                    mockCheckOsVersionUseCase,
                    mockCheckMainlineVersionUseCase,
                    SavedStateHandle(),
                )

            val state = viewModel.uiState.value
            assertEquals(AppStatus.PermissionGranted, state.appStatus)
            assertTrue(state.showDialog)
        }

    @Test
    fun `init checks status and shows permission missing when supported but not permitted`() =
        runTest {
            every { mockCheckOsVersionUseCase() } returns true
            every { mockCheckMainlineVersionUseCase() } returns true
            every { mockCheckAppFunctionPermissionUseCase() } returns false

            viewModel =
                MainViewModel(
                    mockCheckAppFunctionPermissionUseCase,
                    mockCheckOsVersionUseCase,
                    mockCheckMainlineVersionUseCase,
                    SavedStateHandle(),
                )

            val state = viewModel.uiState.value
            assertEquals(AppStatus.PermissionMissing, state.appStatus)
            assertTrue(state.showDialog)
        }

    @Test
    fun `dismissDialog updates state to hide dialog`() =
        runTest {
            every { mockCheckOsVersionUseCase() } returns true
            every { mockCheckMainlineVersionUseCase() } returns true
            every { mockCheckAppFunctionPermissionUseCase() } returns true

            viewModel =
                MainViewModel(
                    mockCheckAppFunctionPermissionUseCase,
                    mockCheckOsVersionUseCase,
                    mockCheckMainlineVersionUseCase,
                    SavedStateHandle(),
                )

            viewModel.dismissDialog()

            val state = viewModel.uiState.value
            assertFalse(state.showDialog)
        }

    @Test
    fun `init checks status and shows instrumentation failed when error present in savedStateHandle`() =
        runTest {
            val savedStateHandle =
                SavedStateHandle().apply { set("EXTRA_INSTRUMENTATION_ERROR", "Some error") }

            viewModel =
                MainViewModel(
                    mockCheckAppFunctionPermissionUseCase,
                    mockCheckOsVersionUseCase,
                    mockCheckMainlineVersionUseCase,
                    savedStateHandle,
                )

            val state = viewModel.uiState.value
            assertTrue(state.appStatus is AppStatus.InstrumentationFailed)
            assertEquals("Some error", (state.appStatus as AppStatus.InstrumentationFailed).error)
            assertTrue(state.showDialog)
        }
}
