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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.ui.theme.AppFunctionsAgentTheme

/**
 * A dialog that shows status messages, including errors and info.
 *
 * @param title The title of the dialog.
 * @param message The message content of the dialog.
 * @param isError True if this is an error message, false for info.
 * @param onDismiss Called when the dialog is dismissed.
 */
@Composable
fun StatusWarningDialog(
    title: String,
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            if (isError) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                )
            } else {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.dialog_ok)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun StatusWarningDialogErrorPreview() {
    AppFunctionsAgentTheme {
        StatusWarningDialog(
            title = "Error Title",
            message = "This is an error message.",
            isError = true,
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusWarningDialogInfoPreview() {
    AppFunctionsAgentTheme {
        StatusWarningDialog(
            title = "Info Title",
            message = "This is an info message.",
            isError = false,
            onDismiss = {},
        )
    }
}
