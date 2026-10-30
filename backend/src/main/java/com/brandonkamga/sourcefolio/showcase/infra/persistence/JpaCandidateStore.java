package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateReason;
import com.brandonkamga.sourcefolio.showcase.domain.model.CandidateScan;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseCandidate;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.domain.port.CandidateStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class JpaCandidateStore implements CandidateStore {

    private final CandidateScanJpaRepository scans;
    private final CandidateJpaRepository candidates;

    JpaCandidateStore(CandidateScanJpaRepository scans, CandidateJpaRepository candidates) {
        this.scans = scans;
        this.candidates = candidates;
    }

    // The detection use case is not transactional (it calls GitHub first), so the replacement
    // owns its transaction here: delete, upsert the scan, insert the new list, or nothing.
    @Override
    @Transactional
    public void replace(UUID memberId, CandidateScan scan) {
        candidates.deleteAllOfMember(memberId);
        scans.save(new CandidateScanEntity(memberId, scan.scannedAt()));
        List<CandidateEntity> rows = new ArrayList<>();
        for (int position = 0; position < scan.candidates().size(); position++) {
            ShowcaseCandidate found = scan.candidates().get(position);
            rows.add(new CandidateEntity(UUID.randomUUID(), memberId, (short) position, found.repository().owner(),
                    found.repository().name(), found.siteUrl().value(), found.reason().name()));
        }
        candidates.saveAll(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CandidateScan> find(UUID memberId) {
        return scans.findById(memberId).map(scan -> new CandidateScan(scan.getScannedAt(),
                candidates.findByMemberIdOrderByPosition(memberId).stream().map(JpaCandidateStore::toDomain).toList()));
    }

    private static ShowcaseCandidate toDomain(CandidateEntity row) {
        return new ShowcaseCandidate(new RepositoryCoordinates(row.getRepositoryOwner(), row.getRepositoryName()),
                new SiteUrl(row.getSiteUrl()), CandidateReason.valueOf(row.getReason()));
    }
}
