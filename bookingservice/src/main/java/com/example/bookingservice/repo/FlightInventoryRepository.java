package com.example.bookingservice.repo;

import com.example.bookingservice.entity.FlightInventory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightInventoryRepository extends JpaRepository<FlightInventory,Long> {

    //FlightInventory findByFlightId(String flightId);
    Optional<FlightInventory> findByFlightId(String flightId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM FlightInventory f
            WHERE f.flightId = :flightId
            """)
    Optional<FlightInventory> findByFlightIdForUpdate(
            @Param("flightId") String flightId
    );


    List<FlightInventory> findByDepartureBeforeAndActiveTrue(LocalDateTime cutoff);

    List<FlightInventory> findByFromAirportAndToAirportAndActiveTrue(String fromAirport, String toAirport);


    @Modifying
    @Query("UPDATE FlightInventory f SET f.seatsAvailable = f.seatsAvailable - :qty " +
            "WHERE f.flightId = :flightId AND f.seatsAvailable >= :qty")
    int decrementSeatsIfAvailable(@Param("flightId") String flightId, @Param("qty") int qty);

    // New: fetch flight row with pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(
                    name = "jakarta.persistence.lock.timeout",
                    value = "3000"   // wait max 3 seconds, then fail
            )
    })
    @Query("SELECT f FROM FlightInventory f WHERE f.flightId = :flightId")
    Optional<FlightInventory> findWithLockByFlightId(@Param("flightId") String flightId);

}
