import org.gradle.jvm.tasks.Jar

application {
    mainClassName = "io.gitlab.arturbosch.detekt.cli.Main"
}

val r8 = configurations.maybeCreate("r8")

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.beust:jcommander")
    implementation(project(":detekt-tooling"))
    implementation(project(":detekt-parser"))
    "r8"("com.android.tools:r8:2.0.99")
    runtimeOnly(project(":detekt-core"))
    runtimeOnly(project(":detekt-rules"))

    testImplementation(project(":detekt-test"))
    testImplementation(project(":detekt-rules"))
}

val fatJarProvider = tasks.withType<Jar>().named("shadowJar")
//val fatJarProvider = tasks.register<Jar>("fatJar") {
//    dependsOn(configurations.named("runtimeClasspath"))
//    dependsOn(tasks.named("jar"))
//
//    classifier = "fat"
//
//    manifest {
//        attributes("Main-Class" to "com.jakewharton.gradle.dependencies.DependencyTreeDiff")
//    }
//
//    doFirst {
//        from(configurations.compileClasspath.get().filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) })
//        exclude("**/*.kotlin_metadata")
//        exclude("**/*.kotlin_module")
//        exclude("**/*.kotlin_builtins")
//        exclude("**/module-info.class")
//        exclude("META-INF/maven/**")
//    }
//}

val r8File = File("$buildDir/libs/${the<BasePluginConvention>().archivesBaseName}-r8.jar")
val r8Jar = tasks.register<JavaExec>("r8Jar") {
    dependsOn(configurations.named("runtimeClasspath"))
    dependsOn(fatJarProvider)
    inputs.file("r8-rules.pro")
    outputs.file(r8File)

    classpath(r8)
    main = "com.android.tools.r8.R8"
    args = listOf(
        "--release",
        "--classfile",
        "--output", r8File.toString(),
        "--pg-conf", "r8-rules.pro",
        "--lib", System.getProperty("java.home").toString()
    )
    doFirst {
        (args as MutableList<String>).add(fatJarProvider.get().archiveFile.get().asFile.absolutePath)
    }
}

// Implements https://github.com/brianm/really-executable-jars-maven-plugin maven plugin behaviour.
// To check details how it works, see http://skife.org/java/unix/2011/06/20/really_executable_jars.html.
// Extracted from https://github.com/pinterest/ktlint/blob/a86d1c76c44d0a1c1adc3f756f36d8b4cac15d32/ktlint/build.gradle#L40-L57
tasks.register<DefaultTask>("shadowJarExecutable") {
    description = "Creates self-executable file, that runs generated shadow jar"
    group = "Distribution"

    dependsOn(r8Jar)
    inputs.file(r8File)
    outputs.file("$buildDir/run/detekt")

    doLast {
        val execFile = outputs.files.singleFile
        execFile.outputStream().use {
            it.write("#!/bin/sh\n\nexec java -Xmx512m -jar \"\$0\" \"\$@\"\n\n".toByteArray())
            it.write(inputs.files.singleFile.readBytes())
        }
        execFile.setExecutable(true, false)
    }
}
