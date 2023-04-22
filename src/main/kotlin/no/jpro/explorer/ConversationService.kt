package no.jpro.explorer

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ConversationService(
    @Value("\${apiKey}")
    val apiKey: String
) {
    val openAI: OpenAI = OpenAI(apiKey)

    @OptIn(BetaOpenAI::class)
    private val messages = mutableListOf<ChatMessage>()

    fun converse(exploreRequest: String): String {

        var response = "No response"

        runBlocking {
            response = privateConversation(exploreRequest)
        }

        return response
    }

    @OptIn(BetaOpenAI::class)
    private suspend fun privateConversation(exploreRequest: String): String {
        val newMessage = ChatMessage(
            role = ChatRole.User,
            content = exploreRequest
        )
        messages.add(newMessage)

        val completionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = messages

        )

        val completion: ChatCompletion = openAI.chatCompletion(completionRequest)

        val responsMessage = completion.choices.first().message
        if (responsMessage != null) {
            messages.add(responsMessage)
        }
        return responsMessage?.content.toString()
    }
}