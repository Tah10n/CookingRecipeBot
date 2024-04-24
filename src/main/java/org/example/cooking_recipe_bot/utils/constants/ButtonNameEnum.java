package org.example.cooking_recipe_bot.utils.constants;

public enum ButtonNameEnum {
    HELP_BUTTON("Помощь"),
    FIND_RECIPE_BUTTON("Найти рецепт"),
    BREAKFAST_BUTTON("Завтрак"),
    LUNCH_BUTTON("Обед"),
    DINNER_BUTTON("Ужин"),
    ADD_RECIPE_BUTTON("Добавить рецепт"),
    DELETE_RECIPE_BUTTON("Удалить рецепт"),
    FIND_RANDOM_RECIPE_BUTTON("Найти случайный рецепт"),
    USERS_BUTTON("Пользователи");

    private final String buttonName;

    ButtonNameEnum(String buttonName) {
        this.buttonName = buttonName;
    }

    public String getButtonName() {
        return buttonName;
    }

}
