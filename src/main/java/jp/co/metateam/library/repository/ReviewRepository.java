package jp.co.metateam.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.metateam.library.model.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	@Query(value = "SELECT * FROM review WHERE book_id = :id", nativeQuery = true)
        List<Review> reviewCheckList(@Param("id") Long bookId);
        
}
