package sk.knet.dp.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import sk.knet.dp.petriflow.Behavior
import sk.knet.dp.petriflow.DataType
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.annotation.PostConstruct
import javax.annotation.security.RolesAllowed
import kotlin.reflect.KClass


data class Prop(
        val name: String,
        val required: Boolean,
        val type: DataType = DataType.BOOLEAN,
        val enumValues: List<String>? = emptyList())

data class Endpoint(
        val id: String,
        var method: RequestMethod,
        var props: List<Prop>,
        var roles: List<String>,
        val label: String)


const val RELAY_BUILD_DIR = "./tmp/relay_build"


@Service
class Generator {

    @Autowired
    lateinit var fileStorage: FileStorage


    @PostConstruct
    fun generateNets(): String {

        val files = fileStorage.listDir() ?: return "no Nets"
        prepareShell()

        files.filter {
            Regex("^Users.*").matches(it.fileName.toString())
        }.forEach {
            val users = UsersReader(it.toString())
            registerUsers(users.document.user, it.fileName.toString().substring(5))
        }



        files.filter {
            Regex("^Net.*").matches(it.fileName.toString())
        }.forEach {
            val netId = it.fileName.toString().substring(3)
            val n = NetReader(it.toString())
            val transitions = n.facadeTransitions
            generateClass(transitions, netId)

        }

        runEndpoint()

        return "done"

    }


    fun registerUsers(users: List<sk.knet.dp.userschema.User>, netId: String) {

        users.forEach {
            val roles = URLEncoder.encode(
                    it.role.map { itt ->
                        "${netId.toUpperCase()}_${itt.id.toUpperCase()}"
                    }.toString(),
                    StandardCharsets.UTF_8.toString())
            URL("http://localhost:8088/addUser?username=${netId}_${it.name}&password=${it.password}&roles=$roles").readText()
            print("${netId}_${it.name} - ${it.password} - $roles\"")
        }

    }


    private fun generateEmptyFunction(endpoint: Endpoint, operation: String, type: KClass<out Annotation> = PostMapping::class): FunSpec.Builder {
        // Annotations
        val rolesAnnotation = AnnotationSpec.builder(RolesAllowed::class)
        endpoint.roles
                .forEach {
                    rolesAnnotation.addMember("\"$it\"")
                }

        val descriptionAnnotation = AnnotationSpec.builder(ApiOperation::class)
        descriptionAnnotation.addMember("value = \"${endpoint.label}\"")
        descriptionAnnotation.addMember("notes = \"Allowed roles: ${endpoint.roles}\"")


        val t = ClassName("org.springframework.http", "ResponseEntity")
                .parameterizedBy(ClassName("kotlin", "String"))


        //Function itself
        val fs = FunSpec.builder("$operation${endpoint.id}")
                .addAnnotation(AnnotationSpec.builder(type)
                        .addMember("\"/{instanceId}/${endpoint.id}/$operation\"")
                        .build())
                .addAnnotation(rolesAnnotation.build())
                .addAnnotation(descriptionAnnotation.build())
                .returns(t)


        fs.addParameter(ParameterSpec
                .builder("instanceId", String::class)
                .addAnnotation(AnnotationSpec
                        .builder(PathVariable::class)
                        .addMember("\"instanceId\"")
                        .build()).build())

        return fs
    }

    private fun generateFinishFunctions(transitions: List<FacadeTransition>, className: String): List<FunSpec> {
//        Prep data
        val endpoints = transitions
                .map { transition ->
                    val props = transition.data
                            .filter { prop ->
                                prop.logic.behavior.contains(Behavior.EDITABLE)
                            }
                            .map { prop ->
                                val required = prop.logic.behavior.contains(Behavior.EDITABLE) && prop.logic.behavior.contains(Behavior.REQUIRED)

                                Prop(prop.id, required, prop.type, prop.values)
                            }

                    val roles = transition.rolesPerform
                            .map { role ->
                                "ROLE_${className.toUpperCase()}_${role.toUpperCase()}"
                            }.toList()

                    Endpoint(transition.id, RequestMethod.POST, props, roles, transition.label.value)
                }
                .filter {
                    it.props.isNotEmpty()
                }




        return endpoints
                .map { endpoint ->

                    val statements = mutableListOf(
                            "val errors = mutableListOf<String>()"
                    )

                    endpoint.props.forEach { prop ->
                        var regex: String? = null
                        when (prop.type) {
                            DataType.BOOLEAN -> regex = """0|1|true|false"""
                            DataType.TEXT -> regex = """.*"""
                            DataType.DATE -> regex = """(\\d{4})-(\\d{2})-(\\d{2})"""
                            DataType.DATE_TIME -> regex = """(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2})\\:(\\d{2})\\:(\\d{2})[+-](\\d{2})\\:(\\d{2})"""
                            DataType.NUMBER -> regex = """\\d+"""
                            DataType.ENUMERATION -> regex = prop.enumValues!!.joinToString("|")
                            DataType.MULTICHOICE -> regex = prop.enumValues!!.joinToString("|")
                            else -> {

                            }
                        }
                        if (regex != null) {
                            statements.add(""" if (${if (!prop.required) """${prop.name} !=  "" && """ else ""}!Regex("$regex").matches(${prop.name}) ) {
                                |   errors.add(""".trimMargin() + "\"\"\"${prop.name} should match $regex\"\"\" )}")
                        }
                    }

                    if (statements.isNotEmpty())


                        statements.add(""" if(errors.isNotEmpty()){
                        |  return ResponseEntity(errors.toString(), BAD_REQUEST)
                        |} """.trimMargin())

                    statements.add("""
                    |     processServerRequest.data("$className",
                    |     "${endpoint.id}",
                    |     instanceId,
                    |    mapOf(${endpoint.props.joinToString(",") { """ "${it.name}" to ${it.name} """ }}))
                    |    return ResponseEntity("", OK)
                    """.trimMargin())


                    val fs = generateEmptyFunction(endpoint, "finish")
                    statements.map { stmt ->
                        fs.addStatement(stmt)
                    }

                    endpoint.props
                            .forEach { param ->
                                val ann = AnnotationSpec.builder(RequestParam::class)
                                        .addMember("value = \"${param.name}\"")
                                if (!param.required) {
                                    ann.addMember("defaultValue = \"\"")
                                }

                                val apiAnn = AnnotationSpec
                                        .builder(ApiParam::class)
                                        .addMember("required = ${param.required}")

                                if (param.type == DataType.ENUMERATION || param.type == DataType.MULTICHOICE) {
                                    apiAnn.addMember("allowableValues = \"\"\"${param.enumValues}\"\"\"")
                                }


                                val par = ParameterSpec
                                        .builder(param.name,
                                                when (param.type) {
                                                    DataType.FILE -> MultipartFile::class
                                                    else -> String::class

                                                }
                                        ).addAnnotation(ann.build())
                                        .addAnnotation(apiAnn.build())



                                fs.addParameter(par.build())

                            }
                    fs.build()
                }
    }

    private fun generateViewEndpoints(transitions: List<FacadeTransition>, className: String): Pair<List<FunSpec>, List<TypeSpec>> {
        val endpointGET = transitions
                .map { transition ->
                    val props = transition.data
                            .map { prop ->
                                Prop(prop.id, false)
                            }

                    val roles = transition.rolesView
                            .map { role ->
                                "ROLE_${className.toUpperCase()}_${role.toUpperCase()}"
                            }.toList()

                    Endpoint(transition.id, RequestMethod.POST, props, roles, transition.label.value)
                }

        val result = endpointGET.map { endpoint ->
            val returnObjectName = "get${endpoint.id}Result"
            val returnObjectParams = endpoint.props.map {
                ParameterSpec
                        .builder(it.name, String::class)
                        .defaultValue("\"\"")
                        .build()
            }

            // Class
            val obj = TypeSpec.classBuilder(returnObjectName)
                    .primaryConstructor(FunSpec.constructorBuilder()
                            .addParameters(returnObjectParams)
                            .build())
                    .build()

            val fs = generateEmptyFunction(endpoint, "", GetMapping::class)
                    .returns(ClassName("sk.knet.dp.relay.$className", returnObjectName))
                    .addStatement("""
                    |     processServerRequest.get("$className",
                    |     "${endpoint.id}",
                    |     instanceId)
                    """.trimMargin())
                    .addStatement("return $returnObjectName()")


            Pair(fs.build(), obj)
        }


        return Pair(result.map { it.first }, result.map { it.second })
    }

    private fun generateAssignFunctions(transitions: List<FacadeTransition>, className: String): List<FunSpec> {
//        Prep data
        val endpoints = transitions
                .map { transition ->
                    val roles = transition.rolesPerform
                            .map { role ->
                                "ROLE_${className.toUpperCase()}_${role.toUpperCase()}"
                            }.toList()

                    Endpoint(transition.id, RequestMethod.POST, emptyList(), roles, transition.label.value)
                }

        return endpoints
                .map { endpoint ->

                    val statements = mutableListOf(
                            """processServerRequest.assign("$className",
                    |     "${endpoint.id}",
                    |     instanceId)
                    |    return ResponseEntity("", OK)""".trimMargin()
                    )

                    val fs = generateEmptyFunction(endpoint, "assign")

                    statements.map { stmt ->
                        fs.addStatement(stmt)
                    }
                    fs.build()
                }
    }

    private fun generateDelegateFunctions(transitions: List<FacadeTransition>, className: String): List<FunSpec> {
//        Prep data
        val endpoints = transitions
                .map { transition ->
                    val roles = transition.rolesDelegate
                            .map { role ->
                                "ROLE_${className.toUpperCase()}_${role.toUpperCase()}"
                            }.toList()

                    Endpoint(transition.id, RequestMethod.POST, emptyList(), roles, transition.label.value)
                }

        return endpoints
                .map { endpoint ->

                    val statements = mutableListOf(
                            """processServerRequest.delegate("$className",
                    |     "${endpoint.id}",
                    |     instanceId,
                    |     userId)
                    |     return ResponseEntity("", OK)""".trimMargin()
                    )

                    val fs = generateEmptyFunction(endpoint, "delegate")

                    statements.map { stmt ->
                        fs.addStatement(stmt)
                    }

                    fs.addParameter(ParameterSpec
                            .builder("userId", String::class)
                            .addAnnotation(AnnotationSpec
                                    .builder(RequestParam::class)
                                    .addMember("value = \"userId\"").build())
                            .addAnnotation(AnnotationSpec
                                    .builder(ApiParam::class)
                                    .addMember("required = true").build())
                            .build())

                    fs.build()
                }
    }

    private fun generateCancelFunctions(transitions: List<FacadeTransition>, className: String): List<FunSpec> {
//        Prep data
        val endpoints = transitions
                .map { transition ->
                    val roles = transition.rolesPerform
                            .map { role ->
                                "ROLE_${className.toUpperCase()}_${role.toUpperCase()}"
                            }.toList()

                    Endpoint(transition.id, RequestMethod.POST, emptyList(), roles, transition.label.value)
                }

        return endpoints
                .map { endpoint ->

                    val statements = mutableListOf(
                            """processServerRequest.cancel("$className",
                    |     "${endpoint.id}",
                    |     instanceId)
                    |     return ResponseEntity("", OK)""".trimMargin()
                    )

                    val fs = generateEmptyFunction(endpoint, "cancel")

                    statements.map { stmt ->
                        fs.addStatement(stmt)
                    }

                    fs.addParameter(ParameterSpec
                            .builder("userId", String::class)
                            .addAnnotation(AnnotationSpec
                                    .builder(RequestParam::class)
                                    .addMember("value = \"userId\"").build())
                            .addAnnotation(AnnotationSpec
                                    .builder(ApiParam::class)
                                    .addMember("required = true").build())
                            .build())

                    fs.build()
                }
    }

    private fun generateClass(
            transitions: List<FacadeTransition>,
            className: String
    ) {
        println("generating class:$className")
        val viewEndpoints = generateViewEndpoints(transitions, className)
        val finishFunctions = generateFinishFunctions(transitions, className)
        val assignFunctions = generateAssignFunctions(transitions, className)
        val delegateFunctions = generateDelegateFunctions(transitions, className)
        val cancelFunctions = generateCancelFunctions(transitions, className)


        val newClass = TypeSpec.classBuilder(className)
                .addFunctions(finishFunctions
                        .union(viewEndpoints.first)
                        .union(assignFunctions)
                        .union(delegateFunctions)
                        .union(cancelFunctions)
                )
                .addTypes(viewEndpoints.second)
                .addProperty(PropertySpec
                        .builder("processServerRequest",
                                ClassName("sk.knet.dp.relay", "ProcessServerRequest"),
                                KModifier.LATEINIT)
                        .mutable()
                        .addAnnotation(Autowired::class).build())
                .addAnnotation(Controller::class)
                .addAnnotation(EnableResourceServer::class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping::class)
                        .addMember("\"$className\"")
                        .build())
                .build()


        val fileSpec = FileSpec
                .builder("sk.knet.dp.relay", className)
                .addImport(HttpStatus::class, "BAD_REQUEST", "OK")
                .addType(newClass)

        File("$RELAY_BUILD_DIR/src/main/kotlin/sk/knet/dp/relay/$className.kt")
                .writeText(fileSpec.build().toString())
    }


    private fun prepareShell() {

        println("resetting repo")
        try {
            val repo = Git(FileRepository("$RELAY_BUILD_DIR/.git"))

            repo.add().addFilepattern(".").call()
            repo.reset().setMode(ResetCommand.ResetType.HARD).call()
            repo.pull().call()

        } catch (ex: GitAPIException) {
            println(ex)
            println("error resetting, pulling fresh repo")
            File(RELAY_BUILD_DIR).deleteRecursively()
            Git.cloneRepository()
                    .setURI("https://github.com/TheYurry/dp_relay.git")
                    .setDirectory(File(RELAY_BUILD_DIR))
                    .setBranch("master").call()
        }
    }


    private fun runEndpoint() {


        File("$RELAY_BUILD_DIR/gradlew").setExecutable(true)
        for (i in 2..2) {
            println("building: $i")
            var ps = Runtime.getRuntime()
                    .exec("./gradlew build", null, File(RELAY_BUILD_DIR))
            ps.waitFor()
            val out = BufferedReader(InputStreamReader(ps.inputStream)).readLines()
            if (out.reversed().any { Regex(".*BUILD SUCCESSFUL.*").matches(it) }) {
                println("ok")
            } else {
                print(out)
                print(BufferedReader(InputStreamReader(ps.errorStream)).readLines())
            }


            println("\nkilling: $i")
            ps = Runtime.getRuntime().exec("fuser -k 808$i/tcp")
            ps.waitFor()
            print(BufferedReader(InputStreamReader(ps.errorStream)).readLines())
            print(BufferedReader(InputStreamReader(ps.inputStream)).readLines())

            println("\nrunning: $i")
            //ps =
            Runtime.getRuntime()
                    .exec("./gradlew bootrun -Pargs=--spring.main.banner-mode=off,--server.port=808$i", null, File(RELAY_BUILD_DIR))
//            ps.waitFor(10, TimeUnit.SECONDS)
//            print(BufferedReader(InputStreamReader(ps.errorStream)).readLines())
//            print(BufferedReader(InputStreamReader(ps.inputStream)).readLines())

            println("\ndone: $i")

        }


    }

}

