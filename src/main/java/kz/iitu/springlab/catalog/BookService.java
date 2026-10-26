package kz.iitu.springlab.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository repository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository repository, AuthorRepository authorRepository) {
        this.repository = repository;
        this.authorRepository = authorRepository;
    }

    // Task 4.1: Paged and sorted search
    public Page<Book> find(Genre genre, Pageable pageable) {
        if (genre != null) {
            return repository.findByGenre(genre, pageable);
        }
        return repository.findAll(pageable);
    }

    // Task 4.3: Slice version for comparison
    public Slice<Book> findSlice(Genre genre, Pageable pageable) {
        if (genre != null) {
            return repository.findSliceByGenre(genre, pageable);
        }
        return repository.findAll(pageable);
    }

    // Task 3.2: JPQL search
    public List<Book> search(Genre genre, int year) {
        return repository.search(genre, year);
    }

    // Task 3.2: Bulk update requires @Transactional (write mode)
    @Transactional
    public int archiveOlderThan(int year) {
        return repository.archiveOlderThan(year);
    }

    // Task 3.4: Fetch join to prevent N+1
    public List<Book> findAllWithAuthor() {
        return repository.findAllWithAuthor();
    }

    public List<Book> findAll() {
        return repository.findAll();
    }

    public List<Book> findAll(String author, String genre, String sort) {
        var stream = repository.findAll().stream();

        if (author != null && !author.isBlank()) {
            stream = stream.filter(b -> b.author() != null &&
                    b.author().toLowerCase().contains(author.trim().toLowerCase()));
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
                case "author" -> comparator = Comparator.comparing(
                        b -> b.author() != null ? b.author() : "", String.CASE_INSENSITIVE_ORDER);
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

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book getById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public Book create(Book book) {
        return create(book, book.author());
    }

    @Transactional
    public Book create(Book book, String authorName) {
        if (authorName != null && !authorName.isBlank()) {
            Author author = authorRepository.findByName(authorName.trim())
                    .orElseGet(() -> authorRepository.save(new Author(authorName.trim(), "Unknown")));
            author.addBook(book);
        }

        boolean duplicate = repository.findAll().stream()
                .anyMatch(b -> b.title().trim().equalsIgnoreCase(book.title().trim())
                        && b.author() != null
                        && b.author().trim().equalsIgnoreCase(book.author() != null ? book.author().trim() : ""));
        if (duplicate) {
            throw new DuplicateTitleException(book.title());
        }

        return repository.save(book);
    }

    @Transactional
    public Book update(long id, Book updatedData) {
        return update(id, updatedData, updatedData.author());
    }

    @Transactional
    public Book update(long id, Book updatedData, String authorName) {
        Book existing = repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        if (authorName != null && !authorName.isBlank()) {
            Author author = authorRepository.findByName(authorName.trim())
                    .orElseGet(() -> authorRepository.save(new Author(authorName.trim(), "Unknown")));
            author.addBook(existing);
        }

        existing.setTitle(updatedData.getTitle());
        existing.setGenre(updatedData.getGenre());
        existing.setYear(updatedData.getYear());
        existing.setArchived(updatedData.isArchived());

        return repository.save(existing);
    }

    @Transactional
    public boolean delete(long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
