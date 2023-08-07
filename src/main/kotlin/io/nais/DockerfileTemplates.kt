package io.nais

val jvmDockerfileTemplate = """
   FROM gcr.io/distroless/java17-debian11:nonroot

   WORKDIR /app

   # TODO change to match the path to your "fat jar"
   COPY build/libs/app-all.jar .

   CMD ["/app/app-all.jar"]

""".trimIndent()

val nodejsDockerfileTemplate = """
   FROM node:20 as builder
   COPY . /app
   WORKDIR /app

   # TODO change to match the path to your stuff
   FROM gcr.io/distroless/nodejs20-debian11
   COPY --from=builder /app /app
   WORKDIR /app
   CMD ["mystuff.js"]

""".trimIndent()

val goDockerfileTemplate = """
   FROM golang:1.20 as build

   WORKDIR /go/app
   COPY . .

   # TODO change to match the path to your stuff

   RUN go mod download
   RUN CGO_ENABLED=0 make
   RUN CGO_ENABLED=0 make test

   FROM gcr.io/distroless/static-debian11
   COPY --from=build /go/bin/app /
   CMD ["/app"]

""".trimIndent()

val pythonDockerfileTemplate = """
   FROM python:3-slim AS builder
   COPY . /app
   WORKDIR /app

   # TODO change to match the path to your stuff

   FROM gcr.io/distroless/python3
   COPY --from=builder /app /app
   WORKDIR /app
   CMD ["mystuff.py", "/etc"]

""".trimIndent()

val staticWebDockerfileTemplate = """
   FROM docker pull nginxinc/nginx-unprivileged:1.24

   WORKDIR /var/lib/nginx/html/

   # TODO change to match the path to your stuff
   COPY my-html-stuff/ .

   EXPOSE 80

""".trimIndent()
