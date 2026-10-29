package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateDetection;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.port.CandidateStore;
import com.brandonkamga.sourcefolio.showcase.domain.port.DetectionScheduler;
import com.brandonkamga.sourcefolio.showcase.domain.port.RepositoryHost;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

public class ShowcaseDetectionImpl implements ShowcaseDetection {

    private final RepositoryHost repositories;
    private final CandidateStore candidates;
    private final DetectionScheduler scheduler;
    private final Clock clock;

    public ShowcaseDetectionImpl(RepositoryHost repositories, CandidateStore candidates, DetectionScheduler scheduler, Clock clock) {
        this.repositories = repositories;
        this.candidates = candidates;
        this.scheduler = scheduler;
        this.clock = clock;
    }

    @Override
    public void requestDetection(UUID memberId, String login) {
        scheduler.scheduleDetection(memberId, login);
    }

    @Override
    public void detect(UUID memberId, String login) {
        CandidateScan scan = new CandidateScan(clock.instant(),
                CandidateDetection.detect(login, repositories.listPublicRepositories(login)));
        candidates.replace(memberId, scan);
    }

    @Override
    public Optional<CandidateScan> findCandidates(UUID memberId) {
        return candidates.find(memberId);
    }
}
