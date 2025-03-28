package application.mapper;

import domain.models.Reservation;
import org.mapstruct.Mapper;
import web.dto.ReservationDTO;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    Reservation toEntity(ReservationDTO dto);
    ReservationDTO toDto(Reservation entity);
}
