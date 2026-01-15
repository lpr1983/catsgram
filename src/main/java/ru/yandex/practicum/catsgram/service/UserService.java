package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    public Collection<User> all() {
        return users.values();
    }

    public User create(User newUser) {

        if (newUser.getEmail() == null || newUser.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        if (emails.contains(newUser.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        newUser.setId(getNextId());
        newUser.setRegistrationDate(Instant.now());

        users.put(newUser.getId(), newUser);
        emails.add(newUser.getEmail());

        return newUser;
    }

    public User update(User user) {

        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User storedUser = users.get(user.getId());
        if (storedUser == null) {
            throw new ConditionsNotMetException("Пользователь с таким Id не найден");
        }

        String storedEmail = storedUser.getEmail();
        String newEmail = user.getEmail();

        if (!storedEmail.equals(newEmail) && emails.contains(newEmail)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        if (newEmail != null) {
            storedUser.setEmail(newEmail);
        }
        if (user.getUsername() != null) {
            storedUser.setUsername(user.getUsername());
        }
        if (user.getPassword() != null) {
            storedUser.setPassword(user.getPassword());
        }

        return user;
    }

    public Optional<User> findUserById(long id) {
        User user = users.get(id);
        return Optional.ofNullable(user);
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
