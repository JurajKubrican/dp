package sk.knet.dp.generator

import com.squareup.kotlinpoet.*
import io.swagger.annotations.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.springframework.web.bind.annotation.*
import java.io.File
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import sk.knet.dp.petriflow.Behavior
import sk.knet.dp.petriflow.Role
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.load.java.lazy.ContextKt.child
import java.io.InputStreamReader
import java.io.BufferedReader
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.security.RolesAllowed


data class Prop(
        val name: String,
        val required: Boolean)

data class Endpoint(
        val id: String,
        var method: RequestMethod,
        var props: List<Prop>,
        var roles: List<String>,
        val label: String)


@Controller
@EnableResourceServer
class Generator {

    val endpointProcesses: MutableList<Process> = mutableListOf()

    @Autowired
    lateinit var fileStorage: FileStorage


    @PostMapping("/register")
    final fun registerClient(
            @RequestParam(value = "uploadfile") file: MultipartFile,
            @RequestParam(value = "clientname") clientName: String,
            model: Model): String {


        val tmpFilnename = fileStorage.store(file)

        val n = Net("filestorage/$tmpFilnename")

        val transitions = n.computedTransitions

        val roles = n.roles

        registerRoles(roles)

        prepareShell()

        val classFile = generateClass(transitions, clientName, roles)
        writeClass(classFile, clientName)


        return "done"

    }


    fun registerRoles(roles: List<Role>) {


//        val restTemplate = RestTemplate()
//        val role = restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", String::class.java)
//        URL("http://localhost:8088/clearRoles").readText()
//        roles.map {
//            URL("http://localhost:8088/addRole?role=").readText()
//        }

    }


    fun generatePostFunctions(transitions: List<ComputedTransition>, className: String): List<FunSpec> {
        val endpointPOST = transitions.map {
            val props = it.data
                    .filter { itt -> itt.logic.behavior.contains(Behavior.EDITABLE) }
                    .map { itt ->
                        Prop(itt.id,
                                itt.logic.behavior.contains(Behavior.EDITABLE) && itt.logic.behavior.contains(Behavior.REQUIRED))
                    }

            val roles = it.rolesPerform.map { itt ->
                "ROLE_${className.toUpperCase()}_${itt.toUpperCase()}"
            }.toList()

            Endpoint(it.id, RequestMethod.POST, props, roles, it.label.value)
        }
                .filter { it.props.isNotEmpty() }

        return endpointPOST.map { endpoint ->

            val rolesAnnotation = AnnotationSpec.builder(RolesAllowed::class)
            endpoint.roles.forEach {
                rolesAnnotation.addMember("\"$it\"")
            }
            val descriprionAnnotation = AnnotationSpec.builder(ApiOperation::class)
            descriprionAnnotation.addMember("value = \"${endpoint.label}\"")
            descriprionAnnotation.addMember("notes = \"Allowed roles: ${endpoint.roles}\"")


            val fs = FunSpec.builder("post${endpoint.id}")
                    .addAnnotation(AnnotationSpec.builder(PostMapping::class)
                            .addMember("\"$className/${endpoint.id}\"")
                            .build())
                    .addAnnotation(rolesAnnotation.build())
                    .addAnnotation(descriprionAnnotation.build())
                    .addStatement("print( \"not implemented\")")
            endpoint.props.forEach { param ->
                val ann = AnnotationSpec.builder(RequestParam::class)
                        .addMember("value = \"${param.name}\"")
                if (!param.required) {
                    ann.addMember("defaultValue = \"\"")
                }
                val par = ParameterSpec
                        .builder(param.name, String::class)
                        .addAnnotation(ann.build())
                        .build()
                fs.addParameter(par)

            }
            fs.build()
        }
    }


    fun generateGetEndpoints(transitions: List<ComputedTransition>, className: String): Pair<List<FunSpec>, List<TypeSpec>> {
        val endpointGET = transitions.map {
            val props = it.data
                    .filter { itt -> itt.logic.behavior.contains(Behavior.VISIBLE) }
                    .map { itt -> Prop(itt.id, false) }

            val roles = it.rolesView.map { itt ->
                "ROLE_${className.toUpperCase()}_${itt.toUpperCase()}"
            }.toList()

            Endpoint(it.id, RequestMethod.POST, props, roles, it.label.value)
        }
                .filter { it.props.isNotEmpty() }

        val result = endpointGET.map { endpoint ->
            val returnObjectName = "get${endpoint.id}Result"
            val returnObjectParams = endpoint.props.map {
                ParameterSpec
                        .builder(it.name, String::class)
                        .defaultValue("\"\"")
                        .build()
            }

            val obj = TypeSpec.classBuilder(returnObjectName)
                    .primaryConstructor(FunSpec.constructorBuilder()
                            .addParameters(returnObjectParams)
                            .build())
                    .build()

            val rolesAnnotation = AnnotationSpec.builder(RolesAllowed::class)
            endpoint.roles.forEach {
                rolesAnnotation.addMember("\"$it\"")
            }
            val descriprionAnnotation = AnnotationSpec.builder(ApiOperation::class)
            descriprionAnnotation.addMember("value = \"${endpoint.label}\"")
            descriprionAnnotation.addMember("notes = \"Allowed roles: ${endpoint.roles}\"")


            val fs = FunSpec.builder("get${endpoint.id}")
                    .addAnnotation(AnnotationSpec.builder(GetMapping::class)
                            .addMember("\"$className/${endpoint.id}\"")
                            .build())
                    .addAnnotation(rolesAnnotation.build())
                    .addAnnotation(descriprionAnnotation.build())
                    .addStatement("return $returnObjectName()")


            Pair(fs.build(), obj)
        }



        return Pair(result.map { it.first }, result.map { it.second })
    }


    fun generateClass(
            transitions: List<ComputedTransition>,
            className: String,
            roles: List<Role>
    ): String {
        val endpointsGET = generateGetEndpoints(transitions, className)


        val postFunctions = generatePostFunctions(transitions, className)


        val newClass = TypeSpec.classBuilder(className)
                .addFunctions(postFunctions.union(endpointsGET.first))
                .addTypes(endpointsGET.second)
                .addAnnotation(RestController::class)
                .addAnnotation(EnableResourceServer::class)
                .build()

        val fileSpec = FileSpec.builder("sk.knet.dp.endpointshell", className)
                .addType(newClass)

        return fileSpec.build().toString()
    }


    fun prepareShell() {


        try {
            val repo = Git(FileRepository("./endpoint-shell/.git"))
            repo.add().call()
            repo.reset()
                    .setMode(ResetCommand.ResetType.HARD)
                    .setRef("origin/master").call()

        } catch (ex: GitAPIException) {
            File("./endpoint-shell").deleteRecursively()
            Git.cloneRepository()
                    .setURI("https://github.com/TheYurry/dp_endpoint.git")
                    .setDirectory(File("./endpoint-shell"))
                    .setBranch("master").call()
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
        for (i in 2..2) {
            println("building: $i")
            var ps = Runtime.getRuntime()
                    .exec("./gradlew build", null, File("./endpoint-shell"))
            ps.waitFor()
            print(BufferedReader(InputStreamReader(ps.inputStream)).readLines())
            print(BufferedReader(InputStreamReader(ps.errorStream)).readLines())

            ps = Runtime.getRuntime()
                    .exec("./gradlew bootrun -Pargs=--spring.main.banner-mode=off,--server.port=808$i", null, File("./endpoint-shell"))
            print(BufferedReader(InputStreamReader(ps.errorStream)).readLines())
            print(BufferedReader(InputStreamReader(ps.inputStream)).readLines())
            ps.waitFor(10, TimeUnit.SECONDS)
            endpointProcesses.add(ps)

        }


    }

}


