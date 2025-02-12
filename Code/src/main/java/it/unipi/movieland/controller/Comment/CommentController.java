package it.unipi.movieland.controller.Comment;

import it.unipi.movieland.model.Comment.Comment;
import it.unipi.movieland.service.Comment.CommentService;
import it.unipi.movieland.model.Post.Post;
import it.unipi.movieland.repository.Post.PostMongoDBRepository;
import it.unipi.movieland.repository.User.UserMongoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService, UserMongoDBRepository userRepository, PostMongoDBRepository postRepository)
    {
        this.commentService = commentService;
    }

    //ENDPOINT PER CREARE UN COMMENTO (MONGODB)
    @PostMapping
    public Comment createComment(
            @RequestParam String text,
            @RequestParam String authorId,
            @RequestParam String postId)
    {
        return commentService.createComment(text,authorId,postId);
    }

    //ENDPOINT PER RECUPERARE TUTTI I COMMENTI (MONGODB)
    @GetMapping
    public Page<Comment> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size)
    {
        return commentService.getAllComments(page, size);
    }

    //ENDPOINT PER RECUPERARE UN COMMENTO PER ID (MONGODB)
    @GetMapping("/{id}")
    public Comment getCommentById(
            @PathVariable String id)
    {
        return commentService.getCommentById(id);
    }

    //ENDPOINT PER ELIMINARE UN COMMENTO PER ID (MONGODB)
    @DeleteMapping("/{id}")
    public void deleteComment(
            @PathVariable String id)
    {
        commentService.deleteComment(id);
    }

    //ENDPOINT PER MODIFICARE UN COMMENTO PER ID (MONGODB)
    @PutMapping("/{id}")
    public Comment updateComment(
            @PathVariable String id,
            @RequestParam String text)
    {
        return commentService.updateComment(id, text);
    }

    //ENDPOINT PER OTTENERE COMMENTI PER AUTORE CON PAGINAZIONE
    @GetMapping("/author/{author}")
    public Page<Comment> getCommentsByAuthor(
            @PathVariable String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        return commentService.getCommentsByAuthor(author, page, size);
    }

    //ENDPOINT PER CERCARE I COMMENTI IN UN RANGE DI DATE
    @GetMapping("/byDateRange")
    public Page<Comment> getCommentsByDateRange(
            @Parameter(description = "Start data in formato 'yyyy-MM-ddTHH:mm:ss'")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDate,

            @Parameter(description = "End data in formato 'yyyy-MM-ddTHH:mm:ss'")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.getCommentsByDateRange(startDate, endDate, page, size);
    }
}