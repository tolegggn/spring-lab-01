package kz.iitu.springlab.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.iitu.springlab.catalog.Genre;

import java.time.Instant;

@Schema(description = "Response representation of a book resource")
public record BookResponse(
        @Schema(description = "Unique database identifier of the book", example = "1")
        Long id,

        @Schema(description = "Title of the book", example = "The Pragmatic Programmer")
        String title,

        @Schema(description = "Author of the book", example = "David Thomas")
        String author,

        @Schema(description = "Year of publication", example = "1999")
        int year,

        @Schema(description = "Genre of the book", example = "NON_FICTION")
        Genre genre,

        @Schema(description = "Timestamp when the book was created on the server", example = "2026-10-20T07:30:00Z")
        Instant createdAt) {
}
