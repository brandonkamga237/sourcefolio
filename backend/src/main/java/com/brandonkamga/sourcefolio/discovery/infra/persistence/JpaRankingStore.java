package com.brandonkamga.sourcefolio.discovery.infra.persistence;

import com.brandonkamga.sourcefolio.discovery.domain.model.CityCount;
import com.brandonkamga.sourcefolio.discovery.domain.model.ProjectCount;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFacets;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingFilter;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingPage;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingRow;
import com.brandonkamga.sourcefolio.discovery.domain.model.RankingSort;
import com.brandonkamga.sourcefolio.discovery.domain.port.RankingStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The rankings in native SQL: the sort expression comes from a fixed table, never from the
 * request, and every value is a bound parameter. Pages are read by keyset on the sort key and
 * the member id, so a deep page costs what the first one does.
 */
@Component
class JpaRankingStore implements RankingStore {

    // The keys of each sort, all bigint so one cursor format serves them; ties end on member_id.
    private static final Map<RankingSort, Sort> SORTS = Map.of(
            RankingSort.REGULARITY, new Sort(List.of("active_months::bigint", "public_contributions::bigint"), true),
            RankingSort.CONTRIBUTIONS, new Sort(List.of("public_contributions::bigint"), true),
            RankingSort.STARS, new Sort(List.of("stars::bigint", "followers::bigint"), true),
            RankingSort.EXTERNAL, new Sort(List.of("merged_elsewhere::bigint"), true),
            RankingSort.SENIORITY, new Sort(List.of("extract(epoch from account_created_at)::bigint"), false));

    private static final String LISTED = """
            published AND analyzed_at IS NOT NULL
            AND (CAST(:country AS VARCHAR) IS NULL OR country_code = CAST(:country AS VARCHAR))
            AND (CAST(:language AS VARCHAR) IS NULL OR CAST(:language AS VARCHAR) = ANY(languages))""";

    private static final String COLUMNS = """
            member_id, login, name, country_code, city, account_created_at, public_contributions, active_months,
            months, stars, followers, merged_elsewhere, languages""";

    @PersistenceContext
    private EntityManager entityManager;

    // Writes come from event listeners that run after the source transaction committed (or
    // outside any, for analyses): each opens its own.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveIndicators(UUID memberId, String login, String name, Instant analyzedAt, Instant accountCreatedAt,
                               int publicContributions, int activeMonths, List<Integer> months, int stars, int followers,
                               int mergedPullRequestsElsewhere, List<String> languages, List<String> externalRepositories) {
        entityManager.createNativeQuery("""
                        INSERT INTO ranking_entries (member_id, login, name, analyzed_at, account_created_at, public_contributions,
                                                     active_months, months, stars, followers, merged_elsewhere, languages, external_repositories)
                        VALUES (:member, :login, :name, :analyzedAt, :createdAt, :contributions, :activeMonths,
                                CAST(:months AS INTEGER[]), :stars, :followers, :merged, CAST(:languages AS VARCHAR(100)[]),
                                CAST(:repositories AS VARCHAR(200)[]))
                        ON CONFLICT (member_id) DO UPDATE SET
                            login = EXCLUDED.login, name = EXCLUDED.name, analyzed_at = EXCLUDED.analyzed_at,
                            account_created_at = EXCLUDED.account_created_at, public_contributions = EXCLUDED.public_contributions,
                            active_months = EXCLUDED.active_months, months = EXCLUDED.months, stars = EXCLUDED.stars,
                            followers = EXCLUDED.followers, merged_elsewhere = EXCLUDED.merged_elsewhere, languages = EXCLUDED.languages,
                            external_repositories = EXCLUDED.external_repositories""")
                .setParameter("member", memberId)
                .setParameter("login", login)
                .setParameter("name", name)
                .setParameter("analyzedAt", analyzedAt)
                .setParameter("createdAt", accountCreatedAt)
                .setParameter("contributions", publicContributions)
                .setParameter("activeMonths", activeMonths)
                .setParameter("months", arrayLiteral(months.stream().map(String::valueOf).toList(), false))
                .setParameter("stars", stars)
                .setParameter("followers", followers)
                .setParameter("merged", mergedPullRequestsElsewhere)
                .setParameter("languages", arrayLiteral(languages, true))
                .setParameter("repositories", arrayLiteral(externalRepositories, true))
                .executeUpdate();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProfile(UUID memberId, String countryCode, String city, boolean published) {
        entityManager.createNativeQuery("""
                        INSERT INTO ranking_entries (member_id, country_code, city, published)
                        VALUES (:member, CAST(:country AS CHAR(2)), :city, :published)
                        ON CONFLICT (member_id) DO UPDATE SET
                            country_code = EXCLUDED.country_code, city = EXCLUDED.city, published = EXCLUDED.published""")
                .setParameter("member", memberId)
                .setParameter("country", countryCode)
                .setParameter("city", city)
                .setParameter("published", published)
                .executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public RankingPage page(RankingFilter filter, RankingSort sort, String cursor, int size) {
        Sort keys = SORTS.get(sort);
        Cursor after = cursor == null ? null : Cursor.decode(cursor, keys.expressions().size());
        String direction = keys.descending() ? "DESC" : "ASC";
        String key = "(" + String.join(", ", keys.expressions()) + ", member_id)";
        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNS).append(", ");
        sql.append(String.join(", ", keys.expressions().stream().map(expression -> expression + " AS k" + keys.expressions().indexOf(expression)).toList()));
        sql.append(" FROM ranking_entries WHERE ").append(LISTED);
        if (after != null) {
            sql.append(" AND ").append(key).append(keys.descending() ? " < " : " > ").append("(");
            for (int index = 0; index < after.values().size(); index++) {
                sql.append(":c").append(index).append(", ");
            }
            sql.append("CAST(:cid AS UUID))");
        }
        sql.append(" ORDER BY ");
        sql.append(keys.expressions().stream().map(expression -> expression + " " + direction).collect(Collectors.joining(", ")));
        sql.append(", member_id ").append(direction).append(" LIMIT :limit");

        Query query = bindFilter(entityManager.createNativeQuery(sql.toString()), filter).setParameter("limit", size + 1);
        if (after != null) {
            for (int index = 0; index < after.values().size(); index++) {
                query.setParameter("c" + index, after.values().get(index));
            }
            query.setParameter("cid", after.memberId().toString());
        }
        @SuppressWarnings("unchecked")
        List<Object[]> found = query.getResultList();
        int offset = after == null ? 0 : after.offset();
        List<RankingRow> rows = new ArrayList<>();
        for (int index = 0; index < Math.min(size, found.size()); index++) {
            rows.add(toRow(offset + index + 1, found.get(index)));
        }
        String next = null;
        if (found.size() > size) {
            Object[] last = found.get(size - 1);
            List<Long> values = new ArrayList<>();
            for (int index = 0; index < keys.expressions().size(); index++) {
                values.add(((Number) last[13 + index]).longValue());
            }
            next = new Cursor(offset + size, values, (UUID) last[0]).encode();
        }
        long total = ((Number) bindFilter(entityManager.createNativeQuery("SELECT count(*) FROM ranking_entries WHERE " + LISTED), filter)
                .getSingleResult()).longValue();
        return new RankingPage(rows, next, total);
    }

    // Each facet ignores its own filter, so a visitor sees what else they could pick.
    @Override
    @Transactional(readOnly = true)
    public RankingFacets facets(RankingFilter filter) {
        @SuppressWarnings("unchecked")
        List<Object[]> countries = bindFilter(entityManager.createNativeQuery(
                "SELECT country_code, count(*) FROM ranking_entries WHERE " + LISTED + " AND country_code IS NOT NULL"
                        + " GROUP BY country_code ORDER BY count(*) DESC, country_code"), new RankingFilter(null, filter.language()))
                .getResultList();
        @SuppressWarnings("unchecked")
        List<Object[]> languages = bindFilter(entityManager.createNativeQuery(
                "SELECT language, count(*) FROM ranking_entries, unnest(languages) AS language WHERE " + LISTED
                        + " GROUP BY language ORDER BY count(*) DESC, language LIMIT 40"), new RankingFilter(filter.countryCode(), null))
                .getResultList();
        return new RankingFacets(
                countries.stream().map(row -> new RankingFacets.Count(row[0].toString().strip(), ((Number) row[1]).longValue())).toList(),
                languages.stream().map(row -> new RankingFacets.Count((String) row[0], ((Number) row[1]).longValue())).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityCount> cityCounts(RankingFilter filter) {
        @SuppressWarnings("unchecked")
        List<Object[]> found = bindFilter(entityManager.createNativeQuery(
                "SELECT country_code, city, count(*) FROM ranking_entries WHERE " + LISTED + " AND country_code IS NOT NULL"
                        + " GROUP BY country_code, city ORDER BY count(*) DESC, country_code, city LIMIT 2000"), filter)
                .getResultList();
        return found.stream().map(row -> new CityCount(row[0].toString().strip(), (String) row[1], ((Number) row[2]).longValue())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectCount> acceptedProjects() {
        @SuppressWarnings("unchecked")
        List<Object[]> found = bindFilter(entityManager.createNativeQuery(
                "SELECT repository, count(*) FROM ranking_entries, unnest(external_repositories) AS repository WHERE " + LISTED
                        + " GROUP BY repository ORDER BY count(*) DESC, repository LIMIT 30"), new RankingFilter(null, null))
                .getResultList();
        return found.stream().map(row -> new ProjectCount((String) row[0], ((Number) row[1]).longValue())).toList();
    }

    private static Query bindFilter(Query query, RankingFilter filter) {
        return query.setParameter("country", filter.countryCode()).setParameter("language", filter.language());
    }

    private static RankingRow toRow(int position, Object[] row) {
        return new RankingRow(position, (UUID) row[0], (String) row[1], (String) row[2],
                row[3] == null ? null : row[3].toString().strip(), (String) row[4], instant(row[5]),
                ((Number) row[6]).intValue(), ((Number) row[7]).intValue(), integers(row[8]), ((Number) row[9]).intValue(),
                ((Number) row[10]).intValue(), ((Number) row[11]).intValue(), strings(row[12]));
    }

    private static Instant instant(Object value) {
        return switch (value) {
            case null -> null;
            case Instant instant -> instant;
            case OffsetDateTime offset -> offset.toInstant();
            case Timestamp timestamp -> timestamp.toInstant();
            default -> throw new IllegalStateException("Unexpected timestamp " + value.getClass());
        };
    }

    private static List<Integer> integers(Object value) {
        if (value instanceof Integer[] array) return Arrays.asList(array);
        if (value instanceof int[] array) return Arrays.stream(array).boxed().toList();
        return List.of();
    }

    private static List<String> strings(Object value) {
        return value instanceof String[] array ? Arrays.asList(array) : List.of();
    }

    // A PostgreSQL array literal: languages are quoted, so "C++" or "Jupyter Notebook" survive.
    private static String arrayLiteral(List<String> values, boolean quoted) {
        return values.stream()
                .map(value -> quoted ? "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" : value)
                .collect(Collectors.joining(",", "{", "}"));
    }

    private record Sort(List<String> expressions, boolean descending) {
    }

    /** Where the next page starts: how many rows came before, the sort key and the member id of the last row. */
    record Cursor(int offset, List<Long> values, UUID memberId) {

        String encode() {
            String raw = offset + ";" + values.stream().map(String::valueOf).collect(Collectors.joining(",")) + ";" + memberId;
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        static Cursor decode(String encoded, int keys) {
            try {
                String[] parts = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8).split(";");
                List<Long> values = Arrays.stream(parts[1].split(",")).map(Long::parseLong).toList();
                if (values.size() != keys) {
                    throw new IllegalArgumentException("cursor of another sort");
                }
                return new Cursor(Integer.parseInt(parts[0]), values, UUID.fromString(parts[2]));
            } catch (RuntimeException unreadable) {
                throw new IllegalArgumentException("Unreadable ranking cursor", unreadable);
            }
        }
    }
}
