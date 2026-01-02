rootProject.name = "my-java-multimodule"

include(
    "emr-core",
    "emr-domain",
    "emr-clinical",
    "emr-finance"
)

project(":emr-finance").projectDir = file("emr-finance")
