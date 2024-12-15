# Documents

文档
https://kotlinlang.org/docs/ksp-overview.html
github
https://github.com/google/ksp

# KotlinPoet 避免换行

```kotlin
"String1 String2 String3 String4 String5...".replace(" ", "·")
```

# 避免 KSP 生成的文件被意外清理

```kotlin
// 新建文件的时候需要传一个 Dependencies 参数
// 如果 originatingFiles 参数不配置，则产物会在第二次编译直接清楚掉，文件清理条件如下:
// 1. originatingFiles 为空集合
// 2. 注解注释的对象不存在了
env.codeGenerator.createNewFile(
    Dependencies(false, *mDependencies.mapNotNull { it.containingFile }.toTypedArray()),
    packageName,
    simpleName
)
```

# ViewBinding 生成之后处理注解

```kotlin
// 添加编译规则，在 binding 生成之后在再处理注解
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            // This is a workaround for https://issuetracker.google.com/301245705 which depends on internal
            // implementations of the android gradle plugin and the ksp gradle plugin which might change in the future
            // in an unpredictable way.
            val dataBindingTask =
                project.tasks.named("dataBindingGenBaseClasses" + variant.name.capitalize())
                    .get() as DataBindingGenBaseClassesTask

            (project.tasks.getByName("ksp" + variant.name.capitalize() + "Kotlin") as AbstractKotlinCompileTool<*>).setSource(
                dataBindingTask.sourceOutFolder
            )
        }
    }
}
```
