package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Verification {
    @JsonProperty(required = true)
    String type;

    String jsonPath;

    String expectedValue;
    String headerName;
}
