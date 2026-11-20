# ---------- Etapa 1: compilar con Maven y Java 25 ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Primero el pom para aprovechar la caché de dependencias
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Luego el código fuente
COPY src src
RUN mvn -q -B clean package -DskipTests

# ---------- Etapa 2: imagen liviana solo con el JRE ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render asigna el puerto en la variable PORT (application.properties la usa)
EXPOSE 8080
# Límite de memoria para el plan gratuito (512 MB)
ENV JAVA_OPTS="-Xmx300m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
