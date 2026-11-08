package com.brandonkamga.sourcefolio.discovery.domain.model;

/** How many listed profiles gave this city, as they typed it; city is null when they gave none. */
public record CityCount(String countryCode, String city, long count) {
}
