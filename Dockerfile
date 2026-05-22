FROM gradle:9.5.1-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle --no-daemon :app:installDist

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV PORT=3000
COPY --from=build /workspace/app/build/install/app/ /app/
EXPOSE 3000
CMD ["./bin/app"]
