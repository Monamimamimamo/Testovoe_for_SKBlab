package org.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class SignupResponse {
    @JsonProperty("successfully")
    private Boolean successfully;

    @JsonProperty("body")
    private String body;

    @JsonProperty("request")
    private SignupRequest request;

    // Конструктор для Jackson
    @JsonCreator
    public SignupResponse(@JsonProperty("successfully") Boolean successfully,
                          @JsonProperty("body") String body,
                          @JsonProperty("request") SignupRequest request) {
        this.successfully = successfully;
        this.body = body;
        this.request = request;
    }
}
