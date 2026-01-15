package ru.yandex.practicum.catsgram.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.ImageFileException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Image;
import ru.yandex.practicum.catsgram.model.ImageData;
import ru.yandex.practicum.catsgram.model.Post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {
    private final Map<Long, Image> images = new HashMap<>();
    private Long id = 0L;
    private final PostService postService;
    private final String imagesDirectory = Paths.get("E:","lpr", "yandex", "files").toString();

    public ImageData getImageData(long imageId) {
        if (!images.containsKey(imageId)) {
            throw new NotFoundException("Изображение с id = " + imageId + " не найдено");
        }
        Image image = images.get(imageId);
        // загрузка файла с диска
        byte[] data = loadFile(image);

        return new ImageData(data, image.getOriginalFileName());
    }

    // получение данных об изображениях указанного поста
    public List<Image> getPostImages(long postId) {
        return images.values()
                .stream()
                .filter(image -> image.getPostId() == postId)
                .collect(Collectors.toList());
    }

    public List<Image> saveImages(long postId, List<MultipartFile> files) {
        return files.stream().map(file -> saveImage(postId, file))
                .collect(Collectors.toList());
    }

    private Image saveImage(long postId, MultipartFile file) {
        Post post = postService.getPostById(postId).orElseThrow(()-> new ConditionsNotMetException("Указанный пост не найден"));

        Path filepath;
        try {
            filepath = saveFile(file, post);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Long imageId = getNextId();

        Image image = new Image();
        image.setId(imageId);
        image.setPostId(postId);
        image.setOriginalFileName(file.getOriginalFilename());
        image.setFilePath(filepath.toString());

        images.put(imageId, image);

        return image;
    }

    private Path saveFile(MultipartFile file, Post post) throws IOException {

        String uniqueFileName = String.format("%d.%s", Instant.now().toEpochMilli(),
                StringUtils.getFilenameExtension(file.getOriginalFilename()));

        Path uploadPath = Paths.get(imagesDirectory, String.valueOf(post.getAuthorId()),
                String.valueOf(post.getId()));

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFileName);
        file.transferTo(filePath);
        return filePath;
    }

    private byte[] loadFile(Image image) {
        Path path = Paths.get(image.getFilePath());
        if (Files.exists(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                throw new ImageFileException("Ошибка чтения файла.  Id: " + image.getId()
                        + ", name: " + image.getOriginalFileName(), e);
            }
        } else {
            throw new ImageFileException("Файл не найден. Id: " + image.getId()
                    + ", name: " + image.getOriginalFileName());
        }
    }

    private long getNextId() {
        id++;
        return id;
    }

}
