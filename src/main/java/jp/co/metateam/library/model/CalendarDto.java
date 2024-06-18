package jp.co.metateam.library.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public class CalendarDto {
    
    private String title;

    private int stockCount;

    public List<RentalCountDto> rentalCountList; {
        this.rentalCountList = new ArrayList<>();
    }

}
