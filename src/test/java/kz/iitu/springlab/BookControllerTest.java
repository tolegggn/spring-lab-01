package kz.iitu.springlab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testGetBooksList() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void testGetBooksFilterByAuthor() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("author", "Bloch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", containsString("Bloch")));
    }

    @Test
    void testGetBookByIdFoundAndNotFound() throws Exception {
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Effective Java"));

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.type").value("https://api.iitu.kz/errors/book-not-found"))
                .andExpect(jsonPath("$.detail", containsString("999")));
    }

    @Test
    void testCreateBookSuccess() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Domain-Driven Design\",\"author\":\"Eric Evans\",\"year\":2003,\"genre\":\"NON_FICTION\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/books/\\d+")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Domain-Driven Design"))
                .andExpect(jsonPath("$.author").value("Eric Evans"))
                .andExpect(jsonPath("$.year").value(2003))
                .andExpect(jsonPath("$.genre").value("NON_FICTION"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testCreateBookValidationError() throws Exception {
        // Empty title violates @NotBlank
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"author\":\"Someone\",\"year\":2020,\"genre\":\"FICTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testCreateBookDuplicateConflict() throws Exception {
        // Attempting to create book that already exists with same title & author
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Clean Code\",\"author\":\"Robert Martin\",\"year\":2008,\"genre\":\"NON_FICTION\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.type").value("https://api.iitu.kz/errors/duplicate-title"))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void testUpdateBook() throws Exception {
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Effective Java 3rd Ed\",\"author\":\"Joshua Bloch\",\"year\":2018,\"genre\":\"NON_FICTION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Effective Java 3rd Ed"));

        mockMvc.perform(put("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Unknown Book\",\"author\":\"Unknown Author\",\"year\":2024,\"genre\":\"FICTION\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testDeleteTwice() throws Exception {
        // First DELETE returns 204 No Content
        mockMvc.perform(delete("/api/books/2"))
                .andExpect(status().isNoContent());

        // Second DELETE returns 204 No Content (idempotent REST design)
        mockMvc.perform(delete("/api/books/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testVariant2SortingAndFiltering() throws Exception {
        // ?sort=year,desc&genre=NOVEL
        // Seeds have "To Kill a Mockingbird" (1960) and "1984" (1949)
        mockMvc.perform(get("/api/books")
                        .param("sort", "year,desc")
                        .param("genre", "NOVEL"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("To Kill a Mockingbird"))
                .andExpect(jsonPath("$[0].year").value(1960))
                .andExpect(jsonPath("$[1].title").value("1984"))
                .andExpect(jsonPath("$[1].year").value(1949));
    }

    @Test
    void testVariant2UnknownSortFieldReturns400() throws Exception {
        // Unknown sort field gives 400 with a problem detail
        mockMvc.perform(get("/api/books")
                        .param("sort", "price,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid sort parameter"))
                .andExpect(jsonPath("$.type").value("https://api.iitu.kz/errors/invalid-sort"))
                .andExpect(jsonPath("$.invalidField").value("price"));
    }

    @Test
    void testMvcPageRendering() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books", "title"))
                .andExpect(content().string(containsString("Catalogue")));
    }

    @Test
    void testPostRedirectGet() throws Exception {
        mockMvc.perform(post("/books")
                        .param("title", "Refactoring")
                        .param("author", "Martin Fowler")
                        .param("year", "2018")
                        .param("genre", "NON_FICTION"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attribute("message", containsString("Refactoring")));
    }
}
