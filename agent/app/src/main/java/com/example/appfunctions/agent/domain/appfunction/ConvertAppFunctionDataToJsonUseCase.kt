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

import android.util.Log
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.metadata.AppFunctionAllOfTypeMetadata
import androidx.appfunctions.metadata.AppFunctionArrayTypeMetadata
import androidx.appfunctions.metadata.AppFunctionBooleanTypeMetadata
import androidx.appfunctions.metadata.AppFunctionComponentsMetadata
import androidx.appfunctions.metadata.AppFunctionDataTypeMetadata
import androidx.appfunctions.metadata.AppFunctionDoubleTypeMetadata
import androidx.appfunctions.metadata.AppFunctionFloatTypeMetadata
import androidx.appfunctions.metadata.AppFunctionIntTypeMetadata
import androidx.appfunctions.metadata.AppFunctionLongTypeMetadata
import androidx.appfunctions.metadata.AppFunctionObjectTypeMetadata
import androidx.appfunctions.metadata.AppFunctionOneOfTypeMetadata
import androidx.appfunctions.metadata.AppFunctionReferenceTypeMetadata
import androidx.appfunctions.metadata.AppFunctionStringTypeMetadata
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/** Use case to convert [AppFunctionData] to a pretty-printed JSON string based on metadata. */
class ConvertAppFunctionDataToJsonUseCase
    @Inject
    constructor() {
        /**
         * Converts a specific property within [AppFunctionData] to a pretty-printed JSON string.
         *
         * @param name The name of the property to start conversion from.
         * @param data The [AppFunctionData] containing the property.
         * @param dataType The metadata describing the data type of the property.
         * @param components The component metadata for resolving references.
         * @return A pretty-printed JSON string representation of the data.
         */
        operator fun invoke(
            name: String,
            data: AppFunctionData,
            dataType: AppFunctionDataTypeMetadata,
            components: AppFunctionComponentsMetadata,
        ): String {
            val value = getValueFromData(data, name, dataType, components)
            val indentSpaces = 2
            return when (value) {
                is JSONObject -> value.toString(indentSpaces)
                is JSONArray -> value.toString(indentSpaces)
                null -> "null"
                else -> value.toString()
            }
        }

        private fun convertToJson(
            data: AppFunctionData,
            dataType: AppFunctionDataTypeMetadata,
            components: AppFunctionComponentsMetadata,
        ): Any {
            return when (dataType) {
                is AppFunctionObjectTypeMetadata -> {
                    val jsonObject = JSONObject()
                    for ((propName, propType) in dataType.properties) {
                        try {
                            val propValue = getValueFromData(data, propName, propType, components)
                            if (propValue != null) {
                                jsonObject.put(propName, propValue)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Unable to parse $propName in ${dataType.qualifiedName}", e)
                        }
                    }
                    jsonObject
                }
                is AppFunctionAllOfTypeMetadata -> {
                    val jsonObject = JSONObject()
                    for (type in dataType.matchAll) {
                        val typeJson = convertToJson(data, type, components)
                        if (typeJson is JSONObject) {
                            val keys = typeJson.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                jsonObject.put(key, typeJson.get(key))
                            }
                        }
                    }
                    jsonObject
                }
                is AppFunctionOneOfTypeMetadata -> {
                    val dataQualifiedName = data.qualifiedName
                    val matchingType =
                        dataType.matchOneOf
                            .map { candidate ->
                                if (candidate is AppFunctionReferenceTypeMetadata) {
                                    components.dataTypes[candidate.referenceDataType]
                                } else {
                                    candidate
                                }
                            }
                            .find { resolvedCandidate ->
                                val candidateQualifiedName =
                                    when (resolvedCandidate) {
                                        is AppFunctionObjectTypeMetadata ->
                                            resolvedCandidate.qualifiedName
                                        is AppFunctionAllOfTypeMetadata ->
                                            resolvedCandidate.qualifiedName
                                        is AppFunctionOneOfTypeMetadata ->
                                            resolvedCandidate.qualifiedName
                                        else -> null
                                    }
                                candidateQualifiedName == dataQualifiedName
                            }

                    if (matchingType != null) {
                        convertToJson(data, matchingType, components)
                    } else {
                        JSONObject()
                            .put(
                                "__error__",
                                "Failed to find matching type for OneOf: $dataQualifiedName",
                            )
                    }
                }
                is AppFunctionReferenceTypeMetadata -> {
                    val referenceKey = dataType.referenceDataType
                    val resolvedType = components.dataTypes[referenceKey]
                    if (resolvedType != null) {
                        convertToJson(data, resolvedType, components)
                    } else {
                        JSONObject().put("__error__", "Failed to resolve reference: $referenceKey")
                    }
                }
                else -> {
                    // Fallback for primitive root types or unhandled types
                    data.toString()
                }
            }
        }

        private fun getValueFromData(
            data: AppFunctionData,
            name: String,
            dataType: AppFunctionDataTypeMetadata,
            components: AppFunctionComponentsMetadata,
        ): Any? {
            return when (dataType) {
                is AppFunctionStringTypeMetadata -> data.getString(name)
                is AppFunctionIntTypeMetadata -> data.getInt(name)
                is AppFunctionLongTypeMetadata -> data.getLong(name)
                is AppFunctionBooleanTypeMetadata -> data.getBoolean(name)
                is AppFunctionDoubleTypeMetadata -> data.getDouble(name)
                is AppFunctionFloatTypeMetadata -> data.getFloat(name)
                is AppFunctionObjectTypeMetadata,
                is AppFunctionOneOfTypeMetadata,
                is AppFunctionAllOfTypeMetadata,
                is AppFunctionReferenceTypeMetadata,
                -> {
                    val nestedData = data.getAppFunctionData(name) ?: return null
                    convertToJson(nestedData, dataType, components)
                }
                is AppFunctionArrayTypeMetadata -> {
                    val jsonArray = JSONArray()
                    when (val itemType = dataType.itemType) {
                        is AppFunctionStringTypeMetadata -> {
                            data.getStringList(name)?.forEach { jsonArray.put(it) }
                        }
                        is AppFunctionIntTypeMetadata -> {
                            data.getIntArray(name)?.forEach { jsonArray.put(it) }
                        }
                        is AppFunctionLongTypeMetadata -> {
                            data.getLongArray(name)?.forEach { jsonArray.put(it) }
                        }
                        is AppFunctionBooleanTypeMetadata -> {
                            data.getBooleanArray(name)?.forEach { jsonArray.put(it) }
                        }
                        is AppFunctionDoubleTypeMetadata -> {
                            data.getDoubleArray(name)?.forEach { jsonArray.put(it) }
                        }
                        is AppFunctionFloatTypeMetadata -> {
                            data.getFloatArray(name)?.forEach { jsonArray.put(it) }
                        }
                        is AppFunctionObjectTypeMetadata -> {
                            data.getAppFunctionDataList(name)?.forEach {
                                jsonArray.put(convertToJson(it, itemType, components))
                            }
                        }
                        is AppFunctionReferenceTypeMetadata -> {
                            val referenceKey = itemType.referenceDataType
                            val resolvedType = components.dataTypes[referenceKey]
                            if (resolvedType != null) {
                                data.getAppFunctionDataList(name)?.forEach {
                                    jsonArray.put(convertToJson(it, resolvedType, components))
                                }
                            } else {
                                jsonArray.put(
                                    JSONObject()
                                        .put("__error__", "Failed to resolve reference: $referenceKey"),
                                )
                            }
                        }
                    }
                    jsonArray
                }
                else -> null
            }
        }

        private companion object {
            const val TAG = "ConvertAppFunctionDataToJsonUseCase"
        }
    }
