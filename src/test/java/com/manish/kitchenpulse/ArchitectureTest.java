package com.manish.kitchenpulse;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Guards the modular-monolith rules so they cannot erode silently. */
@AnalyzeClasses(packages = "com.manish.kitchenpulse", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule modulesHaveNoCyclicDependencies =
            slices().matching("com.manish.kitchenpulse.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule commonDoesNotDependOnFeatureModules =
            noClasses().that().resideInAPackage("..common..")
                    .should().dependOnClassesThat().resideInAnyPackage("..identity..", "..branch..", "..catalog..");

    @ArchTest
    static final ArchRule catalogAndBranchDoNotDependOnIdentity =
            noClasses().that().resideInAnyPackage("..catalog..", "..branch..")
                    .should().dependOnClassesThat().resideInAPackage("..identity..");
}
