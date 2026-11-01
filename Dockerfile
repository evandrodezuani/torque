# Etapa 1: compila o jar. Quem roda o projeto não precisa ter Java nem Maven, só o Docker.
FROM eclipse-temurin:21.0.13_9-jdk-alpine-3.22 AS build
WORKDIR /build

# As dependências vêm antes do código: enquanto o pom.xml não muda, o Docker reaproveita esta camada.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Os testes rodam na máquina do desenvolvedor e no Jenkins, não no build da imagem.
COPY src src
RUN ./mvnw -B -q package -DskipTests

# Etapa 2: só o JRE e o jar. A imagem final não leva o JDK, o Maven nem o código-fonte.
FROM eclipse-temurin:21.0.13_9-jre-alpine-3.22
RUN addgroup -S torque && adduser -S torque -G torque
WORKDIR /app
COPY --from=build /build/target/torque-*.jar app.jar

# A aplicação não roda como root dentro do container.
USER torque
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
