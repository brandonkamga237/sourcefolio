package com.brandonkamga.sourcefolio.profile.domain.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * The interests a member can name (up to four, ranked), stored and exchanged as slugs and
 * translated at display time. The first ten are the specialties of the first version.
 */
public enum Specialty {
    FRONTEND("frontend"),
    BACKEND("backend"),
    FULLSTACK("fullstack"),
    MOBILE("mobile"),
    DEVOPS("devops"),
    DATA_AI("data-ai"),
    GAME("game"),
    EMBEDDED("embedded"),
    SECURITY("security"),
    OTHER("other"),
    DESKTOP("desktop"),
    API("api"),
    WEB3("web3"),
    LOW_CODE("low-code"),
    MACHINE_LEARNING("machine-learning"),
    DATA_ENGINEERING("data-engineering"),
    DATA_ANALYSIS("data-analysis"),
    COMPUTER_VISION("computer-vision"),
    NLP("nlp"),
    GENERATIVE_AI("generative-ai"),
    CLOUD("cloud"),
    SRE("sre"),
    NETWORKING("networking"),
    DATABASE("database"),
    UI_DESIGN("ui-design"),
    UX_DESIGN("ux-design"),
    PRODUCT("product"),
    DESIGN_SYSTEMS("design-systems"),
    MOTION_DESIGN("motion-design"),
    ACCESSIBILITY("accessibility"),
    CREATIVE_CODING("creative-coding"),
    TESTING("testing"),
    PERFORMANCE("performance"),
    OPEN_SOURCE("open-source"),
    DOCUMENTATION("documentation"),
    FINTECH("fintech"),
    HEALTHTECH("healthtech"),
    EDTECH("edtech"),
    ECOMMERCE("ecommerce"),
    AGRITECH("agritech"),
    IOT("iot"),
    ROBOTICS("robotics"),
    AR_VR("ar-vr"),
    TECH_LEAD("tech-lead"),
    FREELANCE("freelance"),
    TEACHING("teaching"),
    DEVREL("devrel"),
    COMMUNITY("community"),
    ENTREPRENEURSHIP("entrepreneurship"),
    RESEARCH("research");

    private final String slug;

    Specialty(String slug) {
        this.slug = slug;
    }

    public String slug() {
        return slug;
    }

    public static Optional<Specialty> fromSlug(String slug) {
        return Arrays.stream(values()).filter(specialty -> specialty.slug.equals(slug)).findFirst();
    }
}
