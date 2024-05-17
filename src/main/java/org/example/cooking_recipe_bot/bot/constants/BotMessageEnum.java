package org.example.cooking_recipe_bot.bot.constants;

import lombok.Getter;

@Getter
public enum BotMessageEnum {
    HELP_MESSAGE("""
            \uD83D\uDC4B я бот для поиска рецептов

            ❗ <b>Что Вы можете сделать:</b>
            ✅ Найти рецепты из базы по ключевому слову или по ингредиенту. Для этого отправьте сообщение боту
            ✅ Найти случайный рецепт
            ✅ Найти рецепты по категориям

            В моей базе рецептов уже добавлены самые крутые рецепты
            Удачи!

            Воспользуйтесь клавиатурой, чтобы начать работу или напишите что вы хотите найти\uD83D\uDC47
            """),
    EXCEPTION_ILLEGAL_MESSAGE("Нет, к такому меня не готовили! Отправьте текст, фото или видео"),
    EXCEPTION_UPDATE_HANDLE("Что-то пошло не так. Обратитесь к программисту"),
    INSERT_RECIPE_MESSAGE("""
            Отправьте рецепт в формате:
            Название

            • ингредиент
            • ингредиент
            • ингредиент

            Инструкции по приготовлению

            #хэштег #хэштег #хэштег
            """),
    RECIPE_NOT_FOUND("Рецепт не найден"),
    RECIPE_ADDED("Рецепт добавлен в базу данных"),
    RECIPE_PARSING_ERROR("Не удалось распарсить рецепт. Проверьте соответствие рецепта шаблону"),
    RECIPE_ALREADY_EXISTS("Рецепт с таким названием уже существует. Добавьте другой"),
    RECIPE_SAVING_ERROR("Не удалось сохранить рецепт"),
    RECIPE_SENDING_ERROR("Не удалось отправить рецепт"),
    INSERT_NOTIFICATION_MESSAGE("Отправьте текст уведомления или напишите /cancel для отмены"),
    CANCEL_MESSAGE("Действие отменено"),
    USER_NOT_FOUND("Пользователь не найден ");

    private final String message;

    BotMessageEnum(String message) {
        this.message = message;
    }

}
