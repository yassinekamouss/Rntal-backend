# --- Étape 1 : Le "Chantier" (Build) ---
# On utilise une image qui contient Maven et Java 17
FROM maven:3.9-eclipse-temurin-17 AS builder

# On définit le dossier de travail
WORKDIR /app

# On copie d'abord le pom.xml pour optimiser le cache Docker
COPY pom.xml .
# On copie le reste du code source
COPY src ./src

# On lance le build Maven pour créer le .jar
# -DskipTests accélère le build en sautant les tests (fréquent en CI)
RUN mvn package -DskipTests


# --- Étape 2 : Le "Serveur" (Runtime) ---
# On repart d'une image Java propre et légère
FROM openjdk:17-jdk-alpine

# On définit le dossier de travail
WORKDIR /app

# On copie UNIQUEMENT le JAR créé à l'étape "builder"
# Note : on le renomme en "app.jar" pour un nom simple
COPY --from=builder /app/target/demo1-0.0.1-SNAPSHOT.jar /app/app.jar

# On expose le port
EXPOSE 8080

# On lance le .jar (en utilisant le nom simple "app.jar")
CMD ["java", "-jar", "/app/app.jar"]