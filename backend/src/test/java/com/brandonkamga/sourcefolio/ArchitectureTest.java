package com.brandonkamga.sourcefolio;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.dependencies.Slice;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * The architecture of .claude/skills/design-patterns, checked rather than hoped for. Every rule
 * names packages by their role (domain, application, infra.persistence…), so a new module is
 * covered the day it is created, without being added to a list.
 */
class ArchitectureTest {

    private static final String ROOT = "com.brandonkamga.sourcefolio";
    private static final Pattern MODULE = Pattern.compile(Pattern.quote(ROOT) + "\\.([a-z]+)\\..*");

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT);
    }

    @Test
    @DisplayName("domain and use cases are plain Java: no framework, persistence, web, job or browser library")
    void coreIsPlainJava() {
        noClasses().that().resideInAnyPackage(ROOT + "..domain..", ROOT + "..application..", ROOT + ".shared.exception..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "jakarta.servlet..", "org.hibernate..",
                        "org.jobrunr..", "com.microsoft.playwright..", "software.amazon..", "io.micrometer..",
                        "tools.jackson..", ROOT + "..infra..", ROOT + "..api..")
                .because("the core declares ports, and adapters in infra implement them")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("JPA is used in infra.persistence only")
    void jpaStaysInPersistenceAdapters() {
        noClasses().that().resideOutsideOfPackage(ROOT + "..infra.persistence..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..", "org.springframework.data..", "org.hibernate..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("an entity never leaves infra.persistence: nothing else even names one")
    void entitiesStayInPersistenceAdapters() {
        noClasses().that().resideOutsideOfPackage(ROOT + "..infra.persistence..")
                .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("another module is reached through its application layer, never its ports or adapters")
    void portsAndAdaptersStayInside() {
        classes().that().resideInAPackage(ROOT + "..")
                .should(notReachIntoAnotherModulesPortsOrAdapters())
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("modules depend on each other in one direction only: no cycle between business capabilities")
    void modulesHaveNoCycles() {
        slices().matching(ROOT + ".(*)..")
                .that(DescribedPredicate.describe("are not shared", (Slice slice) -> !"shared".equals(slice.getNamePart(1))))
                .should().beFreeOfCycles()
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("shared holds building blocks only: it depends on no module")
    void sharedDependsOnNoModule() {
        noClasses().that().resideInAPackage(ROOT + ".shared..")
                .should().dependOnClassesThat(DescribedPredicate.describe("belong to a module",
                        (JavaClass target) -> moduleOf(target).filter(module -> !"shared".equals(module)).isPresent()))
                .allowEmptyShould(true)
                .check(classes);
    }

    private static ArchCondition<JavaClass> notReachIntoAnotherModulesPortsOrAdapters() {
        return new ArchCondition<>("not depend on another module's domain.port or infra") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                Optional<String> own = moduleOf(origin);
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    Optional<String> theirs = moduleOf(target);
                    boolean internal = target.getPackageName().contains(".domain.port") || target.getPackageName().contains(".infra");
                    if (internal && theirs.isPresent() && !theirs.equals(own)) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                }
            }
        };
    }

    private static Optional<String> moduleOf(JavaClass javaClass) {
        Matcher matcher = MODULE.matcher(javaClass.getName());
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
