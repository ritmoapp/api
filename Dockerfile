# Usa uma imagem do JDK para a construção
FROM maven:3.9.3-eclipse-temurin-17 AS build

# Define o diretório de trabalho para a construção
WORKDIR /app

# Copia os arquivos do projeto para o container
COPY pom.xml .
COPY src ./src

# Faz o download das dependências e compila o projeto
RUN mvn clean package -DskipTests

# Usa uma imagem do JRE para o runtime da aplicação
FROM eclipse-temurin:17-jre

# Define o diretório de trabalho para a execução
WORKDIR /app

# Copia o arquivo JAR gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Exposição da porta padrão
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
