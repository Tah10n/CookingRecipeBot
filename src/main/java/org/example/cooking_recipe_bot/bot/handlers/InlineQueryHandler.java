package org.example.cooking_recipe_bot.bot.handlers;

import java.util.Arrays;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.config.BotConfig;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.Recipe;
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
    private final RecipeDAO recipeDAO;
    private final BotStateContextDAO botStateContextDAO;
    private final BotConfig botConfig;
    private final InlineKeyboardMaker inlineKeyboardMaker;

    public InlineQueryHandler(TelegramClient telegramClient, RecipeDAO recipeDAO, BotStateContextDAO botStateContextDAO, BotConfig botConfig, InlineKeyboardMaker inlineKeyboardMaker) {
        this.telegramClient = telegramClient;
        this.recipeDAO = recipeDAO;
        this.botStateContextDAO = botStateContextDAO;
        this.botConfig = botConfig;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
    }

    public BotApiMethod<?> handle(Update update) {
        InlineQuery inlineQuery = update.getInlineQuery();
        String query = inlineQuery.getQuery();
        long chatId = inlineQuery.getFrom().getId();
        BotStateContext botStateContext = botStateContextDAO.findBotStateContextById(inlineQuery.getFrom().getId());

        if (query.contains("/edit_recipe")) {
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("Сотрите имя бота и отправьте отредактированный рецепт \uD83D\uDC47")
                    .replyMarkup(inlineKeyboardMaker.getCancelKeyboard()).build();
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
                Collection<? extends InlineQueryResult> inlineQueryResultList = getInlineQueryResultList(query);
                String id = inlineQuery.getId();
                telegramClient.execute(AnswerInlineQuery.builder().inlineQueryId(id).results(inlineQueryResultList).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));

            }
        }

        return null;
    }

    private Collection<? extends InlineQueryResult> getInlineQueryResultList(String query) {
        Set<InlineQueryResult> inlineQueryResults = new HashSet<>();

        List<Recipe> recipes = recipeDAO.findRecipesByString(query);

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
