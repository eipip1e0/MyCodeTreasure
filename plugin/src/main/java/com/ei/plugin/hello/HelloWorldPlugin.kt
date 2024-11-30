package com.ei.plugin.hello

import org.gradle.api.Plugin
import org.gradle.api.Project

class HelloWorldPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Hello World")
    }
}
