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
package com.example.appfunctions.agent.domain

import com.example.appfunctions.agent.data.LlmModel
import com.example.appfunctions.agent.data.LlmProviderName
import com.example.appfunctions.agent.data.SettingsRepository
import com.example.appfunctions.agent.data.db.entities.MessageEntity
import com.example.appfunctions.agent.data.db.entities.MessageProcessingStatus
import com.example.appfunctions.agent.data.db.entities.MessageRole
import com.example.appfunctions.agent.data.db.entities.ThreadEntity
import com.example.appfunctions.agent.domain.appfunction.ConvertInputToAppFunctionDataUseCase
import com.example.appfunctions.agent.domain.appfunction.ExecuteAppFunctionUseCase
import com.example.appfunctions.agent.domain.appfunction.GetAppFunctionsUseCase
import com.example.appfunctions.agent.domain.chat.ManageThreadsUseCase
import com.example.appfunctions.agent.domain.chat.ObservePendingMessagesUseCase
import com.example.appfunctions.agent.domain.chat.SendMessageUseCase
import com.example.appfunctions.agent.domain.chat.UpdateMessageParams
import com.example.appfunctions.agent.domain.chat.UpdateMessageUseCase
import com.example.appfunctions.agent.domain.chat.UpdateThreadParams
import com.example.appfunctions.agent.domain.chat.UpdateThreadUseCase
import com.example.appfunctions.agent.domain.pendingintent.SavePendingIntentUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AgentOrchestratorTest {
    private val observePendingMessagesUseCase: ObservePendingMessagesUseCase = mockk()
    private val updateMessageUseCase: UpdateMessageUseCase = mockk(relaxed = true)
    private val updateThreadUseCase: UpdateThreadUseCase = mockk(relaxed = true)
    private val manageThreadsUseCase: ManageThreadsUseCase = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk()
    private val llmProviderFactory: LlmProviderFactory = mockk()
    private val getAppFunctionsUseCase: GetAppFunctionsUseCase = mockk()
    private val executeAppFunctionUseCase: ExecuteAppFunctionUseCase = mockk()
    private val sendMessageUseCase: SendMessageUseCase = mockk(relaxed = true)
    private val convertInputToAppFunctionDataUseCase: ConvertInputToAppFunctionDataUseCase = mockk()
    private val savePendingIntentUseCase: SavePendingIntentUseCase = mockk(relaxed = true)

    private lateinit var agentOrchestrator: AgentOrchestrator

    @Before
    fun setUp() {
        agentOrchestrator =
            AgentOrchestrator(
                manageThreadsUseCase = manageThreadsUseCase,
                observePendingMessagesUseCase = observePendingMessagesUseCase,
                sendMessageUseCase = sendMessageUseCase,
                updateMessageUseCase = updateMessageUseCase,
                updateThreadUseCase = updateThreadUseCase,
                llmProviderFactory = llmProviderFactory,
                settingsRepository = settingsRepository,
                getAppFunctionsUseCase = getAppFunctionsUseCase,
                convertInputToAppFunctionDataUseCase = convertInputToAppFunctionDataUseCase,
                executeAppFunctionUseCase = executeAppFunctionUseCase,
                savePendingIntentUseCase = savePendingIntentUseCase,
            )
    }

    @Test
    fun `observeAndProcessMessages fails when API key is missing`() =
        runTest {
            val threadId = "thread_1"
            val message =
                MessageEntity(
                    messageId = "msg_1",
                    threadId = threadId,
                    role = MessageRole.USER,
                    textContent = "Hello",
                    timestamp = System.currentTimeMillis(),
                    processingStatus = MessageProcessingStatus.PENDING_AGENT_RESPONSE,
                )
            val thread =
                ThreadEntity(
                    threadId = threadId,
                    createdAt = System.currentTimeMillis(),
                    llmModel = LlmModel.GEMINI_3_FLASH_PREVIEW,
                    latestInteractionId = null,
                )
            coEvery { observePendingMessagesUseCase(threadId) } returns
                flow {
                    delay(10)
                    emit(message)
                }
            coEvery { manageThreadsUseCase.getThread(threadId) } returns flowOf(thread)
            coEvery { settingsRepository.geminiApiKey } returns flowOf(null)
            coEvery { settingsRepository.disconnectedApps } returns flowOf(emptySet())

            agentOrchestrator.observeAndProcessMessages(threadId)

            // Verify behavior (state)
            assertEquals(AgentStatus.Idle, agentOrchestrator.status.value)

            // Verify interactions
            coVerify {
                updateMessageUseCase(
                    message.messageId,
                    UpdateMessageParams(status = MessageProcessingStatus.PROCESSED),
                )
                sendMessageUseCase(
                    threadId = threadId,
                    role = MessageRole.ASSISTANT,
                    textContent = "API key is missing for GEMINI",
                    processingStatus = MessageProcessingStatus.FAILED,
                )
            }
        }

    @Test
    fun `observeAndProcessMessages fails when LLM returns error`() =
        runTest {
            val threadId = "thread_1"
            val message =
                MessageEntity(
                    messageId = "msg_1",
                    threadId = threadId,
                    role = MessageRole.USER,
                    textContent = "Hello",
                    timestamp = System.currentTimeMillis(),
                    processingStatus = MessageProcessingStatus.PENDING_AGENT_RESPONSE,
                )
            val thread =
                ThreadEntity(
                    threadId = threadId,
                    createdAt = System.currentTimeMillis(),
                    llmModel = LlmModel.GEMINI_3_FLASH_PREVIEW,
                    latestInteractionId = null,
                )
            val llmProvider = mockk<LlmProvider>()

            coEvery { observePendingMessagesUseCase(threadId) } returns
                flow {
                    delay(10)
                    emit(message)
                }
            coEvery { manageThreadsUseCase.getThread(threadId) } returns flowOf(thread)
            coEvery { settingsRepository.geminiApiKey } returns flowOf("dummy_key")
            coEvery { settingsRepository.disconnectedApps } returns flowOf(emptySet())
            coEvery { llmProviderFactory.getProvider(LlmProviderName.GEMINI) } returns llmProvider
            coEvery { getAppFunctionsUseCase() } returns flowOf(emptyMap())

            val errorMsg = "LLM failed"
            coEvery { llmProvider.generateResponse(any(), any(), any(), any(), any()) } returns
                LlmResponse.Error(errorMsg)

            agentOrchestrator.observeAndProcessMessages(threadId)

            // Verify behavior (state)
            assertEquals(AgentStatus.Idle, agentOrchestrator.status.value)

            // Verify interactions
            coVerify {
                updateMessageUseCase(
                    message.messageId,
                    UpdateMessageParams(status = MessageProcessingStatus.PROCESSED),
                )
                sendMessageUseCase(
                    threadId = threadId,
                    role = MessageRole.ASSISTANT,
                    textContent = errorMsg,
                    processingStatus = MessageProcessingStatus.FAILED,
                )
            }
        }

    @Test
    fun `observeAndProcessMessages succeeds when LLM returns text`() =
        runTest {
            val threadId = "thread_1"
            val message =
                MessageEntity(
                    messageId = "msg_1",
                    threadId = threadId,
                    role = MessageRole.USER,
                    textContent = "Hello",
                    timestamp = System.currentTimeMillis(),
                    processingStatus = MessageProcessingStatus.PENDING_AGENT_RESPONSE,
                )
            val thread =
                ThreadEntity(
                    threadId = threadId,
                    createdAt = System.currentTimeMillis(),
                    llmModel = LlmModel.GEMINI_3_FLASH_PREVIEW,
                    latestInteractionId = null,
                )
            val llmProvider = mockk<LlmProvider>()

            coEvery { observePendingMessagesUseCase(threadId) } returns
                flow {
                    delay(10)
                    emit(message)
                }
            coEvery { manageThreadsUseCase.getThread(threadId) } returns flowOf(thread)
            coEvery { settingsRepository.geminiApiKey } returns flowOf("dummy_key")
            coEvery { settingsRepository.disconnectedApps } returns flowOf(emptySet())
            coEvery { llmProviderFactory.getProvider(LlmProviderName.GEMINI) } returns llmProvider
            coEvery { getAppFunctionsUseCase() } returns flowOf(emptyMap())

            val responseText = "Hi there"
            coEvery { llmProvider.generateResponse(any(), any(), any(), any(), any()) } returns
                LlmResponse.Success("interaction_123", listOf(LlmResponsePart.Text(responseText)))

            agentOrchestrator.observeAndProcessMessages(threadId)

            // Verify behavior (state)
            assertEquals(AgentStatus.Idle, agentOrchestrator.status.value)

            // Verify interactions
            coVerify {
                updateThreadUseCase(threadId, UpdateThreadParams(interactionId = "interaction_123"))
                sendMessageUseCase(
                    threadId = threadId,
                    role = MessageRole.ASSISTANT,
                    textContent = responseText,
                    processingStatus = MessageProcessingStatus.PROCESSED,
                )
                updateMessageUseCase(
                    message.messageId,
                    UpdateMessageParams(status = MessageProcessingStatus.PROCESSED),
                )
            }
        }
}
