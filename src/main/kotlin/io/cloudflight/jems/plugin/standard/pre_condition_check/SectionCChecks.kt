package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.ProjectDataSectionC
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectCooperationCriteriaData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectHorizontalPrinciplesData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectHorizontalPrinciplesEffectData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectManagementData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.overallObjective.ProjectOverallObjectiveData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceBenefitData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceStrategyData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceSynergyData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.results.ProjectResultData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.ProjectWorkPackageData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage

private const val SECTION_C_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.c"
private const val SECTION_C_ERROR_MESSAGES_PREFIX = "$SECTION_C_MESSAGES_PREFIX.error"
private const val SECTION_C_INFO_MESSAGES_PREFIX = "$SECTION_C_MESSAGES_PREFIX.info"

fun checkSectionC(sectionCData: ProjectDataSectionC?): PreConditionCheckMessage =
    buildPreConditionCheckMessage(
        messageKey = "$SECTION_C_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        checkIfProjectOverallObjectiveIsProvided(sectionCData?.projectOverallObjective),

        checkIfAtLeastOneTargetGroupIsAdded(sectionCData?.projectRelevance?.projectBenefits),

        checkIfSpecificationIsProvidedForAllTargetGroups(sectionCData?.projectRelevance?.projectBenefits),

        checkIfAtLeastOneStrategyIsAdded(sectionCData?.projectRelevance?.projectStrategies),

        checkIfSpecificationIsProvidedForAllStrategies(sectionCData?.projectRelevance?.projectStrategies),

        checkIfSynergiesAreNotEmpty(sectionCData?.projectRelevance?.projectSynergies),

        checkIfAtLeastOneWorkPackageIsAdded(sectionCData?.projectWorkPackages),

        checkIfNamesOfWorkPackagesAreProvided(sectionCData?.projectWorkPackages),

        checkIfAtLeastOneOutputForEachWorkPackageIsAdded(sectionCData?.projectWorkPackages),

        checkIfAtLeastOneResultIsAdded(sectionCData?.projectResults),

        checkIfFinancialManagementIsProvided(sectionCData?.projectManagement?.projectFinancialManagement),

        checkIfSelectedCooperationCriteriaAreValid(sectionCData?.projectManagement?.projectCooperationCriteria),

        checkIfDescriptionForAllSelectedCooperationCriteriaIsProvided(sectionCData?.projectManagement),

        checkIfTypeOfContributionsAreProvided(sectionCData?.projectManagement?.projectHorizontalPrinciples),

        checkIfDescriptionForTypeOfContributionIsProvided(sectionCData?.projectManagement)
    )

private fun checkIfProjectOverallObjectiveIsProvided(projectOverallObjectiveData: ProjectOverallObjectiveData?) =
    when (projectOverallObjectiveData) {
        null -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.overall.objective.should.be.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.overall.objective.is.provided")
    }

private fun checkIfAtLeastOneTargetGroupIsAdded(projectBenefits: List<ProjectRelevanceBenefitData>?) =
    when {
        projectBenefits.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.target.group.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.at.least.one.target.group.is.added")
    }

private fun checkIfSpecificationIsProvidedForAllTargetGroups(projectBenefits: List<ProjectRelevanceBenefitData>?) =
    when {
        projectBenefits.isNullOrEmpty() -> null
        projectBenefits.any { it.specification.isNullOrEmptyOrMissingAnyTranslation() } ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.specifications.for.all.target.groups.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.specifications.for.all.target.groups.are.added")
    }

private fun checkIfAtLeastOneStrategyIsAdded(projectStrategies: List<ProjectRelevanceStrategyData>?) =
    when {
        projectStrategies.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.strategy.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.at.least.one.strategy.is.added")
    }

private fun checkIfSpecificationIsProvidedForAllStrategies(projectStrategies: List<ProjectRelevanceStrategyData>?) =
    when {
        projectStrategies.isNullOrEmpty() -> null
        projectStrategies.any { it.specification.isNullOrEmptyOrMissingAnyTranslation() } ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.specifications.for.all.strategies.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.specifications.for.all.strategies.are.added")
    }

private fun checkIfSynergiesAreNotEmpty(relevanceSynergies: List<ProjectRelevanceSynergyData>?) =
    when {
        relevanceSynergies.isNullOrEmpty() -> null
        relevanceSynergies.any { it.synergy.isNullOrEmptyOrMissingAnyTranslation() } ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.synergies.should.be.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.synergies.are.provided")
    }

private fun checkIfAtLeastOneWorkPackageIsAdded(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.work.package.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.at.least.one.work.package.is.added")
    }

private fun checkIfNamesOfWorkPackagesAreProvided(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages.isNullOrEmpty() -> null
        workPackages.any { it.name.isNullOrEmptyOrMissingAnyTranslation() } ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.names.of.work.packages.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.names.of.work.packages.are.added")
    }

private fun checkIfAtLeastOneOutputForEachWorkPackageIsAdded(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages.isNullOrEmpty() -> null
        workPackages.any { it.outputs.isNullOrEmpty() } -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.output.for.each.work.package.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.at.least.one.output.for.each.work.package.is.added")
    }

private fun checkIfAtLeastOneResultIsAdded(results: List<ProjectResultData>?) =
    when {
        results.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.result.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.at.least.one.result.is.added")
    }

private fun checkIfFinancialManagementIsProvided(financialManagement: Set<InputTranslationData>?) =
    when {
        financialManagement.isNullOrEmptyOrMissingAnyTranslation() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.financial.management.should.be.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.financial.management.are.provided")
    }

private fun checkIfSelectedCooperationCriteriaAreValid(cooperationCriteria: ProjectCooperationCriteriaData?) =
    when {
        cooperationCriteria == null || !cooperationCriteria.projectJointDevelopment || !cooperationCriteria.projectJointImplementation ||
                !(cooperationCriteria.projectJointFinancing || cooperationCriteria.projectJointStaffing) ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.selected.cooperation.criteria.are.not.valid")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.selected.cooperation.criteria.are.valid")
    }

private fun checkIfDescriptionForAllSelectedCooperationCriteriaIsProvided(projectManagement: ProjectManagementData?) =
    when {
        projectManagement?.projectCooperationCriteria == null -> null
        isJointDevelopmentSelectedAndHasMissingTranslation(projectManagement) ||
                isJointImplementationSelectedAndHasMissingTranslation(projectManagement) ||
                isJointStaffingSelectedAndHasMissingTranslation(projectManagement) ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.description.for.selected.cooperation.criteria.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.description.for.selected.cooperation.criteria.is.provided")
    }

private fun checkIfTypeOfContributionsAreProvided(projectHorizontalPrinciples: ProjectHorizontalPrinciplesData?) =
    when {
        projectHorizontalPrinciples?.sustainableDevelopmentCriteriaEffect == null ||
                projectHorizontalPrinciples.equalOpportunitiesEffect == null ||
                projectHorizontalPrinciples.sexualEqualityEffect == null ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.type.of.contribution.in.horizontal.principles.are.not.valid")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.type.of.contribution.in.horizontal.principles.are.valid")
    }

private fun checkIfDescriptionForTypeOfContributionIsProvided(projectManagement: ProjectManagementData?) =
    when {
        isSustainableDevelopmentCriteriaEffectNotNeutralAndHasMissingTranslation(projectManagement) ||
                isEqualOpportunitiesEffectNotNeutralAndHasMissingTranslation(projectManagement) ||
                isSexualEqualityEffectNotNeutralAndHasMissingTranslation(projectManagement) ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.description.for.type.of.contribution.in.horizontal.principles.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.description.for.type.of.contribution.in.horizontal.principles.is.provided")
    }


private fun isSustainableDevelopmentCriteriaEffectNotNeutralAndHasMissingTranslation(projectManagement: ProjectManagementData?) =
    projectManagement?.projectHorizontalPrinciples?.sustainableDevelopmentCriteriaEffect != ProjectHorizontalPrinciplesEffectData.Neutral && projectManagement?.sustainableDevelopmentDescription.isNullOrEmptyOrMissingAnyTranslation()

private fun isEqualOpportunitiesEffectNotNeutralAndHasMissingTranslation(projectManagement: ProjectManagementData?) =
    projectManagement?.projectHorizontalPrinciples?.equalOpportunitiesEffect != ProjectHorizontalPrinciplesEffectData.Neutral && projectManagement?.equalOpportunitiesDescription.isNullOrEmptyOrMissingAnyTranslation()

private fun isSexualEqualityEffectNotNeutralAndHasMissingTranslation(projectManagement: ProjectManagementData?) =
    projectManagement?.projectHorizontalPrinciples?.sexualEqualityEffect != ProjectHorizontalPrinciplesEffectData.Neutral && projectManagement?.sexualEqualityDescription.isNullOrEmptyOrMissingAnyTranslation()


private fun isJointDevelopmentSelectedAndHasMissingTranslation(projectManagement: ProjectManagementData) =
    projectManagement.projectCooperationCriteria?.projectJointDevelopment == true && projectManagement.projectJointDevelopmentDescription.isNullOrEmptyOrMissingAnyTranslation()

private fun isJointImplementationSelectedAndHasMissingTranslation(projectManagement: ProjectManagementData) =
    projectManagement.projectCooperationCriteria?.projectJointImplementation == true && projectManagement.projectJointImplementationDescription.isNullOrEmptyOrMissingAnyTranslation()

private fun isJointStaffingSelectedAndHasMissingTranslation(projectManagement: ProjectManagementData) =
    projectManagement.projectCooperationCriteria?.projectJointStaffing == true && projectManagement.projectJointStaffingDescription.isNullOrEmptyOrMissingAnyTranslation()