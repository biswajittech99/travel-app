package com.example.invoiceservice.repository;

import com.example.invoiceservice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice,String> {
    Optional<Invoice> findByBookingUid(String bookingUid);

    List<Invoice> findByUserIdOrderByCreatedAtDesc(String userId);
}
