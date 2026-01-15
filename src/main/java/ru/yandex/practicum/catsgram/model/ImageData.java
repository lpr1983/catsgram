package ru.yandex.practicum.catsgram.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ImageData {
    private final byte[] data;
    private final String name;
}