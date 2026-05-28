# Estágio 1: Build (Compilação)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
# Copia o pom.xml e descarrega as dependências (otimiza o cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline
# Copia o código fonte e gera o ficheiro .jar
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Run (Execução)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copia APENAS o .jar gerado no estágio anterior (deixa o contentor muito mais leve)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]