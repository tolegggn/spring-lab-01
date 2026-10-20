package kz.iitu.springlab.catalog;

public class InvalidSortException extends RuntimeException {
    private final String field;

    public InvalidSortException(String field) {
        super("Unknown or invalid sort field: '" + field + "'. Allowed sort fields are: id, title, author, year, genre, createdAt");
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
