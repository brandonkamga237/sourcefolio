package com.brandonkamga.sourcefolio.shared.security;

import com.brandonkamga.sourcefolio.shared.web.LocaleTestConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/** The production filter chain for @WebMvcTest slices in any package (SecurityConfig is package-private). */
@TestConfiguration
@Import({SecurityConfig.class, LocaleTestConfig.class})
public class SecurityTestConfig {
}
