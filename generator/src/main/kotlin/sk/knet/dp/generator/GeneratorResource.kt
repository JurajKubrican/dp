package sk.knet.dp.generator

import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@EnableResourceServer
class GeneratorResource {

    @Autowired
    lateinit var fileStorage: FileStorage

    @Autowired
    lateinit var generator: Generator

    @ResponseBody
    @PostMapping("/")
    final fun registerClient(
            @RequestParam(value = "model") modelFile: MultipartFile,
            @RequestParam(value = "users") usersFile: MultipartFile,
            @RequestParam(value = "netId") netId: String): String {

        fileStorage.store(usersFile, "Users$netId")
        fileStorage.store(modelFile, "Net$netId")
        generator.generateNets()

        return "Done"

    }

    @ResponseBody
    @DeleteMapping("/")
    final fun deleteClient(
            @ApiParam(name = "netId", example = "netID123")
            @RequestParam(value = "netId") netId: String): String {

        fileStorage.delete("Users$netId")
        fileStorage.delete("Net$netId")
        generator.generateNets()

        return "Done"

    }

    @ResponseBody
    @GetMapping("/")
    final fun getClient(): List<String>? {

        return fileStorage.listDir()
                ?.filter {
                    Regex("^Net.*").matches(it.fileName.toString())
                }
                ?.map {
                    it.fileName.toString().substring(3)
                }
    }


}


