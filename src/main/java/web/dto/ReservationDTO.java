package web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import domain.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {
    private String name;
    private DocumentType documentType;
    private String documentNumber;
    private int guests;
    private String observations;
    private LocalDateTime reservationDate;
}
