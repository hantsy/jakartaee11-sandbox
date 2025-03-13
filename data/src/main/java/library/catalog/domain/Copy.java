package library.catalog.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Copy {
    @EmbeddedId
    private CopyId id;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "book_id"))
    private BookId bookId;
    @Embedded
    private BarCode barCode;
    private boolean available;

    Copy() {
    }

    public CopyId id() {
        return this.id;
    }

    public Copy(BookId bookId, BarCode barCode) {
        Objects.requireNonNull(bookId, "bookId must not be null");
        Objects.requireNonNull(barCode, "barCode must not be null");
        this.id = new CopyId();
        this.bookId = bookId;
        this.barCode = barCode;
        this.available = true;
    }

    public void makeUnavailable() {
        this.available = false;
    }

    public void makeAvailable() {
        this.available = true;
    }

    public boolean isAvailable() {
        return this.available;
    }
}