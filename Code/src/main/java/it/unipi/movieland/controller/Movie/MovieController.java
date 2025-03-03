package it.unipi.movieland.controller.Movie;

import it.unipi.movieland.dto.*;
import it.unipi.movieland.model.CountryEnum;
import it.unipi.movieland.model.GenreEnum;
import it.unipi.movieland.model.PlatformEnum;
import it.unipi.movieland.model.TitleTypeEnum;
import it.unipi.movieland.service.Movie.*;
import it.unipi.movieland.model.Movie.Movie;
import it.unipi.movieland.service.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/titles")
public class MovieController {

    @Autowired
    private MovieService movieService;

    // Search movie by id
    @GetMapping("/search/{movieId}")
    public ResponseEntity<Movie> getMovieById(@PathVariable String movieId) {
        Optional<Movie> movie = movieService.getMovieById(movieId);
        return movie.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Search title by word
    @GetMapping("/search/titleOrKeyword/{label}")
    public ResponseEntity<ResponseWrapper<Page<SearchTitleDTO>>> getMovieByTitle(
            @RequestParam("type") TitleTypeEnum type,
            @RequestParam String label,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<SearchTitleDTO> movie = movieService.getMovieByTitleOrKeyword(type, label, page, size);
            if(movie.isEmpty())
                return ResponseEntity.ok(new ResponseWrapper<>("Titles fetched successfully, " +
                        "but no titles found", movie));
            return ResponseEntity.ok(new ResponseWrapper<>("Titles fetched successfully", movie));
        }catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>("Error searching title: " + e.getMessage(), null));
        }
    }

    //search with filters
    @GetMapping("/search/withFilters")
    public ResponseEntity<ResponseWrapper<Page<SearchTitleDTO>>> getMovieByTitleOrKeywordWithFilters(
            @RequestParam("type") TitleTypeEnum type,
            @RequestParam("label") Optional<String> label,
            @RequestParam("genre") Optional<List<GenreEnum>> genre,
            @RequestParam("release_year") Optional<Integer> release_year,
            @RequestParam("platform") Optional<PlatformEnum> platform,
            @RequestParam("production_countries") Optional<CountryEnum> production_countries,
            @RequestParam("age_certification") Optional<String> age_certification,
            @RequestParam("imdb_votes") Optional<Integer> imdb_votes,
            @RequestParam("imdb_scores") Optional<Integer> imdb_scores,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ){
        try {
            Page<SearchTitleDTO> movie = movieService.getTitleWithFilters(type, label, genre, release_year, platform, production_countries, age_certification, imdb_scores, imdb_votes, page, size);
            if(movie.isEmpty())
                return ResponseEntity.ok(new ResponseWrapper<>("Titles fetched successfully, " +
                        "but no titles found", movie));
            return ResponseEntity.ok(new ResponseWrapper<>("Titles fetched successfully", movie));
        }catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>("Error searching titles: " + e.getMessage(), null));
        }
    }

    // Get all movie reviews
    /*@GetMapping("/allReviews/{movieId}")
    public ResponseEntity<List<Review>> getAllReviewsByMovieId(@PathVariable String movieId) {
        Optional<List<Review>> review = movieService.getReviewsByMovieId(movieId);
        return review.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }*/

    // Search new title by name (and by year...released before a specific year)
    @GetMapping("/searchNew/titleIdByName")
    public ResponseEntity<ResponseWrapper<List<SearchNewTitleDTO>>> searchNewTitleByName(
            @RequestParam("type") TitleTypeEnum type,
            @RequestParam("title") String title,
            @RequestParam("year") Optional<Integer> year
            ){
        try {

            Map<Integer, List<SearchNewTitleDTO>> movies = movieService.searchNewTitleByName(type, title, year);
            Integer result = movies.keySet().stream().findFirst().orElse(null);
            return switch(result){
                case 0 -> ResponseEntity.ok(new ResponseWrapper<>("Titles fetched successfully", movies.get(0)));
                case 1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseWrapper<>(type + " with title " + title + " not found", movies.get(1)));
                default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseWrapper<>("An error occurred with the api ", null));
            };
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>("Error searching titles: " + e.getMessage(), null));
        }
    }

    //Add title by id
    @PutMapping("/addTitleById")
    public ResponseEntity<ResponseWrapper<Integer>> addTitleById(
            @RequestParam TitleTypeEnum type,
            @RequestParam String id) {
        try {
            int result = movieService.addTitleById(type, id);
            return switch (result) {
                case 0 -> ResponseEntity.ok(new ResponseWrapper<>(type + " added successfully", result));
                case 1 ->
                        ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseWrapper<>(type + " with id: " + id + " already exists in the database", null));
                case 2 ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseWrapper<>(type + " with id " + id + " not found", null));
                default ->
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseWrapper<>("Error adding title: " + id, null));
            };
        }catch (IOException | InterruptedException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>("Error searching titles: " + e.getMessage(), null));
        }
    }

    // Update title: general info
    @PutMapping("/updateTitle/{movieId}")
    public ResponseEntity<Object> updateMovie(
            @PathVariable String movieId,
            @RequestBody UpdateTitleDTO movie
    ) {
        boolean isUpdated = movieService.updateMovie(movieId, movie);
        if (isUpdated) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message", "Title updated successfully in both MongoDB and Neo4j"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Title with movieId " + movieId + " not found in one or both databases"));
        }
    }

    // Delete title
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.noContent().build();
    }

    // Add title actor
    @PutMapping("/addRole")
    public ResponseEntity<String> addRole(@RequestParam String movie_id, @RequestParam Integer actor_id, @RequestParam String name,
                                          @RequestParam String character) {
        int result = movieService.addRole(movie_id, actor_id, name, character);
        return switch (result) {
            case 0 -> ResponseEntity.ok("Role: '"+ character +"' for actor: "+ actor_id +" added successfully in title: "+movie_id);
            case 1 -> ResponseEntity.status(HttpStatus.CONFLICT).body("Actor already exists or no update was made");
            default -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Actor with id "+ movie_id +" not found!");
        };
    }

    // Update title actor
    @PutMapping("/updateRole")
    public ResponseEntity<String> updateRole(@RequestParam String movie_id, @RequestParam Integer actor_id, @RequestParam String name,
                                            @RequestParam String character) {
        int result = movieService.updateRole(movie_id, actor_id, name, character);
        if(result == 0)
            return ResponseEntity.ok("Role '"+ character +"' for actor: "+ actor_id +" updated successfully in title: "+movie_id);
        else
            return ResponseEntity.ok("Title with id "+ movie_id +" not found!");
    }

    // Remove title actor
    @PutMapping("/removeRole")
    public ResponseEntity<String> deleteRole(@RequestParam String movie_id, @RequestParam Integer actor_id) {
        int result = movieService.deleteRole(movie_id, actor_id);
        if( result == 0 )
            return ResponseEntity.ok("Role for actor: "+ actor_id +" deleted successfully in title: "+movie_id);
        else
            return  ResponseEntity.ok("Title with id "+ movie_id +" not found!");
    }


    // Add title director
    @PutMapping("/addDirector")
    public ResponseEntity<String> addDirector(@RequestParam String movie_id, @RequestParam Integer director_id, @RequestParam String name) {
        int result = movieService.addDirector(movie_id, director_id, name);
        return switch (result) {
            case 0 -> ResponseEntity.ok("Director "+ director_id +" added successfully to title "+ movie_id);
            case 1 -> ResponseEntity.ok("Director with id: "+ director_id +" already exists or no update was made");
            default -> ResponseEntity.ok("Title with id "+ movie_id +" not found!");
        };
    }
    // Remove title director
    @PutMapping("/updateDirector")
    public ResponseEntity<String> updateRole(@RequestParam String movie_id, @RequestParam Integer director_id, @RequestParam String name) {
        int result = movieService.updateDirector(movie_id, director_id, name);
        if ( result == 0 )
            return ResponseEntity.ok("Director "+ director_id +" of title "+ movie_id +" updated  successfully!");
        else
            return ResponseEntity.ok("Title with id "+ movie_id +" not found!");
    }
    // Update title director
    @PutMapping("/removeDirector")
    public ResponseEntity<String> deleteDirector(@PathVariable String movie_id, @PathVariable Integer director_id) {
        int result = movieService.deleteDirector(movie_id, director_id);
        if ( result == 0 )
            return ResponseEntity.ok("Director "+ director_id +" of title "+ movie_id +" removed successfully!");
        else
            return ResponseEntity.ok("Title with id "+ movie_id +" not found!");
    }
}