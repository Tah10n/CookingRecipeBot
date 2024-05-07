package org.example.cooking_recipe_bot.utils.constants;

public enum BotMessageEnum {
    HELP_MESSAGE("\uD83D\uDC4B я бот для поиска рецептов\n\n" +
            "❗ *Что Вы можете сделать:*\n" +
            "✅ Найти рецепты из базы по ключевому слову или по ингредиенту. Для этого отправьте сообщение боту\n" +
            "✅ Найти случайный рецепт\n" +
            "✅ Найти рецепты по категориям\n" + "\n" +
            "В моей базе рецептов уже добавлены самые крутые рецепты\n" +
            "Удачи!\n\n" +
            "Воспользуйтесь клавиатурой, чтобы начать работу или напишите что вы хотите найти\uD83D\uDC47"),
    EXCEPTION_ILLEGAL_MESSAGE("Нет, к такому меня не готовили! Я работаю или с текстом, или с файлом"),
    EXCEPTION_WHAT_THE_FUCK("Что-то пошло не так. Обратитесь к программисту"),
    INSERT_RECIPE_MESSAGE("Отправьте рецепт в формате:\n" +
            "Название\n\n" +
            "• ингредиент\n" +
            "• ингредиент\n" +
            "• ингредиент\n\n" +
            "Инструкции по приготовлению\n\n" +
            "#хэштег #хэштег #хэштег"),
    RECIPE_NOT_FOUND("Рецепт не найден"),
    RECIPE_ADDED("Рецепт добавлен в базу данных"),
    RECIPE_PARSING_ERROR("Не удалось распарсить рецепт. Проверьте соответствие рецепта шаблону"),
    RECIPE_ALREADY_EXISTS("Рецепт с таким названием уже существует. Добавьте другой"),
    RECIPE_SAVING_ERROR("Не удалось сохранить рецепт"),
    RECIPE_SENDING_ERROR("Не удалось отправить рецепт"), INSERT_NOTIFICATION_MESSAGE("Отправьте текст уведомления");

    private final String message;

    BotMessageEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
