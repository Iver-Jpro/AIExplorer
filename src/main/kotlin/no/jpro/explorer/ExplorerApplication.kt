package no.jpro.explorer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExplorerApplication

fun main(args: Array<String>) {
    runApplication<ExplorerApplication>(*args)
}
