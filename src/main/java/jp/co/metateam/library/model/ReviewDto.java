package jp.co.metateam.library.model;

import java.security.Timestamp;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * レビューDTO
 */
@Getter
@Setter
public class ReviewDto {
    
    private Long id;

    private Long score;

    private String body;
    
    private Timestamp createdAt;

    private BookMst bookMst;

}
