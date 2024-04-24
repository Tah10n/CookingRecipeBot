package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.db.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDAO {
   private String botOwner;

    private UsersRepository usersRepository;

    public UserDAO(UsersRepository usersRepository, @Value("${telegrambot.botowner}") String botOwner) {
        this.usersRepository = usersRepository;
        this.botOwner = botOwner;
    }


    public void saveUser(User user) {
        usersRepository.save(user);
    }

    public void deleteUser(long userId) {
        usersRepository.deleteById(userId);
    }

    public User getUserByUserName(String userName) {
        return usersRepository.findByUserName(userName);
    }

    public User getUserById(long userId) {
        return usersRepository.findById(userId).orElse(null);
    }

    public boolean isFirstAdmin(User user) {
            if (user.getUserName().equals(botOwner)) {
                return true;
            }
        return false;
    }

    public Optional<User> findById(Long userId) {
        return Optional.of(usersRepository.findById(userId)).orElse(Optional.empty());
    }

    public List<User> findAllUsers() {
        return usersRepository.findAll();
    }

    public void setAdmin(long userId) {
        User user = usersRepository.findById(userId).get();
        user.setIsAdmin(true);
        usersRepository.save(user);
    }

    public void unsetAdmin(long userId) {
        User user = usersRepository.findById(userId).get();
        user.setIsAdmin(false);
        usersRepository.save(user);
    }

}
