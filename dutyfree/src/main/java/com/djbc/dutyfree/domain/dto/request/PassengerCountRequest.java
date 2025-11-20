package com.djbc.dutyfree.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerCountRequest {

    @NotNull(message = "Count date is required")
    private LocalDate countDate;

    @Min(value = 0, message = "Total passengers must be 0 or greater")
    private Integer totalPassengers;

    @Min(value = 0, message = "Arriving passengers must be 0 or greater")
    private Integer arrivingPassengers;

    @Min(value = 0, message = "Departing passengers must be 0 or greater")
    private Integer departingPassengers;

    @Min(value = 0, message = "International passengers must be 0 or greater")
    private Integer internationalPassengers;

    @Size(max = 20, message = "Flight number must not exceed 20 characters")
    private String flightNumber;

    @Size(max = 100, message = "Airline must not exceed 100 characters")
    private String airline;

    @Size(max = 100, message = "Destination must not exceed 100 characters")
    private String destination;

    @Size(max = 20, message = "Count type must not exceed 20 characters")
    private String countType = "MANUAL";

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
