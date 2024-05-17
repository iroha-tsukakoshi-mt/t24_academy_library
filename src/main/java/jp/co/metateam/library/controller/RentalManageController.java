package jp.co.metateam.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.FieldError;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.values.RentalStatus;
import lombok.extern.log4j.Log4j2;

import jp.co.metateam.library.model.RentalManage;
import java.util.List;

import java.time.LocalDate;
import java.util.Date;
import java.time.format.DateTimeFormatter;

import java.util.Optional;



/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }


    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List <RentalManage> rentalManageList = this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", rentalManageList);
        // 貸出一覧画面に遷移
        return "rental/index";
    }

    @GetMapping("/rental/add")
    public String add(Model model) {
        List <Account> accounts = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findStockAvailableAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }

    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            Optional<String> statusAbleError = rentalManageDto.statusCheckAdd(rentalManageDto.getStatus(), rentalManageDto.getExpectedRentalOn());
            if(statusAbleError.isPresent()) {
                result.addError(new FieldError("rentalManageDto", "status", statusAbleError.get())); //resultにエラーを追加する
                throw new Exception("Validation error.");
            }

            Optional<String> rentalAbleError = rentalManageService.whetherRentalAdd(rentalManageDto.getStockId(), new java.sql.Date(rentalManageDto.getExpectedRentalOn().getTime()), new java.sql.Date(rentalManageDto.getExpectedReturnOn().getTime()));
            if(rentalAbleError.isPresent()) {
                result.addError(new FieldError("rentalManageDto", "status", rentalAbleError.get()));
                throw new Exception("DateRepeating error.");
            }

            this.rentalManageService.save(rentalManageDto); //登録処理

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/add";
        }
    }

    @GetMapping("/rental/{id}/edit") //{id}で貸出管理番号（主キー）の数字を持ってきている（初期表示をするため）
    public String edit(@PathVariable("id") String id, Model model) { //()内は外にある使いたいものを定義
        List<Account> accounts = this.accountService.findAll(); //htmlの＄の中身（水色の部分）
        List<Stock> stockList = this.stockService.findStockAvailableAll(); //プルダウンとして表示するため全件取得

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList); //("ファイルの名称", ファイルの箱（任意）)
        model.addAttribute("rentalStatus", RentalStatus.values()); //画面表示させるためにmodelに入れる

        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id)); //RentalManageServiceのなかの対象となる貸出管理番号（主キー）を取得し、rentalManageに入れる

        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto(); //新しいRentalManageDtoをつくってrentalManage（水色）に入れる

            rentalManageDto.setId(rentalManage.getId()); //rentalManageDtoにIdをセット（setId）→（）の中身はidの身元、rentalManageからゲットする（getId）
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId()); //rentalManageDtoにEmployeeIdをセット（setEmployeeId）→（）の中身はEmployeeIdの身元、rentalManageにあるAccountからゲットする（getAccount・getEmployeeId）
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setStatus(rentalManage.getStatus());

            model.addAttribute("rentalManageDto", rentalManageDto); //rentalManageDtoの内容をmodelに入れ、表示させる
        }

        return "rental/edit"; //編集画面の初期表示
    }

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id)); //変更前の貸出情報を取得
            Optional<String> statusAbleError = rentalManageDto.statusCheck(rentalManage.getStatus(), rentalManageDto.getStatus(), rentalManageDto.getExpectedRentalOn(), rentalManageDto.getExpectedReturnOn()); //変更前と変更後の貸出ステータスと変更後の貸出予定日と返却予定日を取得、aは任意の変数名
            if(statusAbleError.isPresent()) {
                result.addError(new FieldError("rentalManageDto", "status", statusAbleError.get())); //resultにエラーを追加する
                throw new Exception("Validation error.");
            }

            Optional<String> rentalAbleError = rentalManageService.whetherRental(rentalManageDto.getStockId(), rentalManageDto.getId(), new java.sql.Date(rentalManageDto.getExpectedRentalOn().getTime()), new java.sql.Date(rentalManageDto.getExpectedReturnOn().getTime()));
            if(rentalAbleError.isPresent()) {
                result.addError(new FieldError("rentalManageDto", "status", rentalAbleError.get()));
                throw new Exception("DateRepeating error.");
            }

            rentalManageService.update(Long.valueOf(id), rentalManageDto); //更新処理、rentalManageServiceにあるupdateメソッドを（）内の引数で呼ぶ

            return "redirect:/rental/index";
        
        } catch (Exception e) {  // throwを検知した際にcatchにとぶ
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto); //ra.addFlashAttribute()：エラーメッセージをフラッシュ属性として設定し、リダイレクト先のページで表示させる
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return String.format("redirect:/rental/%s/edit", id);
        }
    }
}


   