rootProject.name = "espial"
include("api")
include("sponge")
include("sponge:api12")
findProject(":sponge:api12")?.name = "api12"
include("sponge:api14")
findProject(":sponge:api14")?.name = "api14"
