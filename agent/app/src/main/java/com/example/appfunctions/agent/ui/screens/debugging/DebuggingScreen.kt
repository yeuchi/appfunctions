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
import android.content.res.Resources
import androidx.appfunctions.metadata.AppFunctionMetadata
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appfunctions.agent.R
import com.example.appfunctions.agent.domain.appfunction.AppInfo
import com.example.appfunctions.agent.ui.theme.AppFunctionsAgentTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebuggingScreen(viewModel: DebuggingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DebuggingScreenContent(
        uiState = uiState,
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        onAppSelected = { viewModel.onAppSelected(it) },
        onClearSelectedApp = { viewModel.onClearSelectedApp() },
        onFunctionInputsChange = { functionId, inputs ->
            viewModel.onFunctionInputsChange(functionId, inputs)
        },
        onInvoke = { viewModel.invokeFunction(it) },
        onClearResult = { viewModel.clearResult() },
        onFunctionExpandedChange = { functionId, expanded ->
            viewModel.onFunctionExpandedChange(functionId, expanded)
        },
        onLaunchPendingIntent = { viewModel.launchPendingIntent(it) },
        onTogglePin = { viewModel.onTogglePin(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebuggingScreenContent(
    uiState: DebuggingUiState,
    onSearchQueryChanged: (String) -> Unit,
    onAppSelected: (AppInfo) -> Unit,
    onClearSelectedApp: () -> Unit,
    onFunctionInputsChange: (String, Map<String, Any>) -> Unit,
    onInvoke: (AppFunctionMetadata) -> Unit,
    onClearResult: () -> Unit,
    onFunctionExpandedChange: (String, Boolean) -> Unit,
    onLaunchPendingIntent: (PendingIntent) -> Unit,
    onTogglePin: (AppInfo) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) { focusManager.clearFocus() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Unspecified,
        topBar = {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Searchable Dropdown
                AppDropdown(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    appGroups = uiState.filteredApps,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onAppSelected = onAppSelected,
                    onClearSelectedApp = onClearSelectedApp,
                    onTogglePin = onTogglePin,
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding()),
        ) {
            when (val searchAppResultState = uiState.searchAppResultState) {
                is SearchAppResultState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.debugging_select_app_prompt),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                is SearchAppResultState.FunctionsFoundState -> {
                    FunctionsFoundContent(
                        state = searchAppResultState,
                        onFunctionExpandedChange = onFunctionExpandedChange,
                        onFunctionInputsChange = onFunctionInputsChange,
                        onInvoke = onInvoke,
                        onClearResult = onClearResult,
                        onLaunchPendingIntent = onLaunchPendingIntent,
                    )
                }
                is SearchAppResultState.TroubleshootUiState -> {
                    TroubleshootResult(
                        state = searchAppResultState,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdown(
    appGroups: AppsGroupState,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onAppSelected: (AppInfo) -> Unit,
    onClearSelectedApp: () -> Unit,
    onTogglePin: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        Surface(
            modifier = Modifier.padding(bottom = 8.dp),
            shadowElevation = 2.dp,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceBright,
        ) {
            OutlinedTextField(
                value = searchQuery,
                shape = CircleShape,
                singleLine = true,
                placeholder = { Text(text = stringResource(R.string.debugging_search_app)) },
                onValueChange = {
                    onSearchQueryChanged(it)
                    expanded = true
                },
                trailingIcon = {
                    Row(
                        modifier = Modifier.padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onClearSelectedApp() }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                },
                colors =
                    ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                    ),
                modifier =
                    Modifier.fillMaxWidth()
                        .menuAnchor(
                            ExposedDropdownMenuAnchorType.Companion.PrimaryEditable,
                            enabled = true,
                        ),
            )
        }

        val sections = appGroups.sections
        val pinnedPackageNames =
            remember(sections) {
                sections
                    .find { it.titleRes == Resources.ID_NULL }
                    ?.apps
                    ?.map { it.packageName }
                    ?.toSet() ?: emptySet()
            }

        LazyExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize(),
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            shape = RoundedCornerShape(28.dp),
        ) {
            sections.forEachIndexed { index, section ->
                if (index > 0) {
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp),
                        )
                    }
                }

                // Not showing pinned section title
                if (index != 0) {
                    item {
                        Text(
                            text = stringResource(id = section.titleRes),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }

                items(
                    items = section.apps,
                    key = { app -> "${section.titleRes}_${app.packageName}" },
                ) { app ->
                    AppDropdownItem(
                        app = app,
                        isPinned = pinnedPackageNames.contains(app.packageName),
                        onAppSelected = onAppSelected,
                        onSearchQueryChanged = onSearchQueryChanged,
                        onTogglePin = onTogglePin,
                        onExpandedChange = { expanded = it },
                        showPin = section.showPin,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppDropdownItem(
    app: AppInfo,
    isPinned: Boolean,
    onAppSelected: (AppInfo) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onTogglePin: (AppInfo) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    showPin: Boolean = true,
) {
    DropdownMenuItem(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                app.icon?.let {
                    Image(
                        bitmap = it.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = app.label, modifier = Modifier.weight(1f))
                if (showPin) {
                    IconButton(onClick = { onTogglePin(app) }) {
                        Icon(
                            imageVector =
                                if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "Unpin" else "Pin",
                            tint =
                                if (isPinned) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                } else {
                    Box(Modifier.minimumInteractiveComponentSize())
                }
            }
        },
        onClick = {
            onSearchQueryChanged(app.label)
            onAppSelected(app)
            onExpandedChange(false)
        },
    )
}

@Preview(showBackground = true)
@Composable
fun DebuggingScreenPreview() {
    val dummyState =
        DebuggingUiState(
            filteredApps = AppsGroupState(),
            selectedApp = null,
        )
    AppFunctionsAgentTheme {
        DebuggingScreenContent(
            uiState = dummyState,
            onSearchQueryChanged = {},
            onAppSelected = {},
            onClearSelectedApp = {},
            onFunctionInputsChange = { _, _ -> },
            onInvoke = {},
            onClearResult = {},
            onFunctionExpandedChange = { _, _ -> },
            onLaunchPendingIntent = {},
            onTogglePin = {},
        )
    }
}
