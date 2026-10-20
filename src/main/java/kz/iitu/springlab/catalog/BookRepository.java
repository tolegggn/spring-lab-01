package kz.iitu.springlab.catalog;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookRepository {

    private final Map<Long, Book> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public BookRepository() {
        save(new Book(null, "Effective Java", "Joshua Bloch", 2018, Genre.NON_FICTION, Instant.parse("2026-01-10T10:00:00Z")));
        save(new Book(null, "Clean Code", "Robert Martin", 2008, Genre.NON_FICTION, Instant.parse("2026-01-11T11:00:00Z")));
        save(new Book(null, "Spring in Action", "Craig Walls", 2022, Genre.NON_FICTION, Instant.parse("2026-01-12T12:00:00Z")));
        save(new Book(null, "1984", "George Orwell", 1949, Genre.NOVEL, Instant.parse("2026-01-13T13:00:00Z")));
        save(new Book(null, "To Kill a Mockingbird", "Harper Lee", 1960, Genre.NOVEL, Instant.parse("2026-01-14T14:00:00Z")));
    }

    public List<Book> findAll() {
        return List.copyOf(storage.values());
    }

    public Optional<Book> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Book save(Book book) {
        long id = book.id() != null ? book.id() : sequence.incrementAndGet();
        Instant createdAt = book.createdAt() != null ? book.createdAt() : Instant.now();
        Genre genre = book.genre() != null ? book.genre() : Genre.FICTION;
        Book stored = new Book(id, book.title(), book.author(), book.year(), genre, createdAt);
        storage.put(id, stored);
        return stored;
    }

    public boolean deleteById(long id) {
        return storage.remove(id) != null;
    }
}
