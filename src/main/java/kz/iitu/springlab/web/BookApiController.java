package kz.iitu.springlab.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookNotFoundException;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.web.dto.BookMapper;
import kz.iitu.springlab.web.dto.BookRequest;
import kz.iitu.springlab.web.dto.BookResponse;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Books", description = "The catalogue of books")
@RestController
@RequestMapping("/api/books")
public class BookApiController {

    private final BookService service;
    private final BookMapper mapper;

    public BookApiController(BookService service, BookMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "List all books with optional filtering and sorting")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "List of books matching filter and sort criteria",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookResponse.class)))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid sort field or invalid filter parameter",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    @GetMapping // 200
    public List<BookResponse> list(
            @Parameter(description = "Filter books by author substring (case-insensitive)", example = "Bloch")
            @RequestParam(required = false) String author,
            @Parameter(description = "Filter books by genre", example = "NOVEL")
            @RequestParam(required = false) String genre,
            @Parameter(description = "Sort by field and direction, format: field[,asc|desc]", example = "year,desc")
            @RequestParam(required = false) String sort) {
        return mapper.toResponseList(service.findAll(author, genre, sort));
    }

    @Operation(summary = "Find a book by its identifier")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "The book was found",
                content = @Content(schema = @Schema(implementation = BookResponse.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "No book with this id",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    @GetMapping("/{id}") // 200 or 404
    public BookResponse find(
            @Parameter(description = "Identifier of the book", example = "999")
            @PathVariable long id) {
        return mapper.toResponse(service.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id)));
    }

    @Operation(summary = "Create a new book")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "The book was successfully created",
                headers = @Header(name = "Location", description = "URI of the newly created book", schema = @Schema(type = "string", example = "/api/books/6")),
                content = @Content(schema = @Schema(implementation = BookResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error in request body",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Duplicate book: a book with the same title and author already exists",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    @PostMapping // 201 + Location
    public ResponseEntity<BookResponse> create(
            @Valid @RequestBody BookRequest request) {
        Book saved = service.create(mapper.toDomain(request));
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.id()))
                .body(mapper.toResponse(saved));
    }

    @Operation(summary = "Update an existing book by identifier")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "The book was successfully updated",
                content = @Content(schema = @Schema(implementation = BookResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error in request body",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "No book with this id exists",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Duplicate book: updating would conflict with another book's title and author",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    @PutMapping("/{id}") // 200 or 404
    public BookResponse update(
            @Parameter(description = "Identifier of the book to update", example = "1")
            @PathVariable long id,
            @Valid @RequestBody BookRequest request) {
        return mapper.toResponse(service.update(id, mapper.toDomain(request)));
    }

    @Operation(summary = "Delete a book by its identifier")
    @ApiResponses({
        @ApiResponse(
                responseCode = "204",
                description = "The book was deleted or was already absent (idempotent operation)"
        )
    })
    @DeleteMapping("/{id}") // 204
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifier of the book to delete", example = "1")
            @PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
