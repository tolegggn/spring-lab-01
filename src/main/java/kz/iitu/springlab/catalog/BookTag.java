package kz.iitu.springlab.catalog;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Individual Assignment: Variant 2
 * A @ManyToMany with attributes: A BookTag entity with an added addedAt column instead of a plain join table.
 */
@Entity
@Table(name = "book_tag", uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "tag_id"}))
public class BookTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt = Instant.now();

    protected BookTag() { }

    public BookTag(Book book, Tag tag) {
        this.book = book;
        this.tag = tag;
        this.addedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }
}
