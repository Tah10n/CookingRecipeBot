package org.example.cooking_recipe_bot.utils;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class RecipeParserTest {

    @Test
    void testParseRecipeFromString() {

        String recipeString = "Чечевичные оладьи\n" +
                "• Пол стакана колотой чечевицы (замочить на ночь или часа на 3 (см фото)\n" +
                "• 1 красный перец\n" +
                "• Лук репчатый\n" +
                "• Пучок любой зелени\n" +
                "• 1 яйцо\n" +
                "• Соль/перец/чеснок через чеснокодавку\n" +
                "Чечевицу промыть, лук и морковь порезать и быстро обжарить, соединить все кроме яйца и измельчить в блендере до однородной массы, добавить немного воды (~50мл). " +
                "Консистенция сметаны должна быть. Вмешать яйцо и жарить на ап сковородке (я первую партию на масле гхи, потом без). Огонь слабый, по ~3 минуты с каждой стороны.\n" +
                "С греческим йогуртом+ соевый просто ум отъесть ❤️‍🔥\n" +
                "#20минут #ужин #обед #пп";


        Recipe recipe = null;
        try {
            recipe = RecipeParser.parseRecipeFromString(recipeString);
        } catch (ParseException e) {
            e.printStackTrace();

        }


        assertEquals("чечевичные оладьи", recipe.getName());

        assertEquals("Пол стакана колотой чечевицы (замочить на ночь или часа на 3 (см фото)\n" +
                "1 красный перец\n" +
                "Лук репчатый\n" +
                "Пучок любой зелени\n" +
                "1 яйцо\n" +
                "Соль/перец/чеснок через чеснокодавку", recipe.getIngredients());

        assertEquals("Чечевицу промыть, лук и морковь порезать и быстро обжарить, соединить все кроме яйца и измельчить в блендере до однородной массы, добавить немного воды (~50мл). " +
                "Консистенция сметаны должна быть. Вмешать яйцо и жарить на ап сковородке (я первую партию на масле гхи, потом без). Огонь слабый, по ~3 минуты с каждой стороны.\n" +
                "С греческим йогуртом+ соевый просто ум отъесть ❤️‍🔥", recipe.getInstructions());

        assertEquals("20минут\n" +
                "ужин\n" +
                "обед\n" +
                "пп", recipe.getHashtags());

    }
    @Test
    void testParseRecipeFromString2() {
        String recipeString = "ЧЕСНОЧНОЕ МАСЛО\n" +
                "• пачка вкусного хорошего сливочного масла\n" +
                "• 2 чл крупной соли\n" +
                "• зелень, укроп/петрушка/лучок. прям пучок. мелко порезать\n" +
                "• 1/4 сока лимона\n" +
                "• чеснок зубчика 4-5 через чеснокодавилку\n" +
                "• розовый перец опционально\n" +
                "Масло при комнатной температуре пусть полежит пока не станет мягким, смешать вилкой все ингредиенты, выложить колбасой на пергамент, завернуть и заморозить. \n" +
                "при необходимости доставать и отрезать кусочек. \n" +
                "подходит для стейков/мяса/котлет/рыбы/кукурузы и овощей на гриле/картошке/ на тост … и везде оно будет идеальным дополнением.\n" +
                "#масло #закуска #заготовка #20минут";

        Recipe recipe = null;
        try {
            recipe = RecipeParser.parseRecipeFromString(recipeString);
        } catch (ParseException e) {
            e.printStackTrace();

        }

        assertEquals("чесночное масло", recipe.getName());
        assertEquals("пачка вкусного хорошего сливочного масла\n" +
                "2 чл крупной соли\n" +
                "зелень, укроп/петрушка/лучок. прям пучок. мелко порезать\n" +
                "1/4 сока лимона\n" +
                "чеснок зубчика 4-5 через чеснокодавилку\n" +
                "розовый перец опционально", recipe.getIngredients());
        assertEquals("Масло при комнатной температуре пусть полежит пока не станет мягким, смешать вилкой все ингредиенты, выложить колбасой на пергамент, завернуть и заморозить. \n" +
                "при необходимости доставать и отрезать кусочек. \n" +
                "подходит для стейков/мяса/котлет/рыбы/кукурузы и овощей на гриле/картошке/ на тост … и везде оно будет идеальным дополнением.", recipe.getInstructions());
        assertEquals("масло\nзакуска\nзаготовка\n20минут", recipe.getHashtags());
    }

    @Test
    void testParseRecipeFromString_EmptyRecipeString() {
        String recipeString = "";
        assertThrowsExactly(ParseException.class, () -> RecipeParser.parseRecipeFromString(recipeString));

    }

    @Test
    void testParseRecipeFromString_NoIngredients() {
        String recipeString = "Recipe Name\n\nInstructions\n#Hashtags";
        assertThrowsExactly(ParseException.class, () -> RecipeParser.parseRecipeFromString(recipeString));
    }

    @Test
    void testParseRecipeFromString_NoInstructions() {
        String recipeString = "Recipe Name\n• Ingredients\n#Hashtags";
        assertThrows(ParseException.class, () -> RecipeParser.parseRecipeFromString(recipeString));
    }

    @Test
    void testParseRecipeFromString_NoHashtags() {
        String recipeString = "Recipe Name\n• Ingredients\nInstructions";
        assertThrows(ParseException.class, () -> RecipeParser.parseRecipeFromString(recipeString));
    }

}