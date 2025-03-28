package web.controllers;

import application.services.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domain.enums.DocumentType;
import domain.models.Reservation;
import infrastructure.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import web.dto.ReservationDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class) // Especificar la clase principal
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ReservationService reservationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldCreateReservation() throws Exception {
        // GIVEN
        ReservationDTO reservationDTO = new ReservationDTO("Juan", DocumentType.CC, "12345678", 2, "Sin gluten", LocalDateTime.now());
        Reservation reservation = new Reservation(1L, "Juan", DocumentType.CC, "12345678", 2, "Sin gluten", LocalDateTime.now());

        Mockito.when(reservationService.createReservation(Mockito.any(Reservation.class)))
                .thenReturn(reservation);

        // WHEN - THEN
        mockMvc.perform(post("/api/restaurant/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Juan"));
    }

    @Test
    public void shouldReturnReservationsByDate() throws Exception {
        // GIVEN
        LocalDate date = LocalDate.of(2025, 3, 14);
        List<Reservation> reservations = List.of(
                new Reservation(1L, "Juan", DocumentType.CC, "12345678", 2, "Sin gluten", date.atTime(19, 30))
        );

        Mockito.when(reservationService.getReservationsByDate(date))
                .thenReturn(reservations);

        // WHEN - THEN
        mockMvc.perform(get("/api/restaurant/reservations")
                        .param("date", "14/03/2025")) // Validamos formato dd/MM/yyyy
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Juan"));
    }

    @Test
    public void shouldReturnAvailableDays() throws Exception {
        // GIVEN
        List<LocalDate> availableDays = List.of(LocalDate.of(2025, 3, 15), LocalDate.of(2025, 3, 20));

        Mockito.when(reservationService.getAvailableDays())
                .thenReturn(availableDays);

        // WHEN - THEN
        mockMvc.perform(get("/api/restaurant/available-days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("2025-03-15"));
    }
}