package com.example.bookingservice.repo;

import com.example.bookingservice.entity.HotelInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HotelInventoryRepository extends JpaRepository<HotelInventory,Long> {

    Optional<HotelInventory> findByHotelId(String hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM HotelInventory h WHERE h.hotelId = :hotelId")
    Optional<HotelInventory> findByHotelIdForUpdate(@Param("hotelId") String hotelId);
}
