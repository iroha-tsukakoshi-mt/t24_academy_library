package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;


import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import jp.co.metateam.library.values.RentalStatus;

import java.time.LocalDate;
import java.time.ZoneId;


/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;


    //貸出編集画面におけるステータス妥当性チェックとステータス・日付妥当性チェック
    public Optional<String> statusCheck(Integer preStatus, Integer newStatus, Date expectedRentalOn, Date expectedReturnOn){
        Date now = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        if (preStatus == RentalStatus.RENT_WAIT.getValue() && newStatus == RentalStatus.RETURNED.getValue()
            || preStatus == RentalStatus.RENTAlING.getValue() && newStatus == RentalStatus.RENT_WAIT.getValue() 
            || preStatus == RentalStatus.RENTAlING.getValue() && newStatus == RentalStatus.CANCELED.getValue()
            || preStatus == RentalStatus.RETURNED.getValue() && newStatus == RentalStatus.RENT_WAIT.getValue()
            || preStatus == RentalStatus.RETURNED.getValue() && newStatus == RentalStatus.RENTAlING.getValue()
            || preStatus == RentalStatus.RETURNED.getValue() && newStatus == RentalStatus.CANCELED.getValue()
            || preStatus == RentalStatus.CANCELED.getValue() && newStatus == RentalStatus.RENT_WAIT.getValue()
            || preStatus == RentalStatus.CANCELED.getValue() && newStatus == RentalStatus.RENTAlING.getValue()
            || preStatus == RentalStatus.CANCELED.getValue() && newStatus == RentalStatus.RETURNED.getValue()) {
            return Optional.of("そのステータスは無効です");
        } else if (preStatus == RentalStatus.RENT_WAIT.getValue() && newStatus == RentalStatus.RENTAlING.getValue() && !(expectedRentalOn.compareTo(now) == 0)) {
            return Optional.of("貸出予定日を今日の日付に設定してください");
        } else if (preStatus == RentalStatus.RENTAlING.getValue() && newStatus == RentalStatus.RETURNED.getValue() && !(expectedReturnOn.compareTo(now) == 0)) {
            return Optional.of("返却予定日を今日の日付に設定してください");
        }
        return Optional.empty(); //空を返してる   
    }
    

    //貸出登録画面におけるステータス・日付妥当性チェック
    public Optional<String> statusCheckAdd(Integer status, Date expectedRentalOn){
        Date now = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        if (status == RentalStatus.RENTAlING.getValue() && !(expectedRentalOn.compareTo(now) == 0)) {
            return Optional.of("貸出予定日を今日の日付に設定してください");
        } else if (status == RentalStatus.RETURNED.getValue()
                   || status == RentalStatus.CANCELED.getValue()){
            return Optional.of("そのステータスは選択できません");
        }
        return Optional.empty();   
    }
}

