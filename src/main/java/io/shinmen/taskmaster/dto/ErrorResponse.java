package io.shinmen.taskmaster.dto;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private HttpStatus status;

    private String message;

    private ZonedDateTime timestamp;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> details;
}
