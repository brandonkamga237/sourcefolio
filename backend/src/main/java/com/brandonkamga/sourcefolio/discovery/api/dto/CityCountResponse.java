package com.brandonkamga.sourcefolio.discovery.api.dto;

import com.brandonkamga.sourcefolio.discovery.domain.model.CityCount;

public record CityCountResponse(String country, String city, long count) {

    public static CityCountResponse of(CityCount count) {
        return new CityCountResponse(count.countryCode(), count.city(), count.count());
    }
}
