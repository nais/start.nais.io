package io.nais

val jvmDockerfileTemplate = """
   FROM gcr.io/distroless/java17-debian11:nonroot

   # TODO change to match the path to your "fat jar"
   COPY build/libs/app-all.jar /app/app-all.jar

   WORKDIR /app

   USER nonroot

   CMD ["app-all.jar"]

""".trimIndent()

val nodejsDockerfileTemplate = """
   FROM gcr.io/distroless/nodejs18-debian11:nonroot
   # TODO change to match the path to your code
   COPY ./mystuff.js /app/
   WORKDIR /app
   CMD ["mystuff.js"]

""".trimIndent()

val goDockerfileTemplate = """
   FROM gcr.io/distroless/static-debian11:nonroot
   # TODO change to match the path to your stuff
   COPY ./bin/hello /
   CMD ["/hello"]

""".trimIndent()

val pythonDockerfileTemplate = """
   FROM python3-debian11:nonroot
   # TODO change to match the path to your code
   COPY ./mystuff.py /app/
   WORKDIR /app
   CMD ["mystuff.py"]

""".trimIndent()

val staticWebDockerfileTemplate = """
   FROM nginxinc/nginx-unprivileged:1.22-alpine

   USER nginx
   WORKDIR /app

   # TODO change to match the path to your stuff
   COPY my-html-stuff/ ./

   EXPOSE 8080

""".trimIndent()
