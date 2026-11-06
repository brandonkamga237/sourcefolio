package com.brandonkamga.sourcefolio.contribution.domain.model;

import java.time.YearMonth;

public record MonthActivity(YearMonth month, int contributions) {

    public boolean active() {
        return contributions > 0;
    }
}
