package sk.knet.dp.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.swagger.annotations.ApiOperation
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
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
import java.util.concurrent.TimeUnit
import javax.annotation.security.RolesAllowed


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

@Service
class Generator {

    @Autowired
    lateinit var fileStorage: FileStorage


    fun generateClients(): String {

        val files = fileStorage.listDir() ?: return "no clients"
        prepareShell()

        files.filter {
            Regex("^Users.*").matches(it.fileName.toString())
        }.forEach {
            val users = UsersReader(it.toString())
            registerUsers(users.document.user, it.fileName.toString().substring(5))
        }



        files.filter {
            Regex("^Client.*").matches(it.fileName.toString())
        }.forEach {
            val clientName = it.fileName.toString().substring(6)
            val n = NetReader(it.toString())
            val transitions = n.facadeTransitions
            val classFile = generateClass(transitions, clientName)
            writeClass(classFile, clientName)
        }



        return "done"

    }


    fun registerUsers(users: List<sk.knet.dp.userschema.User>, clientName: String) {

        users.forEach {
            val roles = URLEncoder.encode(
                    it.role.map { itt ->
                        "${clientName.toUpperCase()}_${itt.id.toUpperCase()}"
                    }.toString(),
                    StandardCharsets.UTF_8.toString())
            print(URL("http://localhost:8088/addUser?username=${clientName}_${it.name}&password=${it.password}&roles=$roles").readText())
        }

    }


    private fun generatePostFunctions(transitions: List<FacadeTransition>, className: String): List<FunSpec> {
//        Prep data
        val endpointPOST = transitions
                .map { transition ->
                    val props = transition.data
                            .filter { prop ->
                                prop.logic.behavior.contains(Behavior.EDITABLE)
                            }
                            .map { prop ->
                                val required = prop.logic.behavior.contains(Behavior.EDITABLE) && prop.logic.behavior.contains(Behavior.REQUIRED)

                                Prop(prop.id,
                                        required,
                                        prop.type,
                                        prop.values)
                            }


                    val roles = transition.rolesPerform
                            .map { role ->
                                "ROLE_${className.toUpperCase()}_${role.toUpperCase()}"
                            }.toList()

                    Endpoint(transition.id,
                            RequestMethod.POST,
                            props,
                            roles,
                            transition.label.value)
                }
                .filter {
                    it.props.isNotEmpty()
                }




        return endpointPOST
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

                            statements.add(""" if (${
                            if (!prop.required) {
                                """${prop.name} !=  "" && """
                            } else {
                                ""
                            }} !Regex("$regex").matches(${prop.name}) ) {
                                errors.add("${prop.name}" +
                                " should match $regex" )}""")
                        }
                    }

                    if (statements.isNotEmpty())


                        statements.add(""" if(errors.isNotEmpty()){
                        |  return ResponseEntity(errors.toString(), BAD_REQUEST)
                        |} """.trimMargin())

                    statements.add("""
                         processServerRequest.post("$className",
                         "${endpoint.id}",
                         mapOf(${endpoint.props.joinToString(",") {
                        """ "${it.name}" to ${it.name} """
                    }})
                        )


                        return ResponseEntity("", OK)
                    """.trimIndent())

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
                    val fs = FunSpec.builder("post${endpoint.id}")
                            .addAnnotation(AnnotationSpec.builder(PostMapping::class)
                                    .addMember("\"$className/${endpoint.id}\"")
                                    .build())
                            .addAnnotation(rolesAnnotation.build())
                            .addAnnotation(descriptionAnnotation.build())
                            .returns(t)
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
                                val par = ParameterSpec
                                        .builder(param.name,
                                                when (param.type) {
                                                    DataType.FILE -> MultipartFile::class
                                                    else -> String::class

                                                }
                                        )

                                        .addAnnotation(ann.build())
                                fs.addParameter(par.build())

                            }
                    fs.build()
                }
    }


    private fun generateGetEndpoints(transitions: List<FacadeTransition>, className: String): Pair<List<FunSpec>, List<TypeSpec>> {
        val endpointGET = transitions
                .map { transition ->
                    val props = transition.data
                            .filter { prop ->
                                prop.logic.behavior.contains(Behavior.VISIBLE)
                            }
                            .map { prop ->
                                Prop(prop.id, false)
                            }

                    val roles = transition.rolesView
                            .map { role ->
                                "ROLE_${className.toUpperCase()}_${role.toUpperCase()}"
                            }.toList()


                    Endpoint(transition.id,
                            RequestMethod.POST,
                            props,
                            roles,
                            transition.label.value)
                }
                .filter { transition ->
                    transition.props.isNotEmpty()
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

            // Annotations
            val rolesAnnotation = AnnotationSpec.builder(RolesAllowed::class)
            endpoint.roles.forEach { role ->
                rolesAnnotation.addMember("\"$role\"")
            }

            val descriptionAnnotation = AnnotationSpec.builder(ApiOperation::class)
            descriptionAnnotation.addMember("value = \"${endpoint.label}\"")
            descriptionAnnotation.addMember("notes = \"Allowed roles: ${endpoint.roles}\"")


            val fs = FunSpec.builder("get${endpoint.id}")
                    .addAnnotation(AnnotationSpec.builder(GetMapping::class)
                            .addMember("\"$className/${endpoint.id}\"")
                            .build())
                    .addAnnotation(rolesAnnotation.build())
                    .addAnnotation(descriptionAnnotation.build())
                    .addStatement("return $returnObjectName()")


            Pair(fs.build(), obj)
        }


        return Pair(result.map { it.first }, result.map { it.second })
    }


    private fun generateClass(
            transitions: List<FacadeTransition>,
            className: String
    ): String {
        val endpointsGET = generateGetEndpoints(transitions, className)


        val postFunctions = generatePostFunctions(transitions, className)


        val newClass = TypeSpec.classBuilder(className)
                .addFunctions(postFunctions.union(endpointsGET.first))
                .addTypes(endpointsGET.second)
                .addProperty(PropertySpec
                        .builder("processServerRequest",
                                ClassName("sk.knet.dp.endpointshell", "ProcessServerRequest"),
                                KModifier.LATEINIT)
                        .mutable()
                        .addAnnotation(Autowired::class).build())
                .addAnnotation(RestController::class)
                .addAnnotation(EnableResourceServer::class)
                .build()


        val fileSpec = FileSpec
                .builder("sk.knet.dp.endpointshell", className)
                .addImport(HttpStatus::class, "BAD_REQUEST", "OK")
                .addType(newClass)

        return fileSpec.build().toString()
    }


    private fun prepareShell() {


        try {
            val repo = Git(FileRepository("./endpoint-shell/.git"))
            repo.stashCreate().call()
            repo.pull().call()
            repo.stashApply().call()


        } catch (ex: GitAPIException) {
            File("./endpoint-shell").deleteRecursively()
            Git.cloneRepository()
                    .setURI("https://github.com/TheYurry/dp_endpoint.git")
                    .setDirectory(File("./endpoint-shell"))
                    .setBranch("master").call()
        }
    }


    private fun writeClass(classString: String, clientName: String) {
        File("./endpoint-shell/src/main/kotlin/sk/knet/dp/endpointshell/$clientName.kt")
                .writeText(classString)


        File("./endpoint-shell/gradlew").setExecutable(true)
        for (i in 2..2) {
            println("building: $i")
            var ps = Runtime.getRuntime()
                    .exec("./gradlew build", null, File("./endpoint-shell"))
            ps.waitFor()
            print(BufferedReader(InputStreamReader(ps.inputStream)).readLines())
            print(BufferedReader(InputStreamReader(ps.errorStream)).readLines())

            Runtime.getRuntime().exec("fuser -k 808$i/tcp")

            ps = Runtime.getRuntime()
                    .exec("./gradlew bootrun -Pargs=--spring.main.banner-mode=off,--server.port=808$i", null, File("./endpoint-shell"))
            print(BufferedReader(InputStreamReader(ps.errorStream)).readLines())
            print(BufferedReader(InputStreamReader(ps.inputStream)).readLines())
            ps.waitFor(10, TimeUnit.SECONDS)

        }


    }

}

