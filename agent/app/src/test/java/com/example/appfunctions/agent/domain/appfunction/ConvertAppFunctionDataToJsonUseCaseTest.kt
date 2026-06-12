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

import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.metadata.AppFunctionAllOfTypeMetadata
import androidx.appfunctions.metadata.AppFunctionArrayTypeMetadata
import androidx.appfunctions.metadata.AppFunctionBooleanTypeMetadata
import androidx.appfunctions.metadata.AppFunctionComponentsMetadata
import androidx.appfunctions.metadata.AppFunctionIntTypeMetadata
import androidx.appfunctions.metadata.AppFunctionObjectTypeMetadata
import androidx.appfunctions.metadata.AppFunctionOneOfTypeMetadata
import androidx.appfunctions.metadata.AppFunctionReferenceTypeMetadata
import androidx.appfunctions.metadata.AppFunctionStringTypeMetadata
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConvertAppFunctionDataToJsonUseCaseTest {
    private val convertToJsonUseCase = ConvertAppFunctionDataToJsonUseCase()
    private val components = AppFunctionComponentsMetadata(emptyMap())

    @Test
    fun invoke_objectResponseWithPrimitives_formatsCorrectly() {
        val objectType =
            AppFunctionObjectTypeMetadata(
                properties =
                    mapOf(
                        "str" to AppFunctionStringTypeMetadata(false),
                        "int" to AppFunctionIntTypeMetadata(false),
                        "bool" to AppFunctionBooleanTypeMetadata(false),
                    ),
                required = listOf("str", "int", "bool"),
                qualifiedName = null,
                isNullable = false,
            )

        val data =
            AppFunctionData.Builder(objectType, components)
                .setString("str", "value")
                .setInt("int", 123)
                .setBoolean("bool", true)
                .build()

        val wrapperObjectType =
            AppFunctionObjectTypeMetadata(
                qualifiedName = "wrapper",
                properties = mapOf("root" to objectType),
                required = listOf("root"),
                isNullable = false,
            )
        val wrapperData =
            AppFunctionData.Builder(wrapperObjectType, components)
                .setAppFunctionData("root", data)
                .build()
        val jsonString = convertToJsonUseCase("root", wrapperData, objectType, components)

        val jsonObject = JSONObject(jsonString)
        assertEquals("value", jsonObject.getString("str"))
        assertEquals(123, jsonObject.getInt("int"))
        assertEquals(true, jsonObject.getBoolean("bool"))
    }

    @Test
    fun invoke_objectResponseWithArrays_formatsCorrectly() {
        val objectType =
            AppFunctionObjectTypeMetadata(
                properties =
                    mapOf(
                        "strArray" to
                            AppFunctionArrayTypeMetadata(
                                AppFunctionStringTypeMetadata(false),
                                false,
                            ),
                        "intArray" to
                            AppFunctionArrayTypeMetadata(AppFunctionIntTypeMetadata(false), false),
                    ),
                required = listOf("strArray", "intArray"),
                qualifiedName = null,
                isNullable = false,
            )

        val data =
            AppFunctionData.Builder(objectType, components)
                .setStringList("strArray", listOf("v1", "v2"))
                .setIntArray("intArray", intArrayOf(1, 2))
                .build()

        val wrapperObjectType =
            AppFunctionObjectTypeMetadata(
                qualifiedName = "wrapper",
                properties = mapOf("root" to objectType),
                required = listOf("root"),
                isNullable = false,
            )
        val wrapperData =
            AppFunctionData.Builder(wrapperObjectType, components)
                .setAppFunctionData("root", data)
                .build()
        val jsonString = convertToJsonUseCase("root", wrapperData, objectType, components)

        val jsonObject = JSONObject(jsonString)
        val strArray = jsonObject.getJSONArray("strArray")
        assertEquals("v1", strArray.getString(0))
        assertEquals("v2", strArray.getString(1))
        val intArray = jsonObject.getJSONArray("intArray")
        assertEquals(1, intArray.getInt(0))
        assertEquals(2, intArray.getInt(1))
    }

    @Test
    fun invoke_objectResponseWithNestedObject_formatsCorrectly() {
        val nestedObjectType =
            AppFunctionObjectTypeMetadata(
                properties =
                    mapOf(
                        "prop1" to AppFunctionStringTypeMetadata(false),
                    ),
                required = listOf("prop1"),
                qualifiedName = null,
                isNullable = false,
            )
        val objectType =
            AppFunctionObjectTypeMetadata(
                properties =
                    mapOf(
                        "obj" to nestedObjectType,
                    ),
                required = listOf("obj"),
                qualifiedName = null,
                isNullable = false,
            )

        val nestedData =
            AppFunctionData.Builder(nestedObjectType, components).setString("prop1", "val1").build()
        val data =
            AppFunctionData.Builder(objectType, components)
                .setAppFunctionData("obj", nestedData)
                .build()

        val wrapperObjectType =
            AppFunctionObjectTypeMetadata(
                qualifiedName = "wrapper",
                properties = mapOf("root" to objectType),
                required = listOf("root"),
                isNullable = false,
            )
        val wrapperData =
            AppFunctionData.Builder(wrapperObjectType, components)
                .setAppFunctionData("root", data)
                .build()
        val jsonString = convertToJsonUseCase("root", wrapperData, objectType, components)

        val jsonObject = JSONObject(jsonString)
        val nestedObj = jsonObject.getJSONObject("obj")
        assertEquals("val1", nestedObj.getString("prop1"))
    }

    @Test
    fun invoke_allOfResponse_formatsCorrectly() {
        val type1 =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("prop1" to AppFunctionStringTypeMetadata(false)),
                required = listOf("prop1"),
                qualifiedName = null,
                isNullable = false,
            )
        val type2 =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("prop2" to AppFunctionIntTypeMetadata(false)),
                required = listOf("prop2"),
                qualifiedName = null,
                isNullable = false,
            )
        val allOfType =
            AppFunctionAllOfTypeMetadata(
                matchAll = listOf(type1, type2),
                qualifiedName = "com.example.myapp.AllOf",
                isNullable = false,
                description = "Test AllOf",
            )
        val syntheticObjectType =
            AppFunctionObjectTypeMetadata(
                properties =
                    mapOf(
                        "prop1" to AppFunctionStringTypeMetadata(false),
                        "prop2" to AppFunctionIntTypeMetadata(false),
                    ),
                required = listOf("prop1", "prop2"),
                qualifiedName = null,
                isNullable = false,
            )
        val data =
            AppFunctionData.Builder(syntheticObjectType, components)
                .setString("prop1", "val1")
                .setInt("prop2", 123)
                .build()

        val wrapperObjectType =
            AppFunctionObjectTypeMetadata(
                qualifiedName = "wrapper",
                properties = mapOf("root" to allOfType),
                required = listOf("root"),
                isNullable = false,
            )
        val wrapperData =
            AppFunctionData.Builder(wrapperObjectType, components)
                .setAppFunctionData("root", data)
                .build()
        val jsonString = convertToJsonUseCase("root", wrapperData, allOfType, components)

        val jsonObject = JSONObject(jsonString)
        assertEquals("val1", jsonObject.getString("prop1"))
        assertEquals(123, jsonObject.getInt("prop2"))
    }

    @Test
    fun invoke_oneOfResponse_matchesCorrectly() {
        val typeA =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("propA" to AppFunctionStringTypeMetadata(false)),
                required = listOf("propA"),
                qualifiedName = "com.example.myapp.TypeA",
                isNullable = false,
            )
        val typeB =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("propB" to AppFunctionIntTypeMetadata(false)),
                required = listOf("propB"),
                qualifiedName = "com.example.myapp.TypeB",
                isNullable = false,
            )
        val oneOfType =
            AppFunctionOneOfTypeMetadata(
                matchOneOf = listOf(typeA, typeB),
                qualifiedName = "com.example.myapp.OneOf",
                isNullable = false,
                description = "Test OneOf",
            )

        // Test matching Type A

        val dataA = AppFunctionData.Builder(typeA, components).setString("propA", "valA").build()
        val syntheticObjectTypeA =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("prop" to typeA),
                required = listOf("prop"),
                qualifiedName = null,
                isNullable = false,
            )
        val rootDataA =
            AppFunctionData.Builder(syntheticObjectTypeA, components)
                .setAppFunctionData("prop", dataA)
                .build()

        val jsonStringA = convertToJsonUseCase("prop", rootDataA, oneOfType, components)
        val jsonObjectA = JSONObject(jsonStringA)
        assertEquals("valA", jsonObjectA.getString("propA"))
        assertEquals(false, jsonObjectA.has("propB"))

        // Test matching Type B

        val dataB = AppFunctionData.Builder(typeB, components).setInt("propB", 123).build()
        val syntheticObjectTypeB =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("prop" to typeB),
                required = listOf("prop"),
                qualifiedName = null,
                isNullable = false,
            )
        val rootDataB =
            AppFunctionData.Builder(syntheticObjectTypeB, components)
                .setAppFunctionData("prop", dataB)
                .build()

        val jsonStringB = convertToJsonUseCase("prop", rootDataB, oneOfType, components)
        val jsonObjectB = JSONObject(jsonStringB)
        assertEquals(123, jsonObjectB.getInt("propB"))
        assertEquals(false, jsonObjectB.has("propA"))
    }

    @Test
    fun invoke_primitiveFallback_returnsToString() {
        val stringType = AppFunctionStringTypeMetadata(false)

        val syntheticObjectType =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("str" to stringType),
                required = listOf("str"),
                qualifiedName = null,
                isNullable = false,
            )
        val data =
            AppFunctionData.Builder(syntheticObjectType, components)
                .setString("str", "value")
                .build()

        val result = convertToJsonUseCase("str", data, stringType, components)

        assertEquals(false, result.isEmpty())
    }

    @Test
    fun invoke_referenceToOneOf_resolvesAndMatches() {
        val typeA =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("propA" to AppFunctionStringTypeMetadata(false)),
                required = listOf("propA"),
                qualifiedName = "com.example.myapp.TypeA",
                isNullable = false,
            )
        val oneOfType =
            AppFunctionOneOfTypeMetadata(
                matchOneOf = listOf(typeA),
                qualifiedName = "com.example.myapp.OneOf",
                isNullable = false,
                description = "Test OneOf",
            )
        val componentsWithRef =
            AppFunctionComponentsMetadata(dataTypes = mapOf("refToOneOf" to oneOfType))
        val refType = AppFunctionReferenceTypeMetadata("refToOneOf", false)

        val dataA =
            AppFunctionData.Builder(typeA, componentsWithRef).setString("propA", "valA").build()
        val syntheticObjectType =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("prop" to typeA),
                required = listOf("prop"),
                qualifiedName = null,
                isNullable = false,
            )
        val rootData =
            AppFunctionData.Builder(syntheticObjectType, componentsWithRef)
                .setAppFunctionData("prop", dataA)
                .build()

        val jsonString = convertToJsonUseCase("prop", rootData, refType, componentsWithRef)
        val jsonObject = JSONObject(jsonString)
        assertEquals("valA", jsonObject.getString("propA"))
    }

    @Test
    fun invoke_oneOfWithReference_resolvesAndMatches() {
        val typeA =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("propA" to AppFunctionStringTypeMetadata(false)),
                required = listOf("propA"),
                qualifiedName = "com.example.myapp.TypeA",
                isNullable = false,
            )
        val componentsWithRef = AppFunctionComponentsMetadata(dataTypes = mapOf("refToA" to typeA))
        val refType = AppFunctionReferenceTypeMetadata("refToA", false)
        val oneOfType =
            AppFunctionOneOfTypeMetadata(
                matchOneOf = listOf(refType),
                qualifiedName = "com.example.myapp.OneOf",
                isNullable = false,
                description = "Test OneOf",
            )

        val dataA =
            AppFunctionData.Builder(typeA, componentsWithRef).setString("propA", "valA").build()
        val syntheticObjectType =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("prop" to typeA),
                required = listOf("prop"),
                qualifiedName = null,
                isNullable = false,
            )
        val rootData =
            AppFunctionData.Builder(syntheticObjectType, componentsWithRef)
                .setAppFunctionData("prop", dataA)
                .build()

        val jsonString = convertToJsonUseCase("prop", rootData, oneOfType, componentsWithRef)
        val jsonObject = JSONObject(jsonString)
        assertEquals("valA", jsonObject.getString("propA"))
    }

    @Test
    fun invoke_allOfWithReference_mergesProperties() {
        val type1 =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("prop1" to AppFunctionStringTypeMetadata(false)),
                required = listOf("prop1"),
                qualifiedName = "com.example.myapp.Type1",
                isNullable = false,
            )
        val type2 =
            AppFunctionObjectTypeMetadata(
                properties = mapOf("prop2" to AppFunctionIntTypeMetadata(false)),
                required = listOf("prop2"),
                qualifiedName = "com.example.myapp.Type2",
                isNullable = false,
            )
        val componentsWithRef = AppFunctionComponentsMetadata(dataTypes = mapOf("refTo1" to type1))
        val refType = AppFunctionReferenceTypeMetadata("refTo1", false)
        val allOfType =
            AppFunctionAllOfTypeMetadata(
                matchAll = listOf(refType, type2),
                qualifiedName = "com.example.myapp.AllOf",
                isNullable = false,
                description = "Test AllOf",
            )

        val syntheticObjectType =
            AppFunctionObjectTypeMetadata(
                properties =
                    mapOf(
                        "prop1" to AppFunctionStringTypeMetadata(false),
                        "prop2" to AppFunctionIntTypeMetadata(false),
                    ),
                required = listOf("prop1", "prop2"),
                qualifiedName = null,
                isNullable = false,
            )
        val data =
            AppFunctionData.Builder(syntheticObjectType, componentsWithRef)
                .setString("prop1", "val1")
                .setInt("prop2", 123)
                .build()

        val wrapperObjectType =
            AppFunctionObjectTypeMetadata(
                qualifiedName = "wrapper",
                properties = mapOf("root" to allOfType),
                required = listOf("root"),
                isNullable = false,
            )
        val wrapperData =
            AppFunctionData.Builder(wrapperObjectType, componentsWithRef)
                .setAppFunctionData("root", data)
                .build()
        val jsonString = convertToJsonUseCase("root", wrapperData, allOfType, componentsWithRef)
        val jsonObject = JSONObject(jsonString)
        assertEquals("val1", jsonObject.getString("prop1"))
        assertEquals(123, jsonObject.getInt("prop2"))
    }
}
