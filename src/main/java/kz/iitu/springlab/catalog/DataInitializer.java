package kz.iitu.springlab.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AuthorRepository authorRepository;
    private final TagRepository tagRepository;
    private final BookRepository bookRepository;
    private final BookTagRepository bookTagRepository;
    private final BookService bookService;

    public DataInitializer(AuthorRepository authorRepository,
                           TagRepository tagRepository,
                           BookRepository bookRepository,
                           BookTagRepository bookTagRepository,
                           BookService bookService) {
        this.authorRepository = authorRepository;
        this.tagRepository = tagRepository;
        this.bookRepository = bookRepository;
        this.bookTagRepository = bookTagRepository;
        this.bookService = bookService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========== Task 2.2: Seeding Data ==========");

        // 3 Authors
        Author bloch = new Author("Joshua Bloch", "USA");
        Author martin = new Author("Robert Martin", "USA");
        Author orwell = new Author("George Orwell", "UK");

        authorRepository.saveAll(List.of(bloch, martin, orwell));

        // 4 Tags
        Tag tagJava = new Tag("Java");
        Tag tagArch = new Tag("Architecture");
        Tag tagClassic = new Tag("Classic");
        Tag tagDystopia = new Tag("Dystopia");

        tagRepository.saveAll(List.of(tagJava, tagArch, tagClassic, tagDystopia));

        // 8 Books
        // Author 1: Bloch (2 books)
        Book b1 = new Book("Effective Java", Genre.NON_FICTION, 2018);
        bloch.addBook(b1);

        Book b2 = new Book("Java Puzzlers", Genre.NON_FICTION, 2005);
        bloch.addBook(b2);

        // Author 2: Martin (owns 3 books - requirement!)
        Book b3 = new Book("Clean Code", Genre.NON_FICTION, 2008);
        martin.addBook(b3);

        Book b4 = new Book("Clean Architecture", Genre.NON_FICTION, 2017);
        martin.addBook(b4);

        Book b5 = new Book("Clean Agile", Genre.NON_FICTION, 2019);
        martin.addBook(b5);

        // Author 3: Orwell (3 books)
        Book b6 = new Book("1984", Genre.NOVEL, 1949);
        orwell.addBook(b6);

        Book b7 = new Book("Animal Farm", Genre.NOVEL, 1945);
        orwell.addBook(b7);

        Book b8 = new Book("Homage to Catalonia", Genre.HISTORY, 1938);
        orwell.addBook(b8);

        bookRepository.saveAll(List.of(b1, b2, b3, b4, b5, b6, b7, b8));

        // Variant 2: BookTag with addedAt attribute
        // At least one book carrying two tags (b1 carries tagJava and tagArch; b6 carries tagClassic and tagDystopia)
        b1.addTag(tagJava);
        b1.addTag(tagArch);
        b2.addTag(tagJava);
        b3.addTag(tagArch);
        b4.addTag(tagArch);
        b6.addTag(tagClassic);
        b6.addTag(tagDystopia);

        // Books and tags are managed in this transaction; cascading persists the links.

        log.info("Seeding completed: {} authors, {} tags, {} books",
                authorRepository.count(), tagRepository.count(), bookRepository.count());

        // Variant 2 Verification: Show BookTag rows with added_at column
        log.info("========== Variant 2: BookTag entity with addedAt column ==========");
        List<BookTag> b1Tags = bookTagRepository.findByBookId(b1.getId());
        for (BookTag bt : b1Tags) {
            log.info("BookTag: Book='{}', Tag='{}', addedAt='{}'",
                    bt.getBook().getTitle(), bt.getTag().getName(), bt.getAddedAt());
        }

        log.info("========== Task 3: Queries Execution ==========");
        // 3.1 Derived Queries
        List<Book> byGenre = bookRepository.findByGenre(Genre.NON_FICTION);
        log.info("1. findByGenre(NON_FICTION) -> {} rows", byGenre.size());

        List<Book> byTitle = bookRepository.findByTitleContainingIgnoreCase("clean");
        log.info("2. findByTitleContainingIgnoreCase('clean') -> {} rows", byTitle.size());

        List<Book> byYear = bookRepository.findByYearBetweenOrderByYearDesc(2000, 2020);
        log.info("3. findByYearBetweenOrderByYearDesc(2000, 2020) -> {} rows", byYear.size());

        List<Book> top3 = bookRepository.findTop3ByGenreOrderByYearDesc(Genre.NON_FICTION);
        log.info("4. findTop3ByGenreOrderByYearDesc(NON_FICTION) -> {} rows", top3.size());

        long countBloch = bookRepository.countByAuthorName("Joshua Bloch");
        log.info("5. countByAuthorName('Joshua Bloch') -> {}", countBloch);

        // 3.2 JPQL Search
        List<Book> jpqlSearch = bookRepository.search(Genre.NON_FICTION, 2010);
        log.info("6. search(NON_FICTION, 2010) [JPQL] -> {} rows", jpqlSearch.size());

        // 3.2 Bulk update
        int archivedCount = bookService.archiveOlderThan(1950);
        log.info("7. archiveOlderThan(1950) [Bulk Update] -> updated {} rows", archivedCount);

        // 3.4 N+1 Exercise: fetch join
        log.info("========== Task 3.4: N+1 Solution (join fetch) ==========");
        List<Book> booksWithAuthor = bookRepository.findAllWithAuthor();
        for (Book b : booksWithAuthor) {
            log.info("Book: '{}' by Author: '{}'", b.getTitle(), b.getAuthor().getName());
        }
    }
}
