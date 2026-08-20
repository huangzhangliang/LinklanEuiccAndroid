plugins {
    id("maven-publish")
}

// ===== 从 JitPack 传入的参数或环境变量获取 groupId / version =====
// JitPack 传 -Pgroup=xxx -Pversion=xxx，同时设置 GROUP / VERSION 环境变量
val pubGroup = project.findProperty("group")?.toString()
    ?: System.getenv("GROUP")
    ?: "com.github.huangzhangliang"

val pubVersion = project.findProperty("version")?.toString()
    ?: System.getenv("VERSION")
    ?: "1.0.8"

println("Publishing: group=$pubGroup, version=$pubVersion")

afterEvaluate {
    extensions.configure<PublishingExtension> {
        // 遍历 libs/ 下的所有 AAR 文件，逐个发布为 Maven artifact
        fileTree("libs").matching { include("*.aar") }.files.forEach { aarFile ->
            // 从文件名提取 artifactId
            // 例如: droidlib-linklan-release.aar -> droidlib-linklan-release
            // 例如: droidlib-linklan-release-1.0.8.aar -> droidlib-linklan-release
            var artifactName = aarFile.nameWithoutExtension
            // 去掉可能的版本号后缀
            artifactName = artifactName.replace(Regex("-\\d+\\.\\d+\\.\\d+$"), "")

            println("Publication: $pubGroup:$artifactName:$pubVersion (from ${aarFile.name})")

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
                    }
                }
            }
        }
    }
}
