package io.nais

val jvmDockerfileTemplate = """
   FROM cgr.dev/chainguard/jre/openjdk-17

   WORKDIR /app

   # TODO change to match the path to your "fat jar"
   COPY build/libs/app-all.jar .

   CMD ["-jar", "app-all.jar"]

""".trimIndent()

val nodejsDockerfileTemplate = """
   FROM cgr.dev/chainguard/node:20
   # TODO change to match the path to your code
   WORKDIR /app
   COPY --chown=node:node ./mystuff.js .
   WORKDIR /app
   CMD ["mystuff.js"]

""".trimIndent()

val goDockerfileTemplate = """
   FROM cgr.dev/chainguard/static:latest
   # TODO change to match the path to your stuff
   COPY ./bin/hello /
   ENTRYPOINT ["/hello"]

""".trimIndent()

val pythonDockerfileTemplate = """
   FROM cgr.dev/chainguard/python:3.11
   # TODO change to match the path to your code
   COPY ./mystuff.py /app/
   WORKDIR /app
   ENTRYPOINT ["python", "mystuff.py"]

""".trimIndent()

val staticWebDockerfileTemplate = """
   FROM cgr.dev/chainguard/nginx:latest

   WORKDIR /var/lib/nginx/html/

   # TODO change to match the path to your stuff
   COPY my-html-stuff/ .

   EXPOSE 80

""".trimIndent()
