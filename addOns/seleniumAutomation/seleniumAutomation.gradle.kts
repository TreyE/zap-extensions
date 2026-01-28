import org.zaproxy.gradle.addon.AddOnStatus

description = "Enhanced Selenium Automation"

zapAddOn {
    addOnName.set("Selenium Automation")
    addOnStatus.set(AddOnStatus.ALPHA)

    manifest {
        author.set("Trey Evans")
        classpath.setFrom(files())
        extensions {
            register("org.zaproxy.addon.seleniumAutomation.ExtensionSeleniumAutomation") {
                classnames {
                    allowed.set(listOf("org.zaproxy.addon.seleniumAutomation"))
                }
                dependencies {
                    addOns {
                        register("automation") {
                            version.set(">=0.31.0")
                        }
                        register("selenium") {
                            version.set(">=15.0.0")
                        }
                        register("scripts") {
                            version.set(">=45.2.0")
                        }
                    }
                }
            }
        }
        dependencies {
            addOns {
                register("commonlib") {
                    version.set(">=1.37.0")
                }
                register("network") {
                    version.set(">=0.2.0")
                }
                register("selenium") {
                    version.set(">=15.0.0")
                }
                register("scripts") {
                    version.set(">=45.2.0")
                }
            }
        }
    }
}

crowdin {
    configuration {
        val resourcesPath = "org/zaproxy/addon/${zapAddOn.addOnId.get()}/resources/"
        tokens.put("%messagesPath%", resourcesPath)
    }
}

spotless {
    java {
        target(
            fileTree(projectDir) {
                include("src/**/*.java")
            },
        )
    }
}

dependencies {
    zapAddOn("automation")
    zapAddOn("commonlib")
    zapAddOn("selenium")
    zapAddOn("network")
    zapAddOn("scripts")

    implementation(libs.scripts.byteBuddy)
}
