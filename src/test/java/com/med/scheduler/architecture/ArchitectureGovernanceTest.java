package com.med.scheduler.architecture;

import com.med.scheduler.graph.core.Node;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.med.scheduler", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureGovernanceTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnInfrastructureOrGraph =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..", "..graph..");

    @ArchTest
    static final ArchRule graphNodesMustImplementNodeInterface =
            classes().that().resideInAPackage("..graph.nodes..")
                    .should().implement(Node.class);
}
