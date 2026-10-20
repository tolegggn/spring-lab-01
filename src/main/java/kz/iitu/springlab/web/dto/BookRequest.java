package kz.iitu.springlab.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.iitu.springlab.catalog.Genre;

@Schema(description = "Request body for creating or updating a book")
public record BookRequest(
        @NotBlank
        @Size(max = 120)
        @Schema(description = "Title of the book", example = "The Pragmatic Programmer", maxLength = 120)
        String title,

        @NotBlank
        @Schema(description = "Author of the book", example = "David Thomas")
        String author,

        @Min(1450)
        @Max(2100)
        @Schema(description = "Year of publication (between 1450 and 2100)", example = "1999", minimum = "1450", maximum = "2100")
        int year,

        @NotNull
        @Schema(description = "Genre of the book", example = "NON_FICTION")
        Genre genre) {
}
