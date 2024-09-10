package com.ecocow.movie_api.movie.repository;

import lombok.Data;

import java.util.List;

@Data
public class MovieDTOList {
    private List<MovieDTO> results;
}
