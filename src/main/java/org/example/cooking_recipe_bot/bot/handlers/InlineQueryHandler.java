package org.example.cooking_recipe_bot.bot.handlers;

import java.util.Arrays;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.MessageTranslator;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.config.BotConfig;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAOManager;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class InlineQueryHandler implements UpdateHandler {
    private final TelegramClient telegramClient;
    private final RecipeDAOManager recipeDAOManager;
    private final BotStateContextDAO botStateContextDAO;
    private final BotConfig botConfig;
    private final InlineKeyboardMaker inlineKeyboardMaker;
    private final UserDAO userDAO;
    private final MessageTranslator messageTranslator;

    public InlineQueryHandler(TelegramClient telegramClient, RecipeDAOManager recipeDAOManager, BotStateContextDAO botStateContextDAO, BotConfig botConfig, InlineKeyboardMaker inlineKeyboardMaker, UserDAO userDAO, MessageTranslator messageTranslator) {
        this.telegramClient = telegramClient;
        this.recipeDAOManager = recipeDAOManager;
        this.botStateContextDAO = botStateContextDAO;
        this.botConfig = botConfig;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.userDAO = userDAO;
        this.messageTranslator = messageTranslator;
    }

    public BotApiMethod<?> handle(Update update) {
        InlineQuery inlineQuery = update.getInlineQuery();
        String query = inlineQuery.getQuery();

        User user = userDAO.getUserById(inlineQuery.getFrom().getId());
        BotStateContext botStateContext = botStateContextDAO.findBotStateContextById(inlineQuery.getFrom().getId());

        if (query.contains("/edit_recipe")) {
            long chatId = inlineQuery.getFrom().getId();
            String message = messageTranslator.getMessage(BotMessageEnum.WAITING_FOR_EDITED_RECIPE_MESSAGE.name(), user.getLanguage());
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(message)
                    .replyMarkup(inlineKeyboardMaker.getCancelKeyboard(user)).build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
            botStateContext.setCurrentBotState(BotState.WAITING_FOR_EDITED_RECIPE);
            botStateContextDAO.saveBotStateContext(botStateContext);
            return null;
        } else {
            try {
                Collection<? extends InlineQueryResult> inlineQueryResultList = getInlineQueryResultList(query, user.getLanguage());
                String id = inlineQuery.getId();
                telegramClient.execute(AnswerInlineQuery.builder().inlineQueryId(id).results(inlineQueryResultList).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));

            }
        }

        return null;
    }

    private Collection<? extends InlineQueryResult> getInlineQueryResultList(String query, String language) {
        Set<InlineQueryResult> inlineQueryResults = new HashSet<>();

        List<Recipe> recipes = recipeDAOManager.getRecipeDAO(language).findRecipesByString(query);

        for (Recipe recipe : recipes) {

            InlineQueryResultArticle article = InlineQueryResultArticle.builder()
                    .id(recipe.getId()).title(recipe.getName())
                    .inputMessageContent(InputTextMessageContent.builder()
                            .messageText(recipe.toString())
                            .build())
                    .build();
            if (recipe.getThumbnailId() != null && !recipe.getThumbnailId().isEmpty()) {
                String thumbnailUrl = getUrlFromFileId(recipe.getThumbnailId());
                article.setThumbnailUrl(thumbnailUrl);
            }
            inlineQueryResults.add(article);

        }
        return inlineQueryResults.stream().toList();
    }

    @SneakyThrows
    private String getUrlFromFileId(String photoId) {
        return telegramClient.execute(GetFile.builder().fileId(photoId).build()).getFileUrl(botConfig.getBotToken());
    }
}
