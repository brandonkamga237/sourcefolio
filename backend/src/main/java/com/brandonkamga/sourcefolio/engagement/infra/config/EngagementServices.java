package com.brandonkamga.sourcefolio.engagement.infra.config;

import com.brandonkamga.sourcefolio.engagement.application.EngagementEvents;
import com.brandonkamga.sourcefolio.engagement.application.Likes;
import com.brandonkamga.sourcefolio.engagement.application.LikesImpl;
import com.brandonkamga.sourcefolio.engagement.domain.port.LikeStore;
import com.brandonkamga.sourcefolio.shared.config.TransactionalServices;
import com.brandonkamga.sourcefolio.showcase.application.PublicShowcases;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class EngagementServices {

    @Bean
    Likes likes(PublicShowcases showcases, LikeStore likes, EngagementEvents events, Clock clock, TransactionalServices transactional) {
        return transactional.wrap(Likes.class, new LikesImpl(showcases, likes, events, clock));
    }
}
