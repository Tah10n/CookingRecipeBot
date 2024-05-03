FROM openjdk:17-alpine
LABEL maintainer=Anna
WORKDIR /app
COPY build/docker/libs libs/
COPY build/docker/resources resources/
COPY build/docker/classes classes/
ENTRYPOINT ["java", "-cp", "/app/resources:/app/classes:/app/libs/*", "org.example.cooking_recipe_bot.CookingRecipeBotApplication"]
EXPOSE 8443
