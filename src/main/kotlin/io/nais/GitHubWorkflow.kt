package io.nais

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import io.nais.Environment.PROD
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubWorkflow(
   val name: String = "Build and deploy the main branch",
   val on: PushBuildTrigger,
   val env: Map<String, String> = mapOf("image" to "{{image}}"),
   val jobs: Map<String, Job>
)

fun GitHubWorkflow.serialize() = Yaml(configuration = YamlConfiguration(encodeDefaults = false))
   .encodeToString(GitHubWorkflow.serializer(), this)
   .replace(
      """"##REPLACE_INGRESS##"""", """
    {{#each ingresses as |url|}}
      - {{url}}
    {{/each}}"""
   )
   .replace(""""##REPLACE_IMAGE##"""", "{{image}}")

@Serializable
data class PushBuildTrigger(
   val push: PushEvent
)

@Serializable
data class PushEvent(
   val branches: List<String>
)

@Serializable
data class Job(
   val name: String,
   val needs: String? = null,
   @SerialName("runs-on")
   val runsOn: String,
   val steps: List<BuildStep>
)

@Serializable
data class BuildStep(
   val name: String? = null,
   val uses: String? = null,
   val run: String? = null,
   val args: Map<String, String>? = null,
   val env: Map<String, String>? = null,
   val with: Map<String, String>? = null
)

private val dockerLoginStep = BuildStep(
   name = "Login to GitHub Docker Registry",
   uses = "docker/login-action@v1",
   with = mapOf(
      "registry" to "ghcr.io",
      "username" to "\${{ github.actor }}",
      "password" to "\${{ secrets.GITHUB_TOKEN }}"
   )
)

private val dockerBuildAndPushStep = BuildStep(
   name = "Build and push the Docker image",
   run = "docker build --pull --tag \${IMAGE} . && docker push \${IMAGE}"
)

val gradleJvmBuildSteps = listOf(
   BuildStep(uses = "gradle/wrapper-validation-action@v1"),
   BuildStep(
      uses = "actions/cache@v2",
      with = mapOf(
         "path" to "~/.gradle/caches",
         "key" to "\${{ runner.os }}-gradle-\${{ hashFiles('**/*.gradle.kts') }}",
         "restore-keys" to "\${{ runner.os }}-gradle-",
      )
   ),
   BuildStep(uses = "actions/setup-java@v1", with = mapOf("java-version" to "17")),
   BuildStep(name = "compile and run tests", run = "./gradlew build"),
   dockerLoginStep,
   dockerBuildAndPushStep
)

val mavenJvmBuildSteps = listOf(
   BuildStep(
      uses = "actions/cache@v2",
      with = mapOf(
         "path" to "~/.m2/repository",
         "key" to "\${{ runner.os }}-maven-\${{ hashFiles('**/pom.xml') }}",
         "restore-keys" to "\${{ runner.os }}-maven-",
      )
   ),
   BuildStep(uses = "actions/setup-java@v1", with = mapOf("java-version" to "17")),
   BuildStep(name = "compile and run tests", run = "mvn install"),
   dockerLoginStep,
   dockerBuildAndPushStep
)

val nodejsBuildSteps = listOf(
   BuildStep(uses = "actions/setup-node@v1"),
   BuildStep(name = "install dependencies", run = "npm ci"),
   BuildStep(name = "run tests", run = "npm test"),
   dockerLoginStep,
   dockerBuildAndPushStep
)

val pythonPoetryBuildSteps = listOf(
   BuildStep(name = "setup python", uses = "actions/setup-python@v2", with = mapOf("python-version" to "3.x")),
   BuildStep(name = "install poetry", uses = "abatilo/actions-poetry@v2.1.3"),
   BuildStep(name = "install dependencies", run = "poetry install"),
   BuildStep(name = "run tests", run = "poetry run pytest"),
   dockerLoginStep,
   dockerBuildAndPushStep
)

val pythonPipBuildSteps = listOf(
   BuildStep(name = "setup python", uses = "actions/setup-python@v2", with = mapOf("python-version" to "3.x")),
   BuildStep(name = "install dependencies", run = "pip install -r requirements"),
   BuildStep(name = "run tests", run = "pytest"),
   dockerLoginStep,
   dockerBuildAndPushStep
)

val goMakeBuildSteps = listOf(
   BuildStep(uses = "actions/setup-go@v2", with = mapOf("go-version" to "1.16")),
   BuildStep(name = "perform build", run = "make mytarget"),
   dockerLoginStep,
   dockerBuildAndPushStep
)

val staticWebBuildSteps = listOf(
   BuildStep(name = "perform build", run = """echo "replace me with whatever you do to produce html/css/js files""""),
   dockerLoginStep,
   dockerBuildAndPushStep
)

val checkoutStep = BuildStep(uses = "actions/checkout@v2")

fun appDeployStep(environment: Environment) =
   BuildStep(
      name = "Deploy to $environment",
      uses = "nais/deploy/actions/deploy@v1",
      env = mapOf(
         "APIKEY" to "\${{ secrets.NAIS_DEPLOY_APIKEY }}",
         "CLUSTER" to "${if (environment == PROD) "prod" else "dev"}-gcp",
         "RESOURCE" to ".nais/nais.yaml",
         "VARS" to ".nais/${if (environment == PROD) "prod" else "dev"}.yaml"
      )
   )

fun alertDeployStep(environment: Environment) =
   BuildStep(
      name = "Deploy alerts to $environment",
      uses = "nais/deploy/actions/deploy@v1",
      env = mapOf(
         "APIKEY" to "\${{ secrets.NAIS_DEPLOY_APIKEY }}",
         "CLUSTER" to "${if (environment == PROD) "prod" else "dev"}-gcp",
         "RESOURCE" to ".nais/alerts-${if (environment == PROD) "prod" else "dev"}.yaml",
         "VARS" to ".nais/${if (environment == PROD) "prod" else "dev"}.yaml"
      )
   )

fun topicDeployStep(topicName: String, environment: Environment) =
   BuildStep(
      name = "Deploy Kafka topic $topicName to $environment",
      uses = "nais/deploy/actions/deploy@v1",
      env = mapOf(
         "APIKEY" to "\${{ secrets.NAIS_DEPLOY_APIKEY }}",
         "CLUSTER" to "${if (environment == PROD) "prod" else "dev"}-gcp",
         "RESOURCE" to ".nais/topic-$topicName.yaml",
         "VARS" to ".nais/${if (environment == PROD) "prod" else "dev"}.yaml"
      )
   )

