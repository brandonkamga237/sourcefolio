package com.brandonkamga.sourcefolio.account.api.dto;

import com.brandonkamga.sourcefolio.account.application.Member;

public record MeResponse(String id, String login, String language) {

    public static MeResponse of(Member member) {
        return new MeResponse(member.id().toString(), member.login(), member.language().tag());
    }
}
