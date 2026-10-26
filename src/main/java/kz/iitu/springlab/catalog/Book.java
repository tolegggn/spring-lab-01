package kz.iitu.springlab.catalog;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genre genre;

    private int year;

    private boolean archived;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Version
    private long version;

    // Owning side of Author association
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    // Individual variant 2: intermediate entity BookTag with added_at
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookTag> bookTags = new HashSet<>();

    protected Book() { } // JPA requires this no-arg constructor

    public Book(String title, Genre genre, int year) {
        this(title, null, genre, year);
    }

    public Book(String title, Author author, Genre genre, int year) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.archived = false;
        this.createdAt = Instant.now();
    }

    public Book(Long id, String title, Author author, int year, Genre genre, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.genre = genre;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.archived = false;
    }

    public void addTag(Tag tag) {
        BookTag bookTag = new BookTag(this, tag);
        this.bookTags.add(bookTag);
    }

    // Standard getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getVersion() {
        return version;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Set<BookTag> getBookTags() {
        return bookTags;
    }

    public void setBookTags(Set<BookTag> bookTags) {
        this.bookTags = bookTags;
    }

    // Record-style accessors for compatibility
    public Long id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String author() {
        return author != null ? author.getName() : null;
    }

    public int year() {
        return year;
    }

    public Genre genre() {
        return genre;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public boolean archived() {
        return archived;
    }
}
