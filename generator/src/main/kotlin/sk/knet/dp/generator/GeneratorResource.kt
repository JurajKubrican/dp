package sk.knet.dp.generator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
@EnableResourceServer
class GeneratorResource {

    @Autowired
    lateinit var fileStorage: FileStorage

    @Autowired
    lateinit var generator: Generator

    @PostMapping("/register")
    final fun registerClient(
            @RequestParam(value = "model") modelFile: MultipartFile,
            @RequestParam(value = "users") usersFile: MultipartFile,
            @RequestParam(value = "clientname") clientName: String): String {

        fileStorage.store(usersFile, "Users$clientName")
        fileStorage.store(modelFile, "Client$clientName")
        generator.generateClients()

        return "Done"

    }

}
