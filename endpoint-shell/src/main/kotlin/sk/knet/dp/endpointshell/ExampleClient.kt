package sk.knet.dp.endpointshell

import kotlin.String
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ExampleClient {
    @GetMapping("ExampleClient/exampleEndpoint")
    fun exampleEndpoint(@RequestParam name: String, @RequestParam name2: String) {
        println("""Hello, $name""")
    }

    @GetMapping("ExampleClient/exampleEndpoint2")
    fun exampleEndpoint2(@RequestParam name: String, @RequestParam Fico: String) {
        println("""Hello, $name""")
    }

    @GetMapping("ExampleClient/exampleEndpoint3")
    fun exampleEndpoint3(@RequestParam name: String) {
        println("""Hello, $name""")
    }

    @PostMapping("ExampleClient/exampleEndpointPOST")
    fun exampleEndpointPOST(@RequestParam name: String, @RequestParam name2: String) {
        println("""Hello, $name""")
    }

    @PostMapping("ExampleClient/exampleEndpointPOST2")
    fun exampleEndpointPOST2(@RequestParam name: String, @RequestParam Fico: String) {
        println("""Hello, $name""")
    }

    @PostMapping("ExampleClient/exampleEndpointPOST3")
    fun exampleEndpointPOST3(@RequestParam name: String) {
        println("""Hello, $name""")
    }
}
