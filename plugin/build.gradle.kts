plugins {
    // gradle 插件必须依赖
    `java-gradle-plugin`
    // 发布
    `maven-publish`
    // 插件如果使用 kotlin 编写则需要导致 kotlin 插件，否则 kotlin 的类无法打包进去
    id("kotlin")
}

// 这里不是声明一个局部变量，而是为插件 group 和 version 赋值
group = "com.ei.plugin"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("hello") {
            id = "com.ei.plugin.hello"
            implementationClass = "com.ei.plugin.hello.HelloWorldPlugin"
        }
    }
}

publishing {
    repositories {
        // 发布到本地目录中
        maven(url = "../local-plugin-repo")
    }
}
