plugins {
    id("maven-publish")
}

// ✅ 支持 JitPack 的 groupId
val pubGroup = project.findProperty("group")?.toString()
    ?: System.getenv("GROUP")
    ?: "com.github.huangzhangliang"  // JitPack 格式

val pubVersion = project.findProperty("version")?.toString()
    ?: System.getenv("VERSION")
    ?: "1.0.8"

println("Publishing: group=$pubGroup, version=$pubVersion")

afterEvaluate {
    extensions.configure<PublishingExtension> {
        // ✅ 添加 mavenLocal 仓库（JitPack 需要）
        repositories {
            mavenLocal()
        }

        fileTree("libs").matching { include("*.aar") }.files.forEach { aarFile ->
            var artifactName = aarFile.nameWithoutExtension
            artifactName = artifactName.replace(Regex("-\\d+\\.\\d+\\.\\d+$"), "")

            publications {
                create<MavenPublication>(artifactName) {
                    groupId = pubGroup
                    artifactId = artifactName
                    version = pubVersion
                    artifact(aarFile)

                    pom {
                        name.set(artifactName)
                        description.set("Linklan eSIM SDK")
                        url.set("https://github.com/huangzhangliang/LinklanEuiccAndroid")
                        licenses {
                            license {
                                name.set("MIT")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        developers {
                            developer {
                                id.set("huangzhangliang")
                                name.set("huangzhangliang")
                                email.set("82770536@qq.com")
                            }
                        }
                        scm {
                            url.set("https://github.com/huangzhangliang/LinklanEuiccAndroid")
                        }
                    }
                }
            }
        }
    }
}