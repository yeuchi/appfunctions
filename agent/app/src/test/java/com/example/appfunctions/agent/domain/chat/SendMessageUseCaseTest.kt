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
package com.example.appfunctions.agent.domain.chat

import com.example.appfunctions.agent.data.ChatRepository
import com.example.appfunctions.agent.data.db.entities.MessageProcessingStatus
import com.example.appfunctions.agent.data.db.entities.MessageRole
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SendMessageUseCaseTest {
    private lateinit var useCase: SendMessageUseCase
    private val chatRepository = mockk<ChatRepository>(relaxed = true)

    @Before
    fun setUp() {
        useCase = SendMessageUseCase(chatRepository)
    }

    @Test
    fun invoke_sendsMessage() =
        runTest {
            val threadId = "thread_1"
            val role = MessageRole.USER
            val textContent = "Hello"
            val status = MessageProcessingStatus.PROCESSED

            useCase(threadId, role, textContent, status)

            coVerify {
                chatRepository.sendMessage(
                    match {
                        it.threadId == threadId &&
                            it.role == role &&
                            it.textContent == textContent &&
                            it.processingStatus == status
                    },
                )
            }
        }
}
