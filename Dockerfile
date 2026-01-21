FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# CAMBIO: Usar *.jar para que no falle si cambia la versión del proyecto
COPY target/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java","-jar","app.jar"]