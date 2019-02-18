package sk.knet.dp.generator

import com.squareup.kotlinpoet.*
import org.springframework.web.bind.annotation.*


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
    fun register(
//            @RequestParam(value = "name", defaultValue = "World") name: String
    ): String {


        val clientName = "ExampleClient"
        val endpointGET = setOf(
                Endpoint("exampleEndpoint", RequestMethod.GET, listOf(Prop("name"),Prop("name2"))),
                Endpoint("exampleEndpoint2", RequestMethod.GET, listOf(Prop("name"),Prop("Fico"))),
                Endpoint("exampleEndpoint3", RequestMethod.GET, listOf(Prop("name")))
        )



        val functions = endpointGET.map { endpoint ->

            val fs = FunSpec.builder(endpoint.id)
                    .addAnnotation(AnnotationSpec.builder(GetMapping::class)
                            .addMember("\"$clientName/${endpoint.id}\"")
                            .build())
                    .addStatement("println(%P)", "Hello, \$name")
            endpoint.props.forEach { param ->
                val par = ParameterSpec
                        .builder(param.name, String::class)
                        .addAnnotation(RequestParam::class)
                        .build()
                fs.addParameter(par)

            }

            fs.build()
        }


        val endpointPOST = setOf(
                Endpoint("exampleEndpointPOST", RequestMethod.POST, listOf(Prop("name"),Prop("name2"))),
                Endpoint("exampleEndpointPOST2", RequestMethod.POST, listOf(Prop("name"),Prop("Fico"))),
                Endpoint("exampleEndpointPOST3", RequestMethod.POST, listOf(Prop("name")))
        )

        val functions2 = endpointPOST.map { endpoint ->

            val fs = FunSpec.builder(endpoint.id)
                    .addAnnotation(AnnotationSpec.builder(PostMapping::class)
                            .addMember("\"$clientName/${endpoint.id}\"")
                            .build())
                    .addStatement("println(%P)", "Hello, \$name")
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

        val fileSpec = FileSpec.builder("", clientName)
                .addType(newClass)


        return fileSpec.build().toString()

    }

}
