package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getAllUsers() {
        return repository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        repository.findByEmail(userDto.getEmail())
                .ifPresent(u -> {
                    throw new ConflictException("Email already exists");
                });

        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(repository.save(user));
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {

            repository.findByEmail(userDto.getEmail())
                    .ifPresent(u -> {
                        throw new ConflictException("Email already exists");
                    });
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        return UserMapper.toUserDto(repository.save(existingUser));
    }

    @Override
    public void deleteUser(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        repository.deleteById(id);
    }
}



