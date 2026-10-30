package com.brandonkamga.sourcefolio.shared.web;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/** Lets tests in other packages use the production locale resolver, which is package-private. */
@TestConfiguration
@Import(LocaleConfig.class)
public class LocaleTestConfig {
}
