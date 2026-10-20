package kz.iitu.springlab.service;

public class BookNotFoundException extends kz.iitu.springlab.catalog.BookNotFoundException {
    public BookNotFoundException(Long id) {
        super(id);
    }
}
