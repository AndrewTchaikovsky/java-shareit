package ru.practicum.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final List<User> users = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);

    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        users.add(user);
        return user;
    }
}
