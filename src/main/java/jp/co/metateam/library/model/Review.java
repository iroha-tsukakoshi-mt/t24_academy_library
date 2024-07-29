package jp.co.metateam.library.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * レビュー
 */
@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "score", nullable = false)
    private Long score;

    @Column(name = "body", nullable = false)
    private String body;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id", nullable = false)
    private BookMst bookMst;

    /** Getters */

    public Long getId() {
        return this.id;
    }

    public Timestamp getCreatedAt() {
        return this.createdAt;
    }

    public Long getScore() {
        return this.score;
    }

    public String getBody() {
        return this.body;
    }

    public BookMst getBookMst() {
        return this.bookMst;
    }

    /** Setters */

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setBookMst(BookMst bookMst) {
        this.bookMst = bookMst;
    }

    
}
