package com.brandonkamga.sourcefolio.showcase.infra.persistence;

import com.brandonkamga.sourcefolio.shared.exception.ConflictException;
import com.brandonkamga.sourcefolio.showcase.domain.model.PreviewMedia;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryCoordinates;
import com.brandonkamga.sourcefolio.showcase.domain.model.RepositoryProvider;
import com.brandonkamga.sourcefolio.showcase.domain.model.Showcase;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseId;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseKind;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseSlug;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseStatus;
import com.brandonkamga.sourcefolio.showcase.domain.model.ShowcaseStatusEvent;
import com.brandonkamga.sourcefolio.showcase.domain.model.SiteUrl;
import com.brandonkamga.sourcefolio.showcase.domain.model.SourceRepo;
import com.brandonkamga.sourcefolio.showcase.domain.port.ShowcaseStore;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaShowcaseStore implements ShowcaseStore {

    private final ShowcaseJpaRepository showcases;
    private final SourceRepoJpaRepository sourceRepos;
    private final StatusEventJpaRepository events;

    JpaShowcaseStore(ShowcaseJpaRepository showcases, SourceRepoJpaRepository sourceRepos, StatusEventJpaRepository events) {
        this.showcases = showcases;
        this.sourceRepos = sourceRepos;
        this.events = events;
    }

    // The publication use case is not transactional (it fetches the site first), so the save
    // owns its transaction: showcase, repository and history together, or nothing.
    @Override
    @Transactional
    public Showcase save(Showcase showcase) {
        UUID id = showcase.id().value();
        // The history's identity ids make Hibernate insert early, so the unique index on the
        // repository may answer at saveAll as well as at the final flush.
        try {
            ShowcaseEntity row = showcases.findById(id)
                    .orElseGet(() -> new ShowcaseEntity(id, showcase.slug().value(), showcase.ownerId(), showcase.createdAt()));
            row.update(showcase.kind().name(), showcase.siteUrl().value(), showcase.status().name(), showcase.publishedAt());
            PreviewMedia image = showcase.previewImage();
            PreviewMedia video = showcase.previewVideo();
            row.preview(image == null ? null : image.key(), image == null ? null : image.url(),
                    video == null ? null : video.key(), video == null ? null : video.url());
            showcases.save(row);

            SourceRepo repo = showcase.sourceRepo();
            SourceRepoEntity repoRow = sourceRepos.findById(id).orElseGet(() -> new SourceRepoEntity(id));
            repoRow.update(repo.provider().name(), repo.coordinates().owner(), repo.coordinates().name(), repo.isPublic());
            sourceRepos.save(repoRow);

            events.saveAll(showcase.pendingEvents().stream().map(event -> toEntity(id, event)).toList());
            showcases.flush();
        } catch (DataIntegrityViolationException violation) {
            if (String.valueOf(violation.getMessage()).contains("source_repos_repository")) {
                throw new ConflictException("showcase.repository-taken");
            }
            throw violation;
        }
        return showcase.stored();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Showcase> findByOwner(UUID ownerId) {
        return showcases.findFirstByOwnerUserIdOrderByCreatedAtDesc(ownerId)
                .map(row -> toDomain(row, sourceRepos.findById(row.getId()).orElseThrow()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Showcase> findBySlug(ShowcaseSlug slug) {
        return showcases.findBySlug(slug.value())
                .map(row -> toDomain(row, sourceRepos.findById(row.getId()).orElseThrow()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(ShowcaseSlug slug) {
        return showcases.existsBySlug(slug.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRepository(RepositoryProvider provider, RepositoryCoordinates repository) {
        return sourceRepos.existsRepository(provider.name(), repository.owner(), repository.name());
    }

    // Two reads and one delete whatever the number of showcases; the database cascades the rest.
    @Override
    @Transactional
    public List<Showcase> deleteAllByOwner(UUID ownerId) {
        List<ShowcaseEntity> rows = showcases.findAllByOwnerUserId(ownerId);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<UUID, SourceRepoEntity> repos = sourceRepos.findAllById(rows.stream().map(ShowcaseEntity::getId).toList()).stream()
                .collect(Collectors.toMap(SourceRepoEntity::getShowcaseId, Function.identity()));
        List<Showcase> removed = rows.stream().map(row -> toDomain(row, repos.get(row.getId()))).toList();
        showcases.deleteAllInBatch(rows);
        return removed;
    }

    private static Showcase toDomain(ShowcaseEntity row, SourceRepoEntity repo) {
        return new Showcase(new ShowcaseId(row.getId()), new ShowcaseSlug(row.getSlug()), row.getOwnerUserId(), ShowcaseKind.valueOf(row.getKind()),
                new SiteUrl(row.getSiteUrl()),
                new SourceRepo(RepositoryProvider.valueOf(repo.getProvider()),
                        new RepositoryCoordinates(repo.getOwner(), repo.getName()), repo.isPublic()),
                ShowcaseStatus.valueOf(row.getStatus()), row.getCreatedAt(), row.getPublishedAt(), List.of(),
                row.getPreviewImageKey() == null ? null : new PreviewMedia(row.getPreviewImageKey(), row.getPreviewImageUrl()),
                row.getPreviewVideoKey() == null ? null : new PreviewMedia(row.getPreviewVideoKey(), row.getPreviewVideoUrl()));
    }

    private static StatusEventEntity toEntity(UUID showcaseId, ShowcaseStatusEvent event) {
        return new StatusEventEntity(showcaseId, event.from() == null ? null : event.from().name(), event.to().name(),
                event.reason(), event.actor().name(), event.at());
    }
}
