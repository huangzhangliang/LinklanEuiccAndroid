plugins {
    id("maven-publish")
}

// 从 JitPack 传入的参数获取，没有则用默认值
val jitpackGroup = project.findProperty("group")?.toString() ?: "com.linklan"
val jitpackVersion = project.findProperty("version")?.toString() ?: "1.0.8"

afterEvaluate {
    extensions.configure<PublishingExtension> {
        fileTree("libs").matching { include("*.aar") }.files.forEach { aarFile ->
            // 从文件名提取 artifactId
            val artifactName = aarFile.nameWithoutExtension
                .replace("-$jitpackVersion", "")

            publications {
                create<MavenPublication>(artifactName) {
                    groupId = jitpackGroup                      // JitPack 传入 com.github.huangzhangliang
                    artifactId = artifactName                    // droidlib-linklan-release 等
                    version = jitpackVersion                     // JitPack 传入 1.0.2
                    artifact(aarFile)

                    pom {
                        name.set(artifactName)
                        description.set("Linklan eSIM SDK")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "Local"
                url = uri("${rootProject.buildDir}/repo")
            }
        }
    }
}
