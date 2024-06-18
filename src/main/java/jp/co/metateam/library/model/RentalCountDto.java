package jp.co.metateam.library.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
public class RentalCountDto {
    //貸出可能数
    private Object rentalCount;

    //在庫管理番号
    public List<String> stockIdList; {
        this.stockIdList = new ArrayList<>();
    }

    //貸出予定日
    private LocalDate expectedRentalOn;

}
