package com.ecocow.movie_api.common.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class ResponseMessage<T> {
    private HttpStatus statusCode;
    private String message;
    private T data;
}
