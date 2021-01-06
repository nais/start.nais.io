package io.nais.testdata

val basicNaisYaml = """
      apiVersion: "nais.io/v1alpha1"
      kind: "Application"
      metadata:
        name: "mycoolapp"
        namespace: "myteam"
        labels:
          "team": "myteam"
      spec:
        image: "something/whatever:1"
        liveness:
          path: "/isalive"
          port: 80
          initialDelay: 20
          timeout: 1
        readiness:
          path: "/isready"
          port: 80
          initialDelay: 20
          timeout: 1
        replicas:
          min: 2
          max: 2
          cpuThresholdPercentage: 50
        prometheus:
          enabled: true
          path: "/metrics"
        limits:
          cpu: "200m"
          memory: "256Mi"
        requests:
          cpu: "200m"
          memory: "256Mi"
        ingresses: []
         """.trimIndent()

private val dollar = '$' // workaround, escaping doesn't work in multiline strings (https://youtrack.jetbrains.com/issue/KT-2425)
val gradleJvmWorkflowYaml = """
name: "tulleflow"
on:
  push:
    branches:
    - "main"
env:
  "IMAGE": "docker.pkg.github.com/org/app:version"
jobs:
  build:
    name: "build"
    runs-on: "ubuntu-latest"
    steps:
    - uses: "actions/checkout@v2"
    - uses: "gradle/wrapper-validation-action@v1"
    - uses: "actions/cache@v1"
      with:
        "path": "~/.gradle/caches"
        "key": "${dollar}{{ runner.os }}-gradle-${dollar}{{ hashFiles('**/*.gradle.kts') }}"
        "restore-keys": "${dollar}{{ runner.os }}-gradle-"
    - uses: "actions/setup-java@v1"
      with:
        "java-version": "15"
    - name: "compile and run tests"
      run: "./gradlew build"
    - name: "Build and publish Docker image"
      run: "docker build --tag ${dollar}{IMAGE} . && echo ${dollar}GITHUB_TOKEN | docker login --username\
        \ ${dollar}GITHUB_REPOSITORY --password-stdin https://docker.pkg.github.com && docker\
        \ push ${dollar}{IMAGE}"
      env:
        "GITHUB_TOKEN": " ${dollar}{{ secrets.GITHUB_TOKEN }}"
  deployToDev:
    name: "Deploy to dev"
    runs-on: "ubuntu-latest"
    steps:
    - uses: "actions/checkout@v2"
    - name: "Deploy to dev-gcp"
      uses: "nais/deploy/actions/deploy@v1"
      env:
        "APIKEY": "${dollar}{{ secrets.NAIS_DEPLOY_APIKEY }}"
        "CLUSTER": "dev-gcp"
        "RESOURCE": ".nais/nais.yaml"
        "VARS": ".nais/dev.yaml"
  deployToProd:
    name: "Deploy to prod"
    runs-on: "ubuntu-latest"
    steps:
    - uses: "actions/checkout@v2"
    - name: "Deploy to prod-gcp"
      uses: "nais/deploy/actions/deploy@v1"
      env:
        "APIKEY": "${dollar}{{ secrets.NAIS_DEPLOY_APIKEY }}"
        "CLUSTER": "prod-gcp"
        "RESOURCE": ".nais/nais.yaml"
        "VARS": ".nais/prod.yaml"
""".trimIndent()

val nodejsWorkflowYaml = """
name: "tulleflow"
on:
  push:
    branches:
    - "main"
env:
  "IMAGE": "docker.pkg.github.com/org/app:version"
jobs:
  build:
    name: "build"
    runs-on: "ubuntu-latest"
    steps:
    - uses: "actions/checkout@v2"
    - uses: "actions/cache@v1"
      with:
        "path": "~/.npm"
        "key": "${dollar}{{ runner.os }}-node-${dollar}{{ hashFiles('**/package-lock.json') }}"
        "restore-keys": "${dollar}{{ runner.os }}-node-"
    - uses: "actions/setup-node@v1"
    - name: "install dependencies"
      run: "npm install"
    - name: "compile and run tests"
      run: "npm run build"
  deployToDev:
    name: "Deploy to dev"
    runs-on: "ubuntu-latest"
    steps:
    - uses: "actions/checkout@v2"
    - name: "Deploy to dev-gcp"
      uses: "nais/deploy/actions/deploy@v1"
      env:
        "APIKEY": "${dollar}{{ secrets.NAIS_DEPLOY_APIKEY }}"
        "CLUSTER": "dev-gcp"
        "RESOURCE": ".nais/nais.yaml"
        "VARS": ".nais/dev.yaml"
  deployToProd:
    name: "Deploy to prod"
    runs-on: "ubuntu-latest"
    steps:
    - uses: "actions/checkout@v2"
    - name: "Deploy to prod-gcp"
      uses: "nais/deploy/actions/deploy@v1"
      env:
        "APIKEY": "${dollar}{{ secrets.NAIS_DEPLOY_APIKEY }}"
        "CLUSTER": "prod-gcp"
        "RESOURCE": ".nais/nais.yaml"
        "VARS": ".nais/prod.yaml"
""".trimIndent()
