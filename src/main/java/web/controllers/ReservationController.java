package web.controllers;

import application.mapper.ReservationMapper;
import application.services.ReservationService;
import domain.models.Reservation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.dto.ReservationDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurant/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;


    @PostMapping
    public ResponseEntity<ReservationDTO> createReservation(@RequestBody ReservationDTO reservationDTO) {
        Reservation reservation = reservationMapper.toEntity(reservationDTO);
        Reservation savedReservation = reservationService.createReservation(reservation);
        return ResponseEntity.ok(reservationMapper.toDto(savedReservation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(
            @PathVariable Long id,
            @RequestBody @Valid ReservationDTO reservationDTO) {

        Reservation reservation = Reservation.builder()
                .name(reservationDTO.getName())
                .documentType(reservationDTO.getDocumentType())
                .documentNumber(reservationDTO.getDocumentNumber())
                .guests(reservationDTO.getGuests())
                .observations(reservationDTO.getObservations())
                .reservationDate(reservationDTO.getReservationDate())
                .build();

        Reservation updatedReservation = reservationService.updateReservation(id, reservation);

        return ResponseEntity.ok().body(updatedReservation);
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getReservation(
            @RequestParam String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate reservationDate = LocalDate.parse(date, formatter);

        List<Reservation> reservations = reservationService.getReservationsByDate(reservationDate);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/available-days")
    public ResponseEntity<List<LocalDate>> getAvailableDays() {
        List<LocalDate> availableDays = reservationService.getAvailableDays();
        return ResponseEntity.ok(availableDays);
    }
}