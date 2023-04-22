package no.jpro.explorer

import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*

@Component
@RestController
@RequestMapping("/api")
class ExplorerController(val conversationService: ConversationService, val explorationService: ExplorationService) {

    @CrossOrigin(origins = ["http://localhost:3000"])
    @PostMapping("/chat")
    fun chat(@RequestBody exploreRequest: String) :String{
        if(exploreRequest.isBlank() || exploreRequest == "\"\"")
            return "Please enter a question"
       return conversationService.converse(exploreRequest)

    }

    @CrossOrigin(origins = ["http://localhost:3000"])
    @PostMapping("/explore")
    fun explore(@RequestBody exploreRequest: String) : ExplorationDTO {
        if(exploreRequest.isBlank() || exploreRequest == "\"\"")
            return explorationService.default()
        return explorationService.explore(exploreRequest)
    }

    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping("/reset")
    fun reset() {
        explorationService.reset()
    }
}