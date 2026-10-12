package kz.iitu.springlab;

import kz.iitu.springlab.catalog.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Locale;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookFormValidationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testGetNewFormContainsEmptyFormAndGenres() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("form"))
                .andExpect(model().attributeExists("genres"));
    }

    @Test
    void testEmptyFormSubmissionRedisplaysWithErrors() throws Exception {
        mockMvc.perform(post("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeErrorCount("form", 4)); // title, author, year (0 out of range), genre
    }

    @Test
    void testYearOutOfRangeAndNoGenre() throws Exception {
        mockMvc.perform(post("/books")
                        .param("title", "Java Concurrency")
                        .param("author", "Brian Goetz")
                        .param("year", "1200"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasFieldErrors("form", "year", "genre"));
    }

    @Test
    void testCustomConstraintNoDigits() throws Exception {
        mockMvc.perform(post("/books")
                        .param("title", "Effective Java")
                        .param("author", "Bloch 2018")
                        .param("year", "2018")
                        .param("genre", "SCIENCE"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasFieldErrorCode("form", "author", "NoDigits"));
    }

    @Test
    void testVariant2ClassLevelConstraintReprintYearBeforeYear() throws Exception {
        // year 2020, reprintYear 2010 -> fails class level constraint
        mockMvc.perform(post("/books")
                        .param("title", "Clean Architecture")
                        .param("author", "Robert Martin")
                        .param("year", "2020")
                        .param("reprintYear", "2010")
                        .param("genre", "NON_FICTION"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("form"));
    }

    @Test
    void testValidSubmissionWithVariant2ReprintYear() throws Exception {
        mockMvc.perform(post("/books")
                        .param("title", "Clean Architecture")
                        .param("author", "Robert Martin")
                        .param("year", "2020")
                        .param("reprintYear", "2022")
                        .param("genre", "NON_FICTION"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attribute("message", containsString("Clean Architecture")));
    }

    @Test
    void testInternationalizationSwitchRussian() throws Exception {
        mockMvc.perform(get("/books/new").param("lang", "ru"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Новая книга")))
                .andExpect(content().string(containsString("Название")));
    }

    @Test
    void testPageExceptionHandlerHtmlNotFound() throws Exception {
        mockMvc.perform(get("/books/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/not-found"))
                .andExpect(model().attribute("message", containsString("Book with id 999 not found")))
                .andExpect(content().string(containsString("Book with id 999 not found")));
    }

    @Test
    void testApiExceptionHandlerJsonProblemDetail() throws Exception {
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.detail").value("Book with id 999 not found"));
    }
}
