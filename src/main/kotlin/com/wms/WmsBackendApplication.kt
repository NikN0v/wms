package com.wms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WmsBackendApplication

fun main(args: Array<String>) {
    runApplication<WmsBackendApplication>(*args)
}