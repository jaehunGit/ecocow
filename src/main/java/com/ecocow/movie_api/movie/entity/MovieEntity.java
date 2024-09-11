package com.ecocow.movie_api.movie.entity;

import lombok.*;
import javax.persistence.*;
@Entity(name = "movie")
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class MovieEntity {

    @Id
    private Long id;

    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private float voteAverage;
    private int voteCount;
    private float popularity;
    private String originalLanguage;
    private String releaseDate;

    @Transient
    private Integer matchingGenres;
}
