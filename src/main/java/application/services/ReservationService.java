package application.services;

import domain.exceptions.BusinessException;
import domain.models.Reservation;
import infrastructure.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public Reservation createReservation(Reservation reservation) {

        LocalDate reservationDay = reservation.getReservationDate().toLocalDate();

        // Validar que no haya más de 10 reservas en el día
        long countReservations = reservationRepository.countByReservationDate(reservation.getReservationDate());

        // Validar que no haya más de 2 reservas de la misma persona en un mismo día
        long countReservationsByDocument = reservationRepository.countByReservationDateAndDocumentNumber(
                reservation.getReservationDate(),
                reservation.getDocumentNumber()
        );

        if (countReservations >= 10) {
            throw new BusinessException("No se pueden hacer más de 10 reservas en un mismo día.");
        }

        if (countReservationsByDocument >= 2) {
            throw new BusinessException("No puedes tener más de 2 reservas en el mismo día.");
        }

        // Validar que la cantidad de comensales sea máximo 4
        if (reservation.getGuests() > 4) {
            throw new BusinessException("El número máximo de comensales por reserva es 4.");
        }

        return reservationRepository.save(reservation);
    }

    public Reservation updateReservation(Long id, Reservation reservation) {
        // Verificar si la reserva existe
        Reservation existingReservation = reservationRepository.findById(id);
        if (existingReservation == null) {
            throw new BusinessException("No se encontró la reserva con ID: " + id);
        }

        // Validar que no haya más de 2 reservas de la misma persona en el día
        long countByClient = reservationRepository.countByReservationDateAndDocumentNumber(
                reservation.getReservationDate(),
                reservation.getDocumentNumber()
        );

        if (countByClient >= 2) {
            throw new BusinessException("No puedes tener más de 2 reservas en el mismo día.");
        }

        // Actualizamos los campos de la reserva existente con los nuevos valores
        existingReservation.setName(reservation.getName());
        existingReservation.setDocumentType(reservation.getDocumentType());
        existingReservation.setDocumentNumber(reservation.getDocumentNumber());
        existingReservation.setReservationDate(reservation.getReservationDate());
        existingReservation.setGuests(reservation.getGuests());
        existingReservation.setObservations(reservation.getObservations());

        // Guardamos la reserva actualizada en la base de datos
        return reservationRepository.save(existingReservation);
    }

    public List<Reservation> getReservationsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return reservationRepository.findByReservationDateBetween(startOfDay, endOfDay);
    }

    public List<LocalDate> getAvailableDays() {
        List<LocalDate> allDays = generateNext30Days();

        return allDays.stream()
                .filter(day -> reservationRepository.countByReservationDateBetween(
                        day.atStartOfDay(), day.atTime(23, 59, 59)) < 10)
                .collect(Collectors.toList());
    }

    private List<LocalDate> generateNext30Days() {
        LocalDate today = LocalDate.now();
        return today.datesUntil(today.plusDays(30)).toList();
    }

}
