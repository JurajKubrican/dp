package sk.knet.dp.endpointshell

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EndpointShellApplication

fun main(args: Array<String>) {
    runApplication<EndpointShellApplication>(*args)
}
