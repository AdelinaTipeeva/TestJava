package ru.example.filestorage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public record FileDto(
        @NotBlank(message = "{filestorage.file.error.title_is_blank}")
        String title,

        @NotNull(message = "{filestorage.file.error.date_is_null}")
        @JsonProperty("creation_date")
        @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
        LocalDateTime creationDate,

        String description,

        String content
) implements Serializable {
}
