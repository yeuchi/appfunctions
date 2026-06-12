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
package com.example.appfunctions.agent.domain.appfunction

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

/** Unit tests for [GetInstalledAppsUseCase] using Robolectric. */
@RunWith(RobolectricTestRunner::class)
class GetInstalledAppsUseCaseTest {
    private lateinit var context: Context
    private lateinit var getInstalledAppsUseCase: GetInstalledAppsUseCase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        getInstalledAppsUseCase = GetInstalledAppsUseCase(context)
    }

    @Test
    fun invoke_returnsInstalledAppsSortedByLabel() {
        val shadowPackageManager = Shadows.shadowOf(context.packageManager)

        val appInfo1 =
            ApplicationInfo().apply {
                packageName = "com.example.b"
                nonLocalizedLabel = "B App"
            }
        val packageInfo1 =
            PackageInfo().apply {
                packageName = "com.example.b"
                applicationInfo = appInfo1
            }

        val appInfo2 =
            ApplicationInfo().apply {
                packageName = "com.example.a"
                nonLocalizedLabel = "A App"
            }
        val packageInfo2 =
            PackageInfo().apply {
                packageName = "com.example.a"
                applicationInfo = appInfo2
            }

        shadowPackageManager.installPackage(packageInfo1)
        shadowPackageManager.installPackage(packageInfo2)

        val result = getInstalledAppsUseCase()

        val filteredResult = result.filter { it.packageName.startsWith("com.example") }
        assertEquals(2, filteredResult.size)
        // Sorted by label: A App, B App
        assertEquals("com.example.a", filteredResult[0].packageName)
        assertEquals("A App", filteredResult[0].label)
        assertEquals("com.example.b", filteredResult[1].packageName)
        assertEquals("B App", filteredResult[1].label)
    }
}
