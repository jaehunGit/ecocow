package com.ecocow.movie_api.movie.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenreDTO {

    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;
}
