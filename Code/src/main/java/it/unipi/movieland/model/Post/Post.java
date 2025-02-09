package it.unipi.movieland.model.Post;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Document(collection = "Posts")
public class Post {
    @Id
    @GeneratedValue
    private String _id;
    private LocalDateTime datetime;
    private String text;
    private String author;
    private String movie_id;
    private List<Response> response;

    // Costruttore
    public Post( LocalDateTime datetime, String text, String author, String movie_id, List<Response> response) {
        this.datetime = datetime;
        this.text = text;
        this.author = author;
        this.movie_id = movie_id;
        this.response = response != null ? new ArrayList<>(response) : new ArrayList<>();
    }

    // Getter e Setter
    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(String movie_id) {
        this.movie_id = movie_id;
    }

    public List<Response> getResponse() {
        return new ArrayList<>(response);
    }

    public void setResponse(List<Response> response) {
        this.response = response != null ? new ArrayList<>(response) : new ArrayList<>();
    }

    // Add a respose(comment) to the post
    public void addResponse(Response singleResponse) {
        if (singleResponse != null) {
            this.response.add(singleResponse);
        }
    }

    // Override di toString
    @Override
    public String toString() {
        return "Post{" +
                "_id='" + _id + '\'' +
                ", datetime='" + datetime + '\'' +
                ", text='" + text + '\'' +
                ", author='" + author + '\'' +
                ", movie_id='" + movie_id + '\'' +
                ", response=" + response +
                '}';
    }
}
