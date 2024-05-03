FROM openjdk:17-alpine
LABEL maintainer="Andrei Surkov"
WORKDIR /app
COPY docker/libs libs/
COPY docker/resources resources/
COPY docker/classes classes/
ENTRYPOINT ["java", "-cp", "/app/resources:/app/classes:/app/libs/*", "org.example.cooking_recipe_bot.CookingRecipeBotApplication"]
EXPOSE 8443
