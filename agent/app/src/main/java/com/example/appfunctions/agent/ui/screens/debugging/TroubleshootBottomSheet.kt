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
package com.example.appfunctions.agent.ui.screens.debugging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.domain.troubleshoot.StepStatus

@Composable
fun TroubleshootResult(
    state: SearchAppResultState.TroubleshootUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        state.report?.let { report ->
            Text(
                text = stringResource(R.string.troubleshoot_results_for, report.packageName),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.width(8.dp))

            report.steps.forEach { step ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector =
                            when (step.status) {
                                StepStatus.PASS -> Icons.Filled.CheckCircle
                                StepStatus.FAIL -> Icons.Filled.Error
                                StepStatus.WARNING -> Icons.Filled.Warning
                            },
                        contentDescription = null,
                        tint =
                            when (step.status) {
                                StepStatus.PASS -> MaterialTheme.colorScheme.primary
                                StepStatus.FAIL -> MaterialTheme.colorScheme.error
                                StepStatus.WARNING -> MaterialTheme.colorScheme.secondary
                            },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(step.titleResId),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        step.messageResId?.let {
                            Text(
                                text = stringResource(it, *step.messageArgs.toTypedArray()),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
