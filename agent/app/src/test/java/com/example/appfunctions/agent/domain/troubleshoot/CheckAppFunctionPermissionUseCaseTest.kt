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
package com.example.appfunctions.agent.domain.troubleshoot

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CheckAppFunctionPermissionUseCaseTest {
    private lateinit var context: Context
    private lateinit var useCase: CheckAppFunctionPermissionUseCase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        useCase = CheckAppFunctionPermissionUseCase(context)
    }

    @Test
    fun invoke_permissionGranted_returnsTrue() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(app).grantPermissions("android.permission.EXECUTE_APP_FUNCTIONS")

        val result = useCase()

        assertTrue(result)
    }

    @Test
    fun invoke_permissionDenied_returnsFalse() {
        // Permissions are denied by default in Robolectric unless granted
        val result = useCase()

        assertFalse(result)
    }
}
