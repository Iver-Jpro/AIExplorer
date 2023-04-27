package no.jpro.explorer

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@OptIn(BetaOpenAI::class)
@Component
class ExplorationService(
    @Value("\${apiKey}")
    private val apiKey: String
) {
    private val openAI: OpenAI = OpenAI(apiKey)

    @OptIn(BetaOpenAI::class)
    private val messages = mutableListOf<ChatMessage>()
    private var imageUrl = ""
    private var locationOptions = listOf<String>()

    init {
        initializeMessages()
    }

    fun explore(exploreRequest: String): ExplorationDTO {

        val exploration = privateExploration(exploreRequest)
        return exploration

    }

    fun reset() {
        messages.clear()
        imageUrl=""
        locationOptions = listOf<String>()
        initializeMessages()
    }

    @OptIn(BetaOpenAI::class)
    private fun privateExploration(exploreRequest: String): ExplorationDTO {
        try {
            val newMessage = ChatMessage(
                role = ChatRole.User,
                content = exploreRequest
            )
            val requestMessages= messages.toMutableList()
            requestMessages.add(newMessage)

            val completionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = requestMessages

            )

            val completion: ChatCompletion = chatCompletion(completionRequest)

            val responsMessage = completion.choices.first().message

            if (responsMessage == null) {
                throw Exception("No response from GPT")
            }


            val explorationDTO = processResponse(responsMessage?.content)

            imageUrl = explorationDTO.imageUrl
            locationOptions = explorationDTO.nextLocations

            messages.add(newMessage)
            messages.add(responsMessage)

            return explorationDTO
        } catch (e: Exception) {
            return ExplorationDTO(
                "Something went wrong, try a different option",
                imageUrl,
                locationOptions
            )
        }

    }

    private fun initializeMessages() {
        messages.add(
            ChatMessage(
                role = ChatRole.User,
                content = "Let's play a game, where we explore a location together! I will tell you where we start, and you will give me a description of that location and a list of 4 places we can go from there. I will then tell you where I go, and you will describe that location and 4 places to go. Format the output like this JSON: \n" +
                        "{\n" +
                        "  \"description\": \"Description of the location\",\n" +
                        "  \"nextLocations\": [\n" +
                        "    \"next location 1\",\n" +
                        "    \"next location 2\",\n" +
                        "    \"next location 3\",\n" +
                        "    \"next location 4\"\n" +
                        "  ]\n" +
                        "}\n" +
                        "When we're playing only give me the JSON code, no explanation or pleasantries. Continue until I say the game is over."
            )
        )
        messages.add(
            ChatMessage(
                role = ChatRole.Assistant,
                content = "Sure, I'm ready to play! Where shall we start our adventure?"
            )
        )
    }

    @OptIn(BetaOpenAI::class)
    private fun chatCompletion(completionRequest: ChatCompletionRequest) =
        runBlocking {
            return@runBlocking openAI.chatCompletion(completionRequest)
        }

    private fun processResponse(exploreResponse: String?): ExplorationDTO {
        if (exploreResponse == null) return ExplorationDTO(
            "No response from GPT",
            "https://labs.openai.com/s/el6z7PeHbgiGbQgiOmCS5oVm",
            listOf()
        )
        val chatResponse = extractJsonFromString(exploreResponse)

        val shortDescription = shortify(chatResponse.description)

        val imageUrl = generateImage(shortDescription)

        return ExplorationDTO(
            chatResponse.description,
            imageUrl.url,
            chatResponse.nextLocations
        )
    }

    @OptIn(BetaOpenAI::class)
    fun generateImage(shortDescription: String): ImageURL {
        val deferred = GlobalScope.async {
            val prompt = "3D render, realistic, $shortDescription"
            val images = openAI.imageURL(
                creation = ImageCreation(
                    prompt = prompt,
                    n = 1,
                    size = ImageSize.is1024x1024
                )
            )
            images.firstOrNull() ?: throw Exception("No image generated")
        }
        return runBlocking { deferred.await() }
    }

    @OptIn(BetaOpenAI::class)
    private fun shortify(description: String): String {
        val prompt =
            "Shorten the following description to at most 300 letters, and focus on the visual elements: " + description

        val newMessage = ChatMessage(
            role = ChatRole.User,
            content = prompt
        )
        val completionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(newMessage)

        )

        val completion: ChatCompletion = chatCompletion(completionRequest)

        val responsMessage = completion.choices.first().message
        if (responsMessage == null) {
            throw Exception("No response from GPT")
        }

        return responsMessage.content
    }

    fun extractJsonFromString(input: String): ExplorationChatDTO {
        val startIndex = input.indexOf("{")
        val endIndex = input.lastIndexOf("}")
        if (startIndex == -1 || endIndex == -1) {
            println("*********************ERROR*********************")
            println(input)
            println()
            throw Exception("No json found in response")
        }
        val jsonString = input.substring(startIndex, endIndex + 1)

        return Gson().fromJson(jsonString, ExplorationChatDTO::class.java)
    }

    fun default(): ExplorationDTO {
        return ExplorationDTO(
            "Welcome to the exploration game! Tell me where you want to go",
            "https://labs.openai.com/s/CccKoaABM4Z3gZhBuipRjtfn",
            listOf("Nowhere", "Anywhere", "Somewhere", "Everywhere")
        )

    }

}