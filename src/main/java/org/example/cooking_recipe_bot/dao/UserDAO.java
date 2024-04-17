package org.example.cooking_recipe_bot.dao;

import org.example.cooking_recipe_bot.entity.User;
import org.example.cooking_recipe_bot.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDAO {
    @Value("${telegrambot.botowner}")
    private String botOwner;

    private UsersRepository usersRepository;

    public UserDAO(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
        System.out.println(botOwner);
    }

    public List<User> getAllUsers() {
        return (List<User>) usersRepository.findAll();
    }

    public void saveUser(User user) {
        usersRepository.save(user);
    }

    public void deleteUser(User user) {
        usersRepository.delete(user);
    }

    public User getUserByUserName(String userName) {
        return usersRepository.findByUserName(userName);
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
}
