package sk.knet.dp.generator

import com.squareup.kotlinpoet.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.springframework.web.bind.annotation.*
import java.io.File
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.internal.storage.file.FileRepository
import sk.knet.dp.petriflow.Document
import javax.xml.bind.JAXBContext


data class Prop(
        val name: String
)

data class Endpoint(
        val id: String,
        var method: RequestMethod,
        var props: List<Prop>)


@RestController
class Generator {


    @RequestMapping("/register", method = [RequestMethod.GET])
    fun registerClient(): String {

        val clientName = "newmodel"


        val file = File("src\\main\\resources\\models\\$clientName.xml")
        val jaxbContext = JAXBContext.newInstance(Document::class.java)

        val jaxbUnmarshaller = jaxbContext.createUnmarshaller()
        val document = jaxbUnmarshaller.unmarshal(file) as Document

        val n = Net(document)

        val transitions = n.computedTransitions


//        prepareShell()

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
                        .builder(param.name, String::class)
                        .addAnnotation(RequestParam::class)
                        .build()
                fs.addParameter(par)

            }

            fs.build()
        }


        val endpointPOST = transitions.map {
            Endpoint("${it.label.value}POST", RequestMethod.POST, it.data.map { Prop(it.id) })
        }

        val functions2 = endpointPOST.map { endpoint ->

            val fs = FunSpec.builder(endpoint.id)
                    .addAnnotation(AnnotationSpec.builder(PostMapping::class)
                            .addMember("\"$clientName/${endpoint.id}\"")
                            .build())
                    .addStatement("return", "Hello, world")
            endpoint.props.forEach { param ->
                val par = ParameterSpec
                        .builder(param.name, String::class)
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
            pullCmd.setCredentialsProvider(UsernamePasswordCredentialsProvider("kubrican.juraj@gmail.com", "&7fjCFy!H7b5hUeV"))
            pullCmd.call()


        } catch (ex: GitAPIException) {
            File("./endpoint-shell").deleteRecursively()
            Git.cloneRepository()
                    .setCredentialsProvider(UsernamePasswordCredentialsProvider("kubrican.juraj@gmail.com", "&7fjCFy!H7b5hUeV"))
                    .setURI("https://gitlab.interes.group/kubrican.juraj/endpoint-shell.git")
                    .setDirectory(File("./endpoint-shell"))
                    .setBranch("master")
                    .call()

        }


//


    }


    fun writeClass(classString: String, clientName: String) {
        File("./endpoint-shell/src/main/kotlin/sk/knet/dp/endpointshell/$clientName.kt")
                .writeText(classString)

        println(File("./endpoint-shell/gradlew").setExecutable(true))
//        val ps = Runtime.getRuntime()
//                .exec("./gradlew bootrun", null, File("./endpoint-shell"))


//        ps.waitFor()
//        println(ps.inputStream.bufferedReader().use(BufferedReader::readText))
//        println(ps.errorStream.bufferedReader().use(BufferedReader::readText))

    }
}
