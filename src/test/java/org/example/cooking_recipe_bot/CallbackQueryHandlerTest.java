package org.example.cooking_recipe_bot;

import org.example.cooking_recipe_bot.bot.ActionFactory;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.MessageTranslator;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.handlers.CallbackQueryHandler;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAOManager;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class CallbackQueryHandlerTest {

    @Mock
    private UserDAO userDAO;
    @Mock
    private ActionFactory actionFactory;
    @Mock
    private InlineKeyboardMaker inlineKeyboardMaker;
    @Mock
    private TelegramClient telegramClient;
    @Mock
    private RecipeDAOManager recipeDAOManager;
    @Mock
    private BotStateContextDAO botStateContextDAO;
    @Mock
    private MessageTranslator messageTranslator;
    @Mock
    private ReplyKeyboardMaker replyKeyboardMaker;

    @InjectMocks
    private CallbackQueryHandler callbackQueryHandler;


    @BeforeEach
    public void setUp() {
        try (AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void testHandleCallbackQueryNull() {
        Update update = mock(Update.class);
        when(update.getCallbackQuery()).thenReturn(null);

        assertNull(callbackQueryHandler.handle(update));
    }

    @Test
    void testHandleDeleteUserButton() throws TelegramApiException {
        String action = "delete_user_button";
        long chatId = 12345L;
        int messageId = 67890;
        long userId = 11111L;

        Update update = createMockUpdate(action, chatId, messageId, userId);
        User user = new User();
        user.setId(userId);
        user.setUserName("testUser");
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setIsAdmin(false);
        user.setLanguage("en");

        Message message = mock(Message.class);
        when(update.getCallbackQuery().getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(messageId);
        when(message.getFrom()).thenReturn(mock(org.telegram.telegrambots.meta.api.objects.User.class));
        when(message.getText()).thenReturn("id=" + userId + ", username=" + user.getUserName() + ", firstName=" + user.getFirstName() + ", lastName=" + user.getLastName() + ", isAdmin=" + user.getIsAdmin());

        when(userDAO.getUserById(userId)).thenReturn(user);
        when(messageTranslator.getMessage(BotMessageEnum.USER_WAS_DELETED_MESSAGE.name(), "en"))
                .thenReturn("User was deleted");

        callbackQueryHandler.handle(update);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        ArgumentCaptor<DeleteMessage> deleteMessageCaptor = ArgumentCaptor.forClass(DeleteMessage.class);

        verify(telegramClient).execute(sendMessageCaptor.capture());
        verify(telegramClient).execute(deleteMessageCaptor.capture());

        assertEquals("User was deleted", sendMessageCaptor.getValue().getText());
        assertEquals(String.valueOf(chatId), deleteMessageCaptor.getValue().getChatId());
        assertEquals(messageId, deleteMessageCaptor.getValue().getMessageId());
    }

    @Test
    void testHandleSetAdminButton() throws TelegramApiException {
        String action = "set_admin_button";
        long chatId = 12345L;
        int messageId = 67890;
        long userId = 11111L;
        long userIdFromMessage = 22222L;

        Update update = createMockUpdate(action, chatId, messageId, userId);
        User userRequester = new User();
        userRequester.setId(userId);
        userRequester.setUserName("requester");
        userRequester.setLanguage("en");

        User userFromMessage = new User();
        userFromMessage.setId(userIdFromMessage);
        userFromMessage.setUserName("newAdmin");

        Message message = mock(Message.class);
        when(update.getCallbackQuery().getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(messageId);
        when(message.getFrom()).thenReturn(mock(org.telegram.telegrambots.meta.api.objects.User.class));
        when(message.getText()).thenReturn("id=" + userId + ", username=" + userFromMessage.getUserName() + ", firstName=" + userFromMessage.getFirstName() + ", lastName=" + userFromMessage.getLastName() + ", isAdmin=" + userFromMessage.getIsAdmin());

        when(userDAO.getUserById(userId)).thenReturn(userRequester);
        when(userDAO.setAdmin(any(Long.class))).thenReturn(userFromMessage);
        when(messageTranslator.getMessage(BotMessageEnum.USER_WAS_SET_ADMIN_MESSAGE.name(), "en"))
                .thenReturn("User was set as admin");



        callbackQueryHandler.handle(update);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        ArgumentCaptor<EditMessageText> editMessageTextCaptor = ArgumentCaptor.forClass(EditMessageText.class);

        verify(telegramClient).execute(sendMessageCaptor.capture());
        verify(telegramClient).execute(editMessageTextCaptor.capture());

        assertEquals("User was set as admin", sendMessageCaptor.getValue().getText());
        assertEquals(String.valueOf(chatId), editMessageTextCaptor.getValue().getChatId());
        assertEquals(messageId, editMessageTextCaptor.getValue().getMessageId());
    }



    private Update createMockUpdate(String action, long chatId, int messageId, long userId) {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn(action);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getFrom()).thenReturn(new org.telegram.telegrambots.meta.api.objects.User(userId, "firstName", false));
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(messageId);

        return update;
    }
}
