package com.brandonkamga.sourcefolio;

import com.brandonkamga.sourcefolio.support.PostgresIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SourcefolioApplicationIT extends PostgresIT {

    @Test
    @DisplayName("the application starts on PostgreSQL with its migrations and its job storage")
    void contextLoads() {
    }
}
