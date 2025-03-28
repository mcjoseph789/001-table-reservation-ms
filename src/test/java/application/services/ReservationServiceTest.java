package application.services;

import domain.enums.DocumentType;
import domain.exceptions.BusinessException;
import domain.models.Reservation;
import infrastructure.Application;
import infrastructure.repositories.ReservationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @InjectMocks private ReservationService reservationService;


    @Test
    void shouldCreateReservationSuccessfully() {
        // Arrange: Crear una reserva de prueba
        LocalDate today = LocalDate.now();
        Reservation reservation = Reservation.builder()
                .name("John Doe")
                .documentType(DocumentType.CC)
                .documentNumber("123456789")
                .guests(3)
                .observations("Mesa en la terraza")
                .reservationDate(today.atTime(19, 30))
                .build();

        // Simular que hay 9 reservas en el día (permitido)
        Mockito.when(reservationRepository.countByReservationDate(Mockito.any(LocalDateTime.class)))
                .thenReturn(9L);

        // Simular que la persona solo tiene 1 reserva en el día (permitido)
        Mockito.when(reservationRepository.countByReservationDateAndDocumentNumber(
                        Mockito.any(LocalDateTime.class), Mockito.anyString()))
                .thenReturn(1L);

        // Mock del método save() para devolver una reserva con ID asignado
        Mockito.when(reservationRepository.save(Mockito.any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation savedReservation = invocation.getArgument(0);
                    savedReservation.setId(1L); // Simula que la BD asignó un ID
                    return savedReservation;
                });

        // Act: Ejecutar el servicio
        Reservation createdReservation = reservationService.createReservation(reservation);

        // Assert: Verificar que la reserva se creó correctamente
        Assertions.assertNotNull(createdReservation);
        Assertions.assertNotNull(createdReservation.getId());
        Assertions.assertEquals("John Doe", createdReservation.getName());
        Assertions.assertEquals("123456789", createdReservation.getDocumentNumber());
        Assertions.assertEquals(today.atTime(19, 30), createdReservation.getReservationDate());
        Assertions.assertEquals("Mesa en la terraza", createdReservation.getObservations());

        // Verificar que los métodos del repositorio fueron llamados correctamente
        Mockito.verify(reservationRepository, Mockito.times(1)).countByReservationDate(Mockito.any(LocalDateTime.class));
        Mockito.verify(reservationRepository, Mockito.times(1)).countByReservationDateAndDocumentNumber(Mockito.any(LocalDateTime.class), Mockito.anyString());
        Mockito.verify(reservationRepository, Mockito.times(1)).save(Mockito.any(Reservation.class));
    }

    @Test
    public void shouldUpdateReservationSuccessfully() {
        // Datos de prueba
        Long reservationId = 1L;
        LocalDateTime reservationDate = LocalDateTime.now();

        Reservation existingReservation = new Reservation();
        existingReservation.setId(reservationId);
        existingReservation.setName("Juan Pérez");
        existingReservation.setDocumentType(DocumentType.CC);
        existingReservation.setDocumentNumber("123456789");
        existingReservation.setReservationDate(reservationDate);
        existingReservation.setGuests(2);
        existingReservation.setObservations("Mesa cerca de la ventana");

        Reservation updatedReservation = new Reservation();
        updatedReservation.setName("Carlos López");
        updatedReservation.setDocumentType(DocumentType.CC);
        updatedReservation.setDocumentNumber("987654321");
        updatedReservation.setReservationDate(reservationDate.plusDays(1)); // Fecha diferente
        updatedReservation.setGuests(4);
        updatedReservation.setObservations("Mesa en la terraza");

        // Configurar el comportamiento del mock
        Mockito.when(reservationRepository.findById(reservationId)).thenReturn(existingReservation);
        Mockito.when(reservationRepository.countByReservationDateAndDocumentNumber(
                any(LocalDateTime.class), anyString())).thenReturn(1L);
        Mockito.when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar el método que queremos probar
        Reservation result = reservationService.updateReservation(reservationId, updatedReservation);

        // Verificaciones
        Assertions.assertNotNull(result);
        Assertions.assertEquals(updatedReservation.getName(), result.getName());
        Assertions.assertEquals(updatedReservation.getDocumentType(), result.getDocumentType());
        Assertions.assertEquals(updatedReservation.getDocumentNumber(), result.getDocumentNumber());
        Assertions.assertEquals(updatedReservation.getReservationDate(), result.getReservationDate());
        Assertions.assertEquals(updatedReservation.getGuests(), result.getGuests());
        Assertions.assertEquals(updatedReservation.getObservations(), result.getObservations());

        // Verificar que se llamaron los métodos correctos
        Mockito.verify(reservationRepository, Mockito.times(1)).findById(reservationId);
        Mockito.verify(reservationRepository, Mockito.times(1)).countByReservationDateAndDocumentNumber(any(), anyString());
        Mockito.verify(reservationRepository, Mockito.times(1)).save(any(Reservation.class));
    }

    @Test
    public void shouldThrowExceptionWhenMoreThan10ReservationsPerDay() {
        LocalDate today = LocalDate.now();
        Reservation reservation = new Reservation();
        reservation.setReservationDate(today.atTime(19, 30));

        Mockito.when(reservationRepository.countByReservationDate(any(LocalDateTime.class))).thenReturn(10L);

        Assertions.assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(reservation);
        });
    }

    @Test
    public void shouldThrowExceptionWhenClientHasMoreThan2Reservations() {
        LocalDate today = LocalDate.now();
        Reservation reservation = new Reservation();
        reservation.setReservationDate(today.atTime(19, 30));
        reservation.setDocumentNumber("123456789");

        Mockito.when(reservationRepository.countByReservationDateAndDocumentNumber(any(LocalDateTime.class), eq("123456789"))).thenReturn(2L);

        Assertions.assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(reservation);
        });
    }


    @Test
    public void shouldThrowExceptionWhenReservationDoesntExist() {
        // Arrange
        Reservation reservation = Reservation.builder()
                .id(1L)
                .name("Juan Pérez")
                .documentType(DocumentType.CC)
                .documentNumber("123456789")
                .reservationDate(LocalDateTime.of(2025, 3, 11, 19, 30))
                .guests(3)
                .observations("Mesa cerca de la ventana")
                .build();

        // Simulamos que ya hay 10 reservas para el mismo día
        Mockito.when(reservationRepository.findById(anyLong()))
                .thenReturn(null);

        // Ejecutar y verificar que se lanza la excepción esperada
        Assertions.assertThrows(BusinessException.class, () -> {
            reservationService.updateReservation(reservation.getId(), reservation);
        });
    }

    @Test
    public void shouldReturnReservationsForGivenDate() {
        // GIVEN
        LocalDate date = LocalDate.of(2025, 3, 14);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Reservation> mockReservations = Arrays.asList(
                new Reservation(1L, "Alice", DocumentType.CC, "123456", 2, "Ventana", date.atTime(19, 0)),
                new Reservation(2L, "Bob", DocumentType.CC, "789012", 4, "Terraza", date.atTime(20, 30))
        );

        // Simulamos la respuesta del repositorio
        Mockito.when(reservationRepository.findByReservationDateBetween(startOfDay, endOfDay))
                .thenReturn(mockReservations);

        // WHEN
        List<Reservation> reservations = reservationService.getReservationsByDate(date);

        // THEN
        Assertions.assertNotNull(reservations);
        Assertions.assertEquals(2, reservations.size()); // Debe devolver dos reservas
        Assertions.assertEquals("Alice", reservations.get(0).getName()); // Verifica el nombre de la primera reserva
        Assertions.assertEquals("Bob", reservations.get(1).getName()); // Verifica el nombre de la segunda reserva
    }

    @Test
    public void shouldReturnAvailableDays() {
        // GIVEN
        List<LocalDate> allDays = LocalDate.now()
                .datesUntil(LocalDate.now().plusDays(30))
                .toList();

        // Simular que algunos días están completamente reservados (10 o más reservas)
        for (LocalDate day : allDays) {
            if (day.getDayOfMonth() % 5 == 0) { // Simulamos que cada 5 días están llenos
                Mockito.when(reservationRepository.countByReservationDateBetween(
                                day.atStartOfDay(), day.atTime(23, 59, 59)))
                        .thenReturn(10L); // Día lleno
            } else {
                Mockito.when(reservationRepository.countByReservationDateBetween(
                                day.atStartOfDay(), day.atTime(23, 59, 59)))
                        .thenReturn((long) (Math.random() * 9)); // Días disponibles con menos de 10 reservas
            }
        }

        // WHEN
        List<LocalDate> availableDays = reservationService.getAvailableDays();

        // THEN
        Assertions.assertNotNull(availableDays);
        Assertions.assertFalse(availableDays.isEmpty());
        Assertions.assertTrue(availableDays.size() < allDays.size()); // Debe haber menos días disponibles que los 30 iniciales

        // Verificamos que los días eliminados eran los que estaban llenos (cada 5 días)
        for (LocalDate day : allDays) {
            if (day.getDayOfMonth() % 5 == 0) {
                Assertions.assertFalse(availableDays.contains(day)); // Los días con 10 reservas NO deben estar en la lista
            }
        }
    }
}


