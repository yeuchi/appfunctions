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

import android.app.PendingIntent
import androidx.appfunctions.metadata.AppFunctionMetadata
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.domain.appfunction.ExecuteAppFunctionResult
import com.example.appfunctions.agent.ui.theme.AppFunctionsAgentTheme
import com.example.appfunctions.agent.ui.theme.GoogleSansCodeFontFamily

@Composable
fun FunctionsFoundContent(
    state: SearchAppResultState.FunctionsFoundState,
    onFunctionExpandedChange: (String, Boolean) -> Unit,
    onFunctionInputsChange: (String, Map<String, Any>) -> Unit,
    onInvoke: (AppFunctionMetadata) -> Unit,
    onClearResult: () -> Unit,
    onLaunchPendingIntent: (PendingIntent) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier.padding(horizontal = 2.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 10.dp, top = 2.dp, end = 10.dp, bottom = 80.dp),
    ) {
        items(
            items = state.functions,
            key = { function -> function.id },
        ) { function ->
            val expanded = state.expandedFunctions.contains(function.id)
            val inputValues = state.functionInputs[function.id] ?: emptyMap()

            AppFunctionItem(
                function = function,
                expanded = expanded,
                inputValues = inputValues,
                onExpandedChange = { isExpanded ->
                    onFunctionExpandedChange(function.id, isExpanded)
                },
                onInputValuesChange = { newValues ->
                    onFunctionInputsChange(function.id, newValues)
                },
                onInvoke = { _ -> onInvoke(function) },
            )
        }
    }

    if (state.executionResult != null) {
        AlertDialog(
            onDismissRequest = onClearResult,
            title = {
                Text(
                    text =
                        when (state.executionResult) {
                            is ExecuteAppFunctionResult.Error ->
                                stringResource(R.string.debugging_error)
                            is ExecuteAppFunctionResult.Data ->
                                stringResource(R.string.debugging_execution_result)
                            is ExecuteAppFunctionResult.PendingIntentAction ->
                                stringResource(R.string.debugging_action_required)
                        },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                ) {
                    val text =
                        when (val result = state.executionResult) {
                            is ExecuteAppFunctionResult.Error ->
                                result.exception.message ?: "Unknown error"
                            is ExecuteAppFunctionResult.Data -> "Success:\n${result.formattedJson}"
                            is ExecuteAppFunctionResult.PendingIntentAction ->
                                stringResource(R.string.debugging_pending_intent_disclaimer)
                            else -> ""
                        }
                    Text(
                        text = text,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = GoogleSansCodeFontFamily,
                            ),
                    )
                }
            },
            confirmButton = {
                val result = state.executionResult
                if (result is ExecuteAppFunctionResult.PendingIntentAction) {
                    Button(onClick = { onLaunchPendingIntent(result.pendingIntent) }) {
                        Text(text = stringResource(R.string.debugging_open))
                    }
                } else {
                    Button(onClick = onClearResult) {
                        Text(text = stringResource(R.string.debugging_confirm))
                    }
                }
            },
            dismissButton = {
                if (state.executionResult is ExecuteAppFunctionResult.PendingIntentAction) {
                    TextButton(onClick = onClearResult) {
                        Text(text = stringResource(R.string.debugging_dismiss))
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FunctionsFoundContentPreview() {
    val dummyState =
        SearchAppResultState.FunctionsFoundState(
            functions = emptyList(),
            expandedFunctions = emptySet(),
            functionInputs = emptyMap(),
            executionResult = null,
        )
    AppFunctionsAgentTheme {
        FunctionsFoundContent(
            state = dummyState,
            onFunctionExpandedChange = { _, _ -> },
            onFunctionInputsChange = { _, _ -> },
            onInvoke = { _ -> },
            onClearResult = {},
            onLaunchPendingIntent = {},
        )
    }
}
