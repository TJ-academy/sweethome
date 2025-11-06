package com.example.sweethome.guy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.sweethome.reservation.PaymentMethod;
import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationStatus;

public record ReservationSummaryDTO(
        int reservationIdx,
        String bookerEmail,
        int reservedHomeId,
        int adult,
        int child,
        int pet,
        LocalDateTime reservedDate,
        String message,
        ReservationStatus reservationStatus,
        PaymentMethod payby,
        String bank,
        Long account,
        int totalMoney,
        LocalDate startDate,
        LocalDate endDate,
        String memoForHost,
        String cancelMessage,
        String memoForCheckIn,
        String memoForCheckOut,
        String merchantUid,
        String impUid
) {
    public static ReservationSummaryDTO from(Reservation r) {
        return new ReservationSummaryDTO(
            r.getReservationIdx(),
            r.getBooker().getEmail(),
            r.getReservedHome().getIdx(),
            r.getAdult(),
            r.getChild(),
            r.getPet(),
            r.getReservedDate(),
            r.getMessage(),
            r.getReservationStatus(),
            r.getPayby(),
            r.getBank(),
            r.getAccount(),
            r.getTotalMoney(),
            r.getStartDate(),
            r.getEndDate(),
            r.getMemoForHost(),
            r.getCancelMessage(),
            r.getMemoForCheckIn(),
            r.getMemoForCheckOut(),
            r.getMerchantUid(),
            r.getImpUid()
        );
    }
}