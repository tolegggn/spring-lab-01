package kz.iitu.springlab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
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
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void testGetBooksFilterAndLimit() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("author", "Bloch")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", containsString("Bloch")));
    }

    @Test
    void testGetBookByIdFoundAndNotFound() throws Exception {
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/api/books/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Domain-Driven Design\",\"author\":\"Eric Evans\",\"year\":2003}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Domain-Driven Design"));
    }

    @Test
    void testDeleteTwice() throws Exception {
        mockMvc.perform(delete("/api/books/2"))
                .andExpect(status().isNoContent());

        // Deleting again should return 404
        mockMvc.perform(delete("/api/books/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testVariant2ByYear() throws Exception {
        // Valid 4-digit year -> 200 OK
        mockMvc.perform(get("/api/books/by-year/2018"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].year").value(2018));

        // Missing year filter -> 200 OK with empty array
        mockMvc.perform(get("/api/books/by-year/1990"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Non-matching path (letters, 2 digits, 5 digits) -> 404 Not Found
        mockMvc.perform(get("/api/books/by-year/abcd"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/books/by-year/20"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/books/by-year/20261"))
                .andExpect(status().isNotFound());
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
                        .param("year", "2018"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attribute("message", containsString("Refactoring")));
    }
}
