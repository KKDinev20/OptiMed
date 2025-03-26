package com.optimed.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Embeddable
@Getter
@Setter
public class TimeSlot {
    private LocalTime startTime;
    private LocalTime endTime;
}