package kz.iitu.springlab.service;

public class DuplicateTitleException extends kz.iitu.springlab.catalog.DuplicateTitleException {
    public DuplicateTitleException(String title) {
        super(title);
    }
}
