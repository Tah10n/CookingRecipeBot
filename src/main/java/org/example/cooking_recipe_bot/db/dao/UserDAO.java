package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.config.BotConfig;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.db.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserDAO {
    private final String botOwner;
    private final UsersRepository usersRepository;

    public UserDAO(UsersRepository usersRepository, BotConfig botConfig) {
        this.usersRepository = usersRepository;
        this.botOwner = botConfig.getBotOwner();
    }


    public User saveUser(User user) {
        return usersRepository.save(user);
    }

    public void deleteUser(long userId) {
        usersRepository.deleteById(userId);
    }

    public User getUserById(long userId) {
        return usersRepository.findById(userId).orElse(null);
    }

    public boolean isFirstAdmin(String userName) {
        if(userName == null) return false;
        return userName.equals(botOwner);
    }

    public Optional<User> findById(Long userId) {
        return Optional.of(usersRepository.findById(userId)).orElse(Optional.empty());
    }

    public List<User> findAllUsers() {
        List<User> all = usersRepository.findAll();
        List<User> result = new ArrayList<>();
        for (User user : all) {
            if (!(user.getIsUnsubscribed() != null && user.getIsUnsubscribed())) {

                result.add(user);
            }
        }
        return result;
    }

    public User setAdmin(long userId) {
        User user = usersRepository.findById(userId).get();
        user.setIsAdmin(true);
         return usersRepository.save(user);
    }

    public User unsetAdmin(long userId) {
        User user = usersRepository.findById(userId).get();
        user.setIsAdmin(false);
        return usersRepository.save(user);
    }

}
