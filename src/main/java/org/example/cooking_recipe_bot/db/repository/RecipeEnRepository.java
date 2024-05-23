package org.example.cooking_recipe_bot.db.repository;

import org.example.cooking_recipe_bot.db.entity.RecipeEn;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeEnRepository extends RecipeRepository<RecipeEn> {


}
