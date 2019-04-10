package sk.knet.dp.generator

import com.squareup.kotlinpoet.*
import org.eclipse.jgit.api.Git
import org.springframework.web.bind.annotation.*
import java.io.File
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.stereotype.Controller
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.load.java.lazy.ContextKt.child
import java.io.InputStreamReader
import java.io.BufferedReader
import java.util.concurrent.TimeUnit


data class Prop(
        val name: String,
        val type: KClass<String> = String::class)

data class Endpoint(
        val id: String,
        var method: RequestMethod,
        var props: List<Prop>)


@Controller
@EnableResourceServer
class Generator {

    val endpointProcesses: MutableList<Process> = mutableListOf()

    init {
        registerClient()
    }


    @RequestMapping("/register", method = [RequestMethod.GET])
    final fun registerClient(): String {

        val clientName = "newmodel"


        val n = Net("src/main/resources/models/$clientName.xml")

        val transitions = n.computedTransitions


        prepareShell()

        val classFile = generateClass(transitions)
        writeClass(classFile, clientName)


        return "done"

    }

    fun generateClass(transitions: List<ComputedTransition>): String {
        val clientName = "Client"
        val endpointGET = setOf(
                Endpoint("Endpoint", RequestMethod.GET, listOf(Prop("name"), Prop("name2"))),
                Endpoint("Endpoint2", RequestMethod.GET, listOf(Prop("name"), Prop("Fico"))),
                Endpoint("Endpoint3", RequestMethod.GET, listOf(Prop("name")))
        )


        val functions = endpointGET.map { endpoint ->

            val fs = FunSpec.builder(endpoint.id)
                    .addAnnotation(AnnotationSpec.builder(GetMapping::class)
                            .addMember("\"$clientName/${endpoint.id}\"")
                            .build())
                    .addStatement("return \"Helloworld\"")
            endpoint.props.forEach { param ->
                val par = ParameterSpec
                        .builder(param.name, param.type)
                        .addAnnotation(RequestParam::class)
                        .build()
                fs.addParameter(par)

            }

            fs.build()
        }


        val endpointPOST = transitions.map {
            Endpoint("${it.label.value}POST", RequestMethod.POST, it.data.map { itt ->
                Prop(itt.id)
            })
        }

        val functions2 = endpointPOST.map { endpoint ->

            val fs = FunSpec.builder(endpoint.id)
                    .addAnnotation(AnnotationSpec.builder(PostMapping::class)
                            .addMember("\"$clientName/${endpoint.id}\"")
                            .build())
                    .addStatement("return \"Helloworld\"")
            endpoint.props.forEach { param ->
                val par = ParameterSpec
                        .builder(param.name, param.type)
                        .addAnnotation(RequestParam::class)
                        .build()
                fs.addParameter(par)

            }

            fs.build()
        }


        val newClass = TypeSpec.classBuilder(clientName)
                .addFunctions(functions.union(functions2))
                .addAnnotation(RestController::class)
                .addAnnotation(EnableResourceServer::class)
                .build()

        val fileSpec = FileSpec.builder("sk.knet.dp.endpointshell", clientName)
                .addType(newClass)

        return fileSpec.build().toString()
    }


    fun prepareShell() {


        try {
            val localRepo = FileRepository("./endpoint-shell/.git")
            val git = Git(localRepo)

            val pullCmd = git.pull()
            pullCmd.call()


        } catch (ex: GitAPIException) {
            File("./endpoint-shell").deleteRecursively()
            Git.cloneRepository()
                    .setURI("git@github.com:TheYurry/dp_endpoint.git")
                    .setDirectory(File("./endpoint-shell"))
                    .setBranch("master")
                    .call()

        }


    }


    fun writeClass(classString: String, clientName: String) {
        File("./endpoint-shell/src/main/kotlin/sk/knet/dp/endpointshell/$clientName.kt")
                .writeText(classString)

        endpointProcesses.map {
            println("Killing process: $it")
            it.destroyForcibly()
        }
        endpointProcesses.removeAll { true }


        File("./endpoint-shell/gradlew").setExecutable(true)
        for (i in 2..4) {
            println("building: $i")
            var ps = Runtime.getRuntime()
                    .exec("./gradlew build", null, File("./endpoint-shell"))
            ps.waitFor()
//            print(BufferedReader(InputStreamReader(ps.errorStream)).readLines())
//            print(BufferedReader(InputStreamReader(ps.inputStream)).readLines())

            ps = Runtime.getRuntime()
                    .exec("./gradlew bootrun -Pargs=--spring.main.banner-mode=off,--server.port=808$i", null, File("./endpoint-shell"))
//            ps.waitFor(30,TimeUnit.SECONDS)
//            print(BufferedReader(InputStreamReader(ps.errorStream)).readLines())
//            print(BufferedReader(InputStreamReader(ps.inputStream)).readLines())
            endpointProcesses.add(ps)

        }


    }


}


