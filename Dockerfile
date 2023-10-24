FROM cgr.dev/chainguard/jre

COPY build/libs/app-all.jar /app/app-all.jar

CMD ["-jar", "/app/app-all.jar", "-XX:+UseShenandoahGC", "-Xmx512m"]
