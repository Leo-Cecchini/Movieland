package it.unipi.movieland.utils.deserializers;

import com.google.gson.*;
import it.unipi.movieland.model.CountryEnum;
import it.unipi.movieland.model.GenreEnum;
import it.unipi.movieland.model.Movie.Movie;
import it.unipi.movieland.model.TitleTypeEnum;
import org.apache.commons.lang3.EnumUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TitleDeserializer implements JsonDeserializer<Movie> {
    @Override
    public Movie deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String title = getAsString(jsonObject, "title");
        Integer release_year = getAsInt(jsonObject, "year");
        String description = getAsString(jsonObject, "description");
        Integer runtime = getAsInt(jsonObject, "runtime");
        String _id = getNestedAsString(jsonObject, "ids", "imdb");
        TitleTypeEnum type = TitleTypeEnum.valueOf(Objects.requireNonNull(getAsString(jsonObject, "type")).toUpperCase());

        // imdb_score and imdb_votes
        Integer imdb_score = null;
        Integer imdb_votes = null;
        if (jsonObject.has("ratings")) {
            JsonArray ratings = jsonObject.getAsJsonArray("ratings");
            for (JsonElement ratingElement : ratings) {
                JsonObject ratingObj = ratingElement.getAsJsonObject();
                if ("imdb".equalsIgnoreCase(getAsString(ratingObj, "source"))) {
                    imdb_score = getAsInt(ratingObj, "score");
                    imdb_votes = getAsInt(ratingObj, "votes");
                    break;
                }
            }
        }

        // extract genres as list<String>  (toLowerCase)
        List<GenreEnum> genres = new ArrayList<>();
        if (jsonObject.has("genres")) {
            JsonArray genresArray = jsonObject.getAsJsonArray("genres");
            for (JsonElement genreElement : genresArray) {
                String genreStr = getAsString(genreElement.getAsJsonObject(), "title").toUpperCase();
                if (EnumUtils.isValidEnum(GenreEnum.class, genreStr)) {
                    genres.add(GenreEnum.valueOf(genreStr));
                }
            }
        }

        // extract country in maiusc
        List<CountryEnum> production_countries = new ArrayList<>();
        String countryValue = getAsString(jsonObject, "country");
        if (countryValue != null) {
            production_countries.add(CountryEnum.valueOf(countryValue.toUpperCase()));
        }

        String age_certification = getAsString(jsonObject, "certification");
        String poster_path = getAsString(jsonObject, "poster");

        return new Movie(title, release_year, description, runtime, _id, type, imdb_score, imdb_votes, genres, production_countries, age_certification, poster_path);
    }

    private String getAsString(JsonObject obj, String memberName) {
        return obj.has(memberName) && !obj.get(memberName).isJsonNull() ? obj.get(memberName).getAsString() : null;
    }

    private Integer getAsInt(JsonObject obj, String memberName) {
        return obj.has(memberName) && !obj.get(memberName).isJsonNull() ? obj.get(memberName).getAsInt() : null;
    }

    private String getNestedAsString(JsonObject obj, String parentKey, String childKey) {
        if (obj.has(parentKey) && obj.get(parentKey).isJsonObject()) {
            JsonObject nestedObj = obj.getAsJsonObject(parentKey);
            return getAsString(nestedObj, childKey);
        }
        return null;
    }
}
