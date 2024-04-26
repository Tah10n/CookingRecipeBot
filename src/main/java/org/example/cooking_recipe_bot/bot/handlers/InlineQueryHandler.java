package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.utils.RecipeParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.cached.InlineQueryResultCachedPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class InlineQueryHandler implements UpdateHandler {
    UserDAO userDAO;
    InlineKeyboardMaker inlineKeyboardMaker;
    TelegramClient telegramClient;
    RecipeDAO recipeDAO;
    BotStateContextDAO botStateContextDAO;

    public InlineQueryHandler(UserDAO userDAO, InlineKeyboardMaker inlineKeyboardMaker, TelegramClient telegramClient, RecipeDAO recipeDAO, BotStateContextDAO botStateContextDAO) {
        this.userDAO = userDAO;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.telegramClient = telegramClient;
        this.recipeDAO = recipeDAO;
        this.botStateContextDAO = botStateContextDAO;
    }

    public BotApiMethod<?> handle(Update update) throws TelegramApiException {
        InlineQuery inlineQuery = update.getInlineQuery();
        String query = inlineQuery.getQuery();
        User user = userDAO.getUserByUserName(inlineQuery.getFrom().getUserName());
        long chatId = inlineQuery.getFrom().getId();

        if (query.contains("/edit_recipe")) {
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("Сотрите имя бота и отправьте отредактированный рецепт \uD83D\uDC47").build();
            telegramClient.execute(sendMessage);

            BotStateContext botStateContext = botStateContextDAO.findBotStateContextByUserName(inlineQuery.getFrom().getUserName());
            botStateContext.setCurrentBotState(BotState.WAITING_FOR_EDITED_RECIPE);
            botStateContextDAO.saveBotStateContext(botStateContext);
//            String[] split = query.split("//");
//            String recipeId = split[1];
//            try {
//                Recipe recipe = recipeDAO.findRecipeById(recipeId);
//                recipeDAO.updateRecipe(recipe);
//                AnswerInlineQuery answerInlineQuery = AnswerInlineQuery.builder().inlineQueryId(inlineQuery.getId())
//                        .result(InlineQueryResultCachedPhoto.builder().id(recipe.getId()).photoFileId(recipe.getPhotoId()).title(recipe.getName()).build()).build();
//                telegramClient.execute(answerInlineQuery);
//            } catch (ParseException e) {
//                e.printStackTrace();
//                log.error(e.getMessage());
//
//                SendMessage sendMessage = SendMessage.builder().text(BotMessageEnum.RECIPE_PARSING_ERROR.getMessage()).chatId(chatId).build();
//                return sendMessage;
//            } catch (TelegramApiException e) {
//                e.printStackTrace();
//                log.error(e.getMessage());
//            }
            return null;
        } else{
            try {
                telegramClient.execute(AnswerInlineQuery.builder().inlineQueryId(inlineQuery.getId()).results(getInlineQueryResultList(query)).build());
            } catch (TelegramApiException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }


            return null;
        }

        private Collection<? extends InlineQueryResult> getInlineQueryResultList (String query){
            List<InlineQueryResult> inlineQueryResults = new ArrayList<>();
            //todo: add pagination, change search method
            recipeDAO.findRecipesByHashtagsContains(query).forEach(recipe -> {
                InlineQueryResultCachedPhoto inlineQueryResult = InlineQueryResultCachedPhoto.builder().id(recipe.getId()).photoFileId(recipe.getPhotoId())
                        .title(recipe.getName()).build();
                inlineQueryResults.add(inlineQueryResult);
            });
            return inlineQueryResults;
        }
    }
