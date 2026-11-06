package com.brandonkamga.sourcefolio.contribution.domain.model;

import java.time.LocalDate;

/** One square of the contribution calendar: how many contributions, and GitHub's shade 0 to 4. */
public record CalendarDay(LocalDate date, int contributions, int level) {

    public CalendarDay {
        if (contributions < 0 || level < 0 || level > 4) {
            throw new IllegalArgumentException("invalid calendar day " + date);
        }
    }
}
