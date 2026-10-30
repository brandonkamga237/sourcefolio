package com.brandonkamga.sourcefolio.discovery.infra.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/** The package-private route customizer, for @WebMvcTest slices in other packages. */
@TestConfiguration
@Import(CatalogRoutes.class)
public class CatalogRoutesTestConfig {
}
