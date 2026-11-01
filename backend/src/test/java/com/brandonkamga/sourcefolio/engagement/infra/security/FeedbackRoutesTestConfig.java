package com.brandonkamga.sourcefolio.engagement.infra.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/** The package-private route customizer, for @WebMvcTest slices in other packages. */
@TestConfiguration
@Import(FeedbackRoutes.class)
public class FeedbackRoutesTestConfig {
}
