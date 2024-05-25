# Telegram Recipe Bot

## Introduction

This is a Telegram bot designed to help you find, store, and manage cooking recipes. It's built using Spring Boot, MongoDB for the database, and the [TelegramBots](https://github.com/rubenlagus/TelegramBots) library for interacting with the Telegram API. 

You can try bot [here](https://t.me/pocket_chef_bot?start).

## Bot Functionality

- Search Recipes: You can search for recipes by any keyword, use existing buttons for search, or get a random recipe.
- Recipe Rating System: The bot also incorporates a rating system for the recipes. This allows users to rate recipes they have tried on a scale of 1-5. Ratings from all users are averaged to provide an overall score for each recipe, helping users identify the most popular and well-liked recipes in the database.
- Admins and Users: This bot offers different functionalities for admins and users. While all users can search for recipes, rate them, and view the overall scores, only admins have the ability to add and modify recipes in the database. This includes the ability to upload photos and videos for each recipe. The admin rights are crucial in maintaining the quality of the content and ensuring that the database remains useful and informative for all users.
- Admin Notifications: Administrators have the additional capability to send notifications to all users. This is particularly useful for announcing new recipes, updates, or important information related to the use of the bot.
- Languages: There are two supported languages, Russian and English. When you add a recipe, the bot automatically translates and adds the recipe in the second language using the [Google Cloud Translate API](https://github.com/googleapis/google-cloud-java/tree/main/java-translate).

![This is how looks like admin’s interface](https://github.com/Tah10n/CookingRecipeBot/assets/103191025/90550f8f-04c4-4ba9-b3d3-43551f1367e0)

This is how looks like admin’s interface

## Implementing Webhooks


The bot utilizes webhook technology to receive updates from Telegram. This method is more efficient than long polling as it enables the bot to receive updates instantly without the need to frequently send getUpdates requests.

Here's how to use ngrok for your webhook URL:

1. Download and install ngrok from https://ngrok.com/download.
2. Once installed, start ngrok on the same port as your local server by opening a new terminal/command prompt window. If your server is running on port 8080 (I used 8443), use the command: `ngrok http 8080`.
3. After starting ngrok, it will display a forwarding URL. This public URL forwards to your local server.
4. Copy this URL and set it as your webhook URL in the application properties file (I used an environment variable for this).
5. Telegram will now send updates to this ngrok URL, which will then forward them to your local server.

Don't forget to keep ngrok running while using the bot. If you stop ngrok, the URL will stop forwarding to your local server.
