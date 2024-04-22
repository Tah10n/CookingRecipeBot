package org.example.cooking_recipe_bot.db.repository;

import org.example.cooking_recipe_bot.db.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends MongoRepository<User, Long> {
    User findByUserName(String userName);

}

