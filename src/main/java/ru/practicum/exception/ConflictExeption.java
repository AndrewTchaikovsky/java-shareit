package ru.practicum.exception;

public class ConflictExeption extends RuntimeException {
    public ConflictExeption(String message) {
        super(message);
    }
}
