# =============================================================================
# ⚠️ ARQUIVO OBSOLETO — NÃO É MAIS UTILIZADO
# =============================================================================
# Este Dockerfile foi criado quando o plano era dockerizar TUDO (banco + backend
# + frontend). A decisão atual é dockerizar APENAS o banco (ver docker-compose.yml).
# O backend Spring Boot agora roda localmente via IntelliJ (run config "RODAR WICKET").
#
# Mantido aqui apenas para referência futura — caso queira voltar a dockerizar
# o backend, este arquivo serve como ponto de partida.
# =============================================================================

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
ENTRYPOINT ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED", "-jar", "app.jar"]