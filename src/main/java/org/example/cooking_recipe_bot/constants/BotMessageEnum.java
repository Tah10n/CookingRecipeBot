package org.example.cooking_recipe_bot.constants;

public enum BotMessageEnum {
    HELP_MESSAGE("\uD83D\uDC4B Привет, я бот для поиска рецептов\n\n" +
            "❗ *Что Вы можете сделать:*\n" +
            "✅ Найти рецепты из базы по ключевому слову или по ингредиенту. Для этого отправьте сообщение боту\n" +
            "✅ Найти случайный рецепт\n" +
            "✅ Найти рецепты по категориям\n" + "\n" +
            "В моей базе рецептов уже добавлены самые крутые рецепты\n" +
            "Удачи!\n\n" +
            "Воспользуйтесь клавиатурой, чтобы начать работу или напишите что вы хотите найти\uD83D\uDC47"),
    EXCEPTION_ILLEGAL_MESSAGE("Нет, к такому меня не готовили! Я работаю или с текстом, или с файлом"),
    EXCEPTION_WHAT_THE_FUCK("Что-то пошло не так. Обратитесь к программисту");

    private final String message;

    BotMessageEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
