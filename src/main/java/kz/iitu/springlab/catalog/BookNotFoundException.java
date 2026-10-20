package kz.iitu.springlab.catalog;

public class BookNotFoundException extends RuntimeException {
    private final Long id;

    public BookNotFoundException(Long id) {
        super("Book with id " + id + " not found");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
