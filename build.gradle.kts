plugins {
    id("maven-publish")    // 注意：不需要 android 插件，只做 AAR 发布
}

// ===== 把 AAR 文件作为 Maven artifact 发布 =====
afterEvaluate {
    extensions.configure<PublishingExtension> {
        fileTree("libs").matching { include("*.aar") }.files.forEach { aarFile ->
            val artifactName = aarFile.nameWithoutExtension
                .removeSuffix("-1.0.8")   // 去掉版本号，得到 artifactId

            publications {
                create<MavenPublication>(artifactName) {
                    groupId = "com.linklan"
                    artifactId = artifactName
                    version = "1.0.8"
                    artifact(aarFile)

                    pom {
                        name.set(artifactName)
                        description.set("LinklanLPA eSIM SDK")
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
