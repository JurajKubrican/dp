package sk.knet.dp.generator

import com.squareup.kotlinpoet.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.springframework.web.bind.annotation.*
import java.io.File
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.internal.storage.file.FileRepository
import kotlin.reflect.KClass


data class Prop(
        val name: String,
        val type: KClass<String> = String::class)

data class Endpoint(
        val id: String,
        var method: RequestMethod,
        var props: List<Prop>)


@RestController
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
                    .addStatement("return", "Hello, world")
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
                    .addStatement("return", "Hello, world")
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

        if (System.getProperty("os.name").startsWith("Windows")) {
            val ps = Runtime.getRuntime()
                    .exec("powershell ./gradlew bootrun", null, File("./endpoint-shell"))
            endpointProcesses.add(ps)
            println("Starting process: $ps")


        } else {
            File("./endpoint-shell/gradlew").setExecutable(true)
            for (i in 2..4) {
                val ps = Runtime.getRuntime()
                        .exec("./gradlew bootrun -Pargs=--spring.main.banner-mode=off,--server.port=808$i", null, File("./endpoint-shell"))
                endpointProcesses.add(ps)
                println("Starting process: $ps")
            }

        }


    }


}
