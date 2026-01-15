package ru.yandex.practicum.catsgram.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.service.PostService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @Autowired
    PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{id}")
    public Optional<Post> getPostByid(@PathVariable("id") Long postId) {
        return postService.getPostById(postId);
    }

    @GetMapping
    public Collection<Post> findAll(@RequestParam(defaultValue = "10") int size,
                                    @RequestParam(name = "from", defaultValue = "0") int from,
                                    @RequestParam(name = "sort", defaultValue = "desc") String sortOrder) {

        if (!sortOrder.equals("asc") && !sortOrder.equals("desc")) {
            throw new ParameterNotValidException("sort", "Некорректный параметр сортировки. Может быть asc и desc.");
        }
        if (size <= 0) {
            throw new ParameterNotValidException("size", "Некорректный размер выборки. Размер должен быть больше нуля.");
        }
        if (from < 0) {
            throw new ParameterNotValidException("from", "from должен быть >= 0.");
        }

        return postService.findAll(size, from, sortOrder);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Post create(@RequestBody Post post) {
        return postService.create(post);
    }

    @PutMapping
    public Post update(@RequestBody Post newPost) {
        return postService.update(newPost);
    }
}