package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);


    @Query("SELECT COUNT (*) FROM Stock WHERE id = ?1 AND status = 0")
    Integer count(String id);

    //貸出編集画面
    @Query("SELECT COUNT (*) FROM RentalManage WHERE stock.id = ?1 AND id != ?2 AND status IN (0,1)")
    Integer test(String stockId, Long id);

    @Query("SELECT COUNT (*) FROM RentalManage WHERE stock.id = ?1 AND id != ?2 AND status IN (0,1) AND (expectedReturnOn < ?3 OR ?4 < expectedRentalOn)")
    Integer datecount(String stockId, Long id, Date expected_return_on, Date expected_rental_on);

    //貸出登録画面
    @Query("SELECT COUNT (*) FROM RentalManage WHERE stock.id = ?1 AND status IN (0,1)")
    Integer testAdd(String stockId);

    @Query("SELECT COUNT (*) FROM RentalManage WHERE stock.id = ?1 AND status IN (0,1) AND (expectedReturnOn < ?2 OR ?3 < expectedRentalOn)")
    Integer datecountAdd(String stockId, Date expected_return_on, Date expected_rental_on);

}