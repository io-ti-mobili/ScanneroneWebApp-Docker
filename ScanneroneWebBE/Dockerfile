# Stage 1: Build con Maven usando Eclipse Temurin
FROM maven:4.0.0-rc-5-eclipse-temurin-17 AS build
WORKDIR /app

# Copia il pom.xml e scarica le dipendenze (sfrutta la cache di Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia il codice sorgente e compila
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime usando l'immagine JRE (più leggera del JDK) di Temurin
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Il nome del JAR è generato usando l'<artifactId> e la <version> del tuo pom.xml
COPY --from=build /app/target/ScanneroneWebBE-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
