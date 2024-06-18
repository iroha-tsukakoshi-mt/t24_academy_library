package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.model.RentalCountDto;


@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository, RentalManageRepository rentalManageRepository){
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }
    
    @Transactional
    public List <Stock> findStockAvailableAll() {
        List <Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional 
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional 
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    //曜日の取得
    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }
    
    
    //カレンダーリストの中身（書籍名、在庫数、貸出可能数の取得）
    public List<CalendarDto> generateValues(Integer year, Integer month, Integer daysInMonth) {
        List<CalendarDto> calendarDtoList = new ArrayList<CalendarDto>();  //カレンダーリスト

        List<BookMst> bookList = this.bookMstRepository.findAll();  //書籍リスト

        //書籍ごとのループ（一行ごと）
        for (int i = 0; i < bookList.size(); i++) {
            //bookListの行を宣言
            BookMst book = bookList.get(i);
            //書籍id＋ステータスが「利用可」の在庫情報を全件取得しリスト化
            List<Stock> stockList = this.stockRepository.findByBookMstIdAndStatus(book.getId(), Constants.STOCK_AVAILABLE);

            if(stockList.size() > 0){
                //calendarDtoに追加
                CalendarDto calendarDto = new CalendarDto();  //インスタンス化
                calendarDto.setTitle(book.getTitle());
                calendarDto.setStockCount(stockList.size());

                //現在日付を取得
                LocalDate currentDate = LocalDate.now();

                for (int n = 1; n <= daysInMonth; n++) {
                    LocalDate dateCheck = LocalDate.of(year, month, n);
                    RentalCountDto rentalCountDto = new RentalCountDto();

                    //rentalableCountメソッド呼び出し
                    rentalCountDto = rentalableCount(stockList, rentalCountDto, dateCheck);
                    
                    //カレンダーの日付が過去日・貸出可能数が0の場合「×を表示」
                    Object obj = 0;
                    if (currentDate.isAfter(dateCheck) || rentalCountDto.getRentalCount() == obj ){
                        rentalCountDto.setRentalCount("×");
                    //カレンダーの日付が今日以降
                    }

                    calendarDto.rentalCountList.add(rentalCountDto);
                    

                }
            
                calendarDtoList.add(calendarDto);

            }
            
        }

        return calendarDtoList;
    }
    


    //貸出可能数取得メソッド（書籍idが一致する在庫ごと）
    //rentalCountList（貸出可能数×30日分、貸出可能な在庫管理番号×30日分、貸出予定日（1日～30日））を返す
    private RentalCountDto rentalableCount(List<Stock>stockList, RentalCountDto rentalCountDto, LocalDate dateCheck){
         
        boolean  flag =  false;

        //dateCheck（LocalDate型）をDate型に変換
        Date date = Date.from(dateCheck.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Integer count = 0;


        for (int i = 0; i < stockList.size(); i++) {  
            //ストックリスト（全件）の行を宣言          
            Stock stock = stockList.get(i);
            
            //在庫idが一致＋ステータスが「貸出待ち」「貸出中」の貸出情報を全件取得しリスト化            
            List<RentalManage> rentalManageList = rentalManageRepository.rentalCountCheck(stock.getId());

            if(rentalManageList.size() > 0){

                for (int k = 0; k < rentalManageList.size(); k++) {
                    //rentalManageListの行を宣言
                    RentalManage rentalManage = rentalManageList.get(k);

                    Date expectedRentalOn = rentalManage.getExpectedRentalOn();
                    Date expectedReturnOn = rentalManage.getExpectedReturnOn();

                    int comparison1 = date.compareTo(expectedRentalOn);
                    int comparison2 = date.compareTo(expectedReturnOn);

                    if(comparison1 < 0 || comparison2 > 0){
            
                        flag =  true;

                    } else {
                        rentalCountDto.setRentalCount(0);
                        flag = false;
                        break; //for文を強制終了
                    }
                }

                if(flag){
                    count = count + 1;
                
                    //rentalCountに
                    rentalCountDto.setRentalCount(count);
            
                    //stockIdListに在庫管理番号を追加
                    rentalCountDto.stockIdList.add(stock.getId());

                    rentalCountDto.setExpectedRentalOn(dateCheck);

                }

            } else {
                count = count + 1;

                rentalCountDto.setRentalCount(count);

                rentalCountDto.stockIdList.add(stock.getId());

                rentalCountDto.setExpectedRentalOn(dateCheck);
            }

        }

        return rentalCountDto;

    }


    @Transactional
    public List<Stock> getList(List<String> idList){
        List <Stock> stockList = new ArrayList<>();
 
        for(int i = 0; i < idList.size(); i++){
            String stockId = idList.get(i);
            Optional<Stock> optionalStock = stockRepository.findById(stockId);
            Stock stock = optionalStock.orElse(new Stock());
 
            stockList.add(stock);  
 
 
        }
        return stockList;
    }



}




