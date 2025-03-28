package infrastructure.repositories;

import domain.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {
    long countByReservationDate(LocalDateTime date);
    long countByReservationDateAndDocumentNumber(LocalDateTime date, String documentNumber);
    Reservation findById(Long id);
    List<Reservation> findByReservationDateBetween(LocalDateTime start, LocalDateTime end);
    long countByReservationDateBetween(LocalDateTime start, LocalDateTime end);
}
