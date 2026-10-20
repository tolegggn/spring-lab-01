package kz.iitu.springlab.catalog;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author, String genre, String sort) {
        var stream = repository.findAll().stream();

        if (author != null && !author.isBlank()) {
            stream = stream.filter(b -> b.author().toLowerCase().contains(author.trim().toLowerCase()));
        }

        if (genre != null && !genre.isBlank()) {
            boolean validGenre = Arrays.stream(Genre.values())
                    .anyMatch(g -> g.name().equalsIgnoreCase(genre.trim()));
            if (!validGenre) {
                throw new InvalidSortException("genre: " + genre);
            }
            stream = stream.filter(b -> b.genre().name().equalsIgnoreCase(genre.trim()));
        }

        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            String dir = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";

            Comparator<Book> comparator;
            switch (field.toLowerCase()) {
                case "id" -> comparator = Comparator.comparing(Book::id);
                case "title" -> comparator = Comparator.comparing(Book::title, String.CASE_INSENSITIVE_ORDER);
                case "author" -> comparator = Comparator.comparing(Book::author, String.CASE_INSENSITIVE_ORDER);
                case "year" -> comparator = Comparator.comparingInt(Book::year);
                case "genre" -> comparator = Comparator.comparing(b -> b.genre().name());
                case "createdat" -> comparator = Comparator.comparing(Book::createdAt);
                default -> throw new InvalidSortException(field);
            }

            if ("desc".equals(dir)) {
                comparator = comparator.reversed();
            }

            stream = stream.sorted(comparator);
        }

        return stream.toList();
    }

    public List<Book> findAll(String author) {
        return findAll(author, null, null);
    }

    public List<Book> findAll() {
        return findAll(null, null, null);
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book getById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book) {
        boolean duplicate = repository.findAll().stream()
                .anyMatch(b -> b.title().trim().equalsIgnoreCase(book.title().trim())
                            && b.author().trim().equalsIgnoreCase(book.author().trim()));
        if (duplicate) {
            throw new DuplicateTitleException(book.title());
        }
        return repository.save(book);
    }

    public Book update(long id, Book book) {
        Optional<Book> existing = repository.findById(id);
        if (existing.isEmpty()) {
            throw new BookNotFoundException(id);
        }

        boolean duplicate = repository.findAll().stream()
                .anyMatch(b -> !b.id().equals(id)
                            && b.title().trim().equalsIgnoreCase(book.title().trim())
                            && b.author().trim().equalsIgnoreCase(book.author().trim()));
        if (duplicate) {
            throw new DuplicateTitleException(book.title());
        }

        Book updated = new Book(id, book.title(), book.author(), book.year(), book.genre(),
                existing.get().createdAt());
        return repository.save(updated);
    }

    public Optional<Book> replace(long id, Book book) {
        if (repository.findById(id).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(update(id, book));
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }

    public List<Book> findByYear(int year) {
        return repository.findAll().stream()
                .filter(b -> b.year() == year)
                .toList();
    }
}
