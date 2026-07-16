package com.maasteria.agent.api;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@AnalyzeClasses(packages = "com.maasteria.agent", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule hexagonal_dependency_direction = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("Api").definedBy("..api..")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Api")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Api")
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Api")
            .whereLayer("Api").mayNotBeAccessedByAnyLayer();

    @ArchTest
    static final ArchRule domain_is_framework_independent = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "..application..", "..infrastructure..", "..api..");

    @ArchTest
    static final ArchRule application_is_independent_from_spring_and_adapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "..infrastructure..", "..api..");

    @ArchTest
    static final ArchRule input_ports_are_interfaces = classes()
            .that().resideInAPackage("..application.port.in..")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule output_ports_are_interfaces = classes()
            .that().resideInAPackage("..application.port.out..")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule application_services_follow_naming = classes()
            .that().resideInAPackage("..application.service..")
            .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule rest_controllers_are_annotated = classes()
            .that().resideInAPackage("..api.controller..")
            .and().areTopLevelClasses()
            .and().haveSimpleNameNotEndingWith("ExceptionHandler")
            .should().beAnnotatedWith(RestController.class);

    @ArchTest
    static final ArchRule configuration_classes_are_annotated = classes()
            .that().resideInAPackage("..infrastructure.config..")
            .should().beAnnotatedWith(Configuration.class);

    @ArchTest
    static final ArchRule bean_factories_live_in_configuration = methods()
            .that().areAnnotatedWith(Bean.class)
            .should().beDeclaredInClassesThat().resideInAPackage("..config..");

    @ArchTest
    static final ArchRule field_injection_is_forbidden = noFields()
            .should().beAnnotatedWith(Autowired.class);

    @ArchTest
    static final ArchRule api_does_not_use_concrete_rag_adapters = noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure.rag..", "..infrastructure.ai..", "..infrastructure.evaluator..");
}
