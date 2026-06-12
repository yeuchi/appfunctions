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
package com.example.appfunctions.agent

import android.app.Instrumentation
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import com.example.appfunctions.agent.MainActivity.Companion.EXTRA_INSTRUMENTATION_ERROR
import java.util.concurrent.CountDownLatch

class ShellIdentityInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        start()
    }

    override fun onStart() {
        super.onStart()
        var errorMsg: String? = null
        val automation = uiAutomation
        if (automation != null) {
            automation.adoptShellPermissionIdentity(
                "android.permission.EXECUTE_APP_FUNCTIONS",
            )
        } else {
            errorMsg = "UiAutomation is null. Another instrumentation might be running."
        }
        val intent =
            Intent.makeMainActivity(
                ComponentName(
                    targetContext,
                    MainActivity::class.java,
                ),
            )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (errorMsg != null) {
            intent.putExtra(EXTRA_INSTRUMENTATION_ERROR, errorMsg)
        }
        targetContext.startActivity(intent)

        // Keep the instrumentation alive so the shell identity persists.
        try {
            CountDownLatch(1).await()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
