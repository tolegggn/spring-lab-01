package kz.iitu.springlab;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookRepository;
import kz.iitu.springlab.catalog.BookTagRepository;
import kz.iitu.springlab.catalog.Genre;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional
class Lab8PersistenceTest {
    @Autowired BookRepository books;
    @Autowired BookTagRepository bookTags;
    @Autowired EntityManager entityManager;
    @Autowired EntityManagerFactory entityManagerFactory;

    @Test
    void queriesAndVariantTwoPersistInDatabase() {
        assertEquals(5, books.findByGenre(Genre.NON_FICTION).size());
        assertEquals(3, books.findByTitleContainingIgnoreCase("clean").size());
        assertEquals(5, books.findByYearBetweenOrderByYearDesc(2000, 2020).size());
        assertEquals(3, books.findTop3ByGenreOrderByYearDesc(Genre.NON_FICTION).size());
        assertEquals(2, books.countByAuthorName("Joshua Bloch"));
        assertEquals(3, books.search(Genre.NON_FICTION, 2010).size());
        Book effectiveJava = books.findByTitleContainingIgnoreCase("Effective Java").getFirst();
        var tags = bookTags.findByBookId(effectiveJava.getId());
        assertEquals(2, tags.size());
        assertTrue(tags.stream().allMatch(tag -> tag.getAddedAt() != null));
        var tag = tags.getFirst().getTag();
        effectiveJava.addTag(tag);
        assertEquals(2, effectiveJava.getBookTags().size());
        assertTrue(tag.getBookTags().stream().anyMatch(link -> link.getBook().getId().equals(effectiveJava.getId())));
        assertEquals(3, books.archiveOlderThan(1950));
        assertTrue(books.findByYearBetweenOrderByYearDesc(1900, 1949).stream().allMatch(Book::isArchived));
    }

    @Test
    void measureNPlusOneAndPagingStatements() {
        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        entityManager.clear();
        stats.clear();
        books.findAll().forEach(book -> book.getAuthor().getName());
        long lazyCount = stats.getPrepareStatementCount();

        entityManager.clear();
        stats.clear();
        books.findAllWithAuthor().forEach(book -> book.getAuthor().getName());
        long joinCount = stats.getPrepareStatementCount();

        entityManager.clear();
        stats.clear();
        var page = books.findAll(PageRequest.of(0, 3));
        assertEquals(8, page.getTotalElements());
        long pageCount = stats.getPrepareStatementCount();

        entityManager.clear();
        stats.clear();
        var slice = books.findSliceBy(PageRequest.of(0, 3));
        assertTrue(slice.hasNext());
        long sliceCount = stats.getPrepareStatementCount();

        System.out.printf("LAB08 SQL COUNTS: N+1=%d, fetch join=%d, Page=%d, Slice=%d%n",
                lazyCount, joinCount, pageCount, sliceCount);
        assertEquals(4, lazyCount);
        assertEquals(1, joinCount);
        assertEquals(2, pageCount);
        assertEquals(1, sliceCount);
    }
}
