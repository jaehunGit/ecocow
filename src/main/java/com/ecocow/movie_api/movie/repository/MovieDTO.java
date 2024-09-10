package com.ecocow.movie_api.movie.repository;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MovieDTO {
    private Long id;
    private String title;
    private String overview;

    @JsonProperty("tdb_id")
    private Long tdbId;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("vote_average")
    private double voteAverage;

    @JsonProperty("vote_count")
    private int voteCount;

    private double popularity;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("vote_percentage")
    private double votePercentage;

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;
}


