package com.example.bookingservice.repo;

import com.example.bookingservice.entity.BookingAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingAuditRepository extends JpaRepository<BookingAudit,Long> {
}
