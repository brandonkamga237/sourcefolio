package com.brandonkamga.sourcefolio.showcase.application;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateDetection;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.model.PublicRepository;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.port.CandidateStore;
import com.brandonkamga.sourcefolio.showcase.domain.port.DetectionScheduler;
import com.brandonkamga.sourcefolio.showcase.domain.port.RepositoryHost;

import java.time.Clock;
import java.util.List;
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
        List<PublicRepository> found = repositories.listPublicRepositories(login);
        List<ShowcaseCandidate> detected = CandidateDetection.detect(login, found);
        candidates.replace(memberId, new CandidateScan(clock.instant(), detected, CandidateDetection.withoutSite(found, detected)));
    }

    @Override
    public Optional<CandidateScan> findCandidates(UUID memberId) {
        return candidates.find(memberId);
    }
}
