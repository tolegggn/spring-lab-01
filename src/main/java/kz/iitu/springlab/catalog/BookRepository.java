package kz.iitu.springlab.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Task 3.1: Five derived queries
    List<Book> findByGenre(Genre genre);

    List<Book> findByTitleContainingIgnoreCase(String fragment);

    List<Book> findByYearBetweenOrderByYearDesc(int from, int to);

    List<Book> findTop3ByGenreOrderByYearDesc(Genre genre);

    long countByAuthorName(String name);

    boolean existsByTitleIgnoreCaseAndAuthorNameIgnoreCase(String title, String authorName);

    // Task 3.2: JPQL query with named parameters
    @Query("""
           select b from Book b
           where b.genre = :genre and b.year >= :year
           order by b.title""")
    List<Book> search(@Param("genre") Genre genre, @Param("year") int year);

    // Task 3.2: Bulk update
    @Modifying(clearAutomatically = true)
    @Query("update Book b set b.archived = true where b.year < :year")
    int archiveOlderThan(@Param("year") int year);

    // Task 3.4: N+1 cure (fetch join)
    @Query("select b from Book b left join fetch b.author")
    List<Book> findAllWithAuthor();

    // Task 3.4: N+1 cure (EntityGraph)
    @EntityGraph(attributePaths = "author")
    List<Book> findWithGraphByGenre(Genre genre);

    // Task 4.1: Pagination and sorting with EntityGraph to avoid LazyInitializationException when open-in-view=false
    @EntityGraph(attributePaths = "author")
    Page<Book> findByGenre(Genre genre, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Book> findByAuthorNameContainingIgnoreCase(String author, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Book> findByGenreAndAuthorNameContainingIgnoreCase(Genre genre, String author, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "author")
    Page<Book> findAll(Pageable pageable);

    // Task 4.3: Slice version
    @EntityGraph(attributePaths = "author")
    Slice<Book> findSliceByGenre(Genre genre, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Slice<Book> findSliceBy(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<Book> findById(Long id);
}
