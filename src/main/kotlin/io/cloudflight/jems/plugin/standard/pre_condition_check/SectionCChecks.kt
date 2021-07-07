package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.ProjectDataSectionC
import io.cloudflight.jems.plugin.contract.models.project.sectionC.longTermPlans.ProjectLongTermPlansData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectCooperationCriteriaData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectHorizontalPrinciplesData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectHorizontalPrinciplesEffectData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.management.ProjectManagementData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.overallObjective.ProjectOverallObjectiveData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.partnership.ProjectPartnershipData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceBenefitData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceStrategyData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceSynergyData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.results.ProjectResultData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.ProjectWorkPackageData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageActivityData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageInvestmentData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageOutputData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import java.math.BigDecimal

private const val SECTION_C_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.c"
private const val SECTION_C_ERROR_MESSAGES_PREFIX = "$SECTION_C_MESSAGES_PREFIX.error"
private const val SECTION_C_INFO_MESSAGES_PREFIX = "$SECTION_C_MESSAGES_PREFIX.info"

fun checkSectionC(
    sectionCData: ProjectDataSectionC?, lifecycleData: ProjectLifecycleData, callData: CallDetailData
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(
        messageKey = "$SECTION_C_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c1", messageArgs = emptyMap(),
            checkIfProjectOverallObjectiveIsProvided(sectionCData?.projectOverallObjective)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c2", messageArgs = emptyMap(),

            checkIfTerritorialChallengeGroupIsProvided(sectionCData?.projectRelevance?.territorialChallenge),

            checkIfCommonChallengeGroupIsProvided(sectionCData?.projectRelevance?.commonChallenge),

            checkIfTransnationalCooperationGroupIsProvided(sectionCData?.projectRelevance?.transnationalCooperation),

            checkIfAtLeastOneTargetGroupIsAdded(sectionCData?.projectRelevance?.projectBenefits),

            checkIfSpecificationIsProvidedForAllTargetGroups(sectionCData?.projectRelevance?.projectBenefits),

            checkIfAtLeastOneStrategyIsAdded(sectionCData?.projectRelevance?.projectStrategies),

            checkIfSpecificationIsProvidedForAllStrategies(sectionCData?.projectRelevance?.projectStrategies),

            checkIfSynergiesAreNotEmpty(sectionCData?.projectRelevance?.projectSynergies),

            checkIfAvailableKnowledgeAreNotEmpty(sectionCData?.projectRelevance?.availableKnowledge)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c3", messageArgs = emptyMap(),

            checkIfProjectPpartnershipIsAdded(sectionCData?.projectPartnership)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c4", messageArgs = emptyMap(),

            checkIfAtLeastOneWorkPackageIsAdded(sectionCData?.projectWorkPackages),

            checkIfNamesOfWorkPackagesAreProvided(sectionCData?.projectWorkPackages),

            checkIfObjectivesOfWorkPackagesAreProvided(sectionCData?.projectWorkPackages),

            checkIfAtLeastOneOutputForEachWorkPackageIsAdded(sectionCData?.projectWorkPackages),

            checkIfWorkPackageContentIsProvided(sectionCData?.projectWorkPackages)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c5", messageArgs = emptyMap(),
            checkIfAtLeastOneResultIsAdded(sectionCData?.projectResults),

            checkIfResultContentIsProvided(sectionCData?.projectResults, lifecycleData, callData)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c7", messageArgs = emptyMap(),

            checkIfCoordinateProjectIsValid(sectionCData?.projectManagement),

            checkIfMeasuresQualityIsValid(sectionCData?.projectManagement),

            checkIfCommunicationIsValid(sectionCData?.projectManagement),

            checkIfFinancialManagementIsProvided(sectionCData?.projectManagement?.projectFinancialManagement),

            checkIfSelectedCooperationCriteriaAreValid(sectionCData?.projectManagement?.projectCooperationCriteria),

            checkIfDescriptionForAllSelectedCooperationCriteriaIsProvided(sectionCData?.projectManagement),

            checkIfTypeOfContributionsAreProvided(sectionCData?.projectManagement?.projectHorizontalPrinciples),

            checkIfDescriptionForTypeOfContributionIsProvided(sectionCData?.projectManagement)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c8", messageArgs = emptyMap(),

            checkIfOwnershipIsValid(sectionCData?.projectLongTermPlans),

            checkIfDurabilityIsValid(sectionCData?.projectLongTermPlans),

            checkIfTransferabilityIsValid(sectionCData?.projectLongTermPlans)
        )
    )

private fun checkIfProjectOverallObjectiveIsProvided(projectOverallObjectiveData: ProjectOverallObjectiveData?) =
    when (projectOverallObjectiveData) {
        null -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.overall.objective.should.be.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.overall.objective.is.provided")
    }

private fun checkIfTerritorialChallengeGroupIsProvided(territorialChallenge: Set<InputTranslationData>?) =
    when {
        territorialChallenge.isNullOrEmptyOrMissingAnyTranslation() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.territorial.challenge.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.territorial.challenge.is.provided")
    }

private fun checkIfCommonChallengeGroupIsProvided(commonChallenge: Set<InputTranslationData>?) =
    when {
        commonChallenge.isNullOrEmptyOrMissingAnyTranslation() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.common.challenges.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.common.challenges.provided")
    }

private fun checkIfTransnationalCooperationGroupIsProvided(transnationalCooperation: Set<InputTranslationData>?) =
    when {
        transnationalCooperation.isNullOrEmptyOrMissingAnyTranslation() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.transnational.cooperation.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.transnational.cooperation.provided")
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
        relevanceSynergies.isNullOrEmpty() -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.synergies.are.not.added")
        relevanceSynergies.any { it.synergy.isNullOrEmptyOrMissingAnyTranslation() } ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.synergies.should.be.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.synergies.are.provided")
    }

private fun checkIfAvailableKnowledgeAreNotEmpty(availableKnowledge: Set<InputTranslationData>?) =
    when {
        availableKnowledge == null ||
        availableKnowledge.isNullOrEmptyOrMissingAnyTranslation() ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.build.available.knowledge.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.build.available.knowledge.is.provided")
    }

private fun checkIfWorkPackageContentIsProvided(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages != null &&
                workPackages.any { workPackage ->
                    isActivitiesContentMissing(workPackage.activities) ||
                    isOutputsContentMissing(workPackage.outputs)
                } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            workPackages.forEach { workPackage ->
                if (workPackage.activities.isEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.title.is.not.provided",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation()))
                        )
                    )
                }
                val errorActivitiesMessages = checkIfActivitiesAreValid(workPackage.activities)
                if (errorActivitiesMessages.isNotEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessages(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation())),
                            errorActivitiesMessages
                        )
                    )
                }
                val errorOutputMessages = checkIfOutputsAreValid(workPackage.outputs)
                if (errorOutputMessages.isNotEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessages(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation())),
                            errorOutputMessages
                        )
                    )
                }
                val errorInvestmentsMessages = checkIfInvestmentsAreValid(workPackage.investments)
                if (errorInvestmentsMessages.isNotEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessages(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation())),
                            errorInvestmentsMessages
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c4.content",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
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

private fun checkIfObjectivesOfWorkPackagesAreProvided(workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages.isNullOrEmpty() -> null
        workPackages.any { it.objectiveAndAudience.isNullOrEmptyOrMissingAnyTranslation() ||
                it.specificObjective.isNullOrEmptyOrMissingAnyTranslation() } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            workPackages.forEach { workPackage ->
                if (workPackage.objectiveAndAudience.isNullOrEmptyOrMissingAnyTranslation()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.audience.objective.is.not.provided",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation()))
                        )
                    )
                }
                if (workPackage.specificObjective.isNullOrEmptyOrMissingAnyTranslation()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.specific.objective.is.not.provided",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation()))
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages("$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.objectives",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
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

private fun checkIfResultContentIsProvided(results: List<ProjectResultData>?, lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
    when {
        results != null &&
        results.any { result ->
            result.programmeResultIndicatorId ?: 0 <= 0 ||
            result.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO ||
            result.periodNumber ?: 0 <= 0 ||
            result.translatedValues.isResultNullOrEmptyOrMissingAnyDescription()
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            results.forEach { result ->
                if (result.programmeResultIndicatorId ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.indicator.is.not.provided",
                            mapOf("name" to (result.programmeResultIndicatorIdentifier ?: result.resultNumber.toString()))
                        )
                    )
                }
                if (result.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.target.is.not.provided",
                            mapOf("name" to (result.programmeResultIndicatorIdentifier ?: result.resultNumber.toString()))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_DELIVERY_PERIOD, lifecycleData, callData) && result.periodNumber ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.delivery.is.not.provided",
                            mapOf("name" to (result.programmeResultIndicatorIdentifier ?: result.resultNumber.toString()))
                        )
                    )
                }
                if (result.translatedValues.isResultNullOrEmptyOrMissingAnyDescription()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.description.is.not.provided",
                            mapOf("name" to (result.programmeResultIndicatorIdentifier ?: result.resultNumber.toString()))
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c5.content",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
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

private fun checkIfCoordinateProjectIsValid(projectManagement: ProjectManagementData?) =
    when {
        projectManagement == null || projectManagement.projectCoordination.isNullOrEmptyOrMissingAnyTranslation()
                 ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c71.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.c71.is.provided")
    }

private fun checkIfMeasuresQualityIsValid(projectManagement: ProjectManagementData?) =
    when {
        projectManagement == null || projectManagement.projectQualityAssurance.isNullOrEmptyOrMissingAnyTranslation()
        ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c72.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.c72.is.provided")
    }

private fun checkIfCommunicationIsValid(projectManagement: ProjectManagementData?) =
    when {
        projectManagement == null || projectManagement.projectCommunication.isNullOrEmptyOrMissingAnyTranslation()
        ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c73.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.c73.is.provided")
    }

private fun checkIfOwnershipIsValid(projectLongTermPlans: ProjectLongTermPlansData?) =
    when {
        projectLongTermPlans == null || projectLongTermPlans.projectOwnership.isNullOrEmptyOrMissingAnyTranslation()
        ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c81.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.c81.is.provided")
    }

private fun checkIfDurabilityIsValid(projectLongTermPlans: ProjectLongTermPlansData?) =
    when {
        projectLongTermPlans == null || projectLongTermPlans.projectDurability.isNullOrEmptyOrMissingAnyTranslation()
        ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c82.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.c82.is.provided")
    }

private fun checkIfTransferabilityIsValid(projectLongTermPlans: ProjectLongTermPlansData?) =
    when {
        projectLongTermPlans == null || projectLongTermPlans.projectTransferability.isNullOrEmptyOrMissingAnyTranslation()
        ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c83.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.c83.is.provided")
    }

private fun checkIfActivitiesAreValid(activities: List<WorkPackageActivityData>): List<PreConditionCheckMessage> {
    val errorActivitiesMessages = mutableListOf<PreConditionCheckMessage>()
    if (activities.isNotEmpty()) {
        activities.forEach { activity ->
            if (activity.translatedValues.isActivityNullOrEmptyOrMissingAnyDescriptionOrTitle()) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.title.is.not.provided",
                        mapOf("id" to (activity.activityNumber.toString()))
                    )
                )
            }
            if (activity.startPeriod ?: 0 <= 0) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.start.period.is.not.provided",
                        mapOf("id" to (activity.activityNumber.toString()))
                    )
                )
            }
            if (activity.endPeriod ?: 0 <= 0) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.end.period.is.not.provided",
                        mapOf("id" to (activity.activityNumber.toString()))
                    )
                )
            }
            if (activity.deliverables.isEmpty()) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.deliverable.is.not.provided",
                        mapOf("id" to (activity.activityNumber.toString()))
                    )
                )
            }
        }
    } else {
        errorActivitiesMessages.add(
            buildErrorPreConditionCheckMessage(
                "$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.work.package.activity.should.be.added")
        )
    }
    return errorActivitiesMessages
}

private fun checkIfOutputsAreValid(outputs: List<WorkPackageOutputData>): List<PreConditionCheckMessage> {
    val errorOutputsMessages = mutableListOf<PreConditionCheckMessage>()
    if (outputs.isNotEmpty()) {
        outputs.forEach { output ->
            if (output.translatedValues.isOutputNullOrEmptyOrMissingAnyDescriptionOrTitle()) {
                errorOutputsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output.title.is.not.provided",
                        mapOf("id" to (output.outputNumber.toString()))
                    )
                )
            }
            if (output.programmeOutputIndicatorId ?: 0 <= 0) {
                errorOutputsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output.indicator.is.not.provided",
                        mapOf("id" to (output.outputNumber.toString()))
                    )
                )
            }
            if (output.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO) {
                errorOutputsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output.value.is.not.provided",
                        mapOf("id" to (output.outputNumber.toString()))
                    )
                )
            }
            if (output.periodNumber ?: 0 <= 0) {
                errorOutputsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output.delivery.period.is.not.provided",
                        mapOf("id" to (output.outputNumber.toString()))
                    )
                )
            }
        }
    } else {
        errorOutputsMessages.add(
            buildErrorPreConditionCheckMessage(
                "$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.work.package.output.should.be.added")
        )
    }
    return errorOutputsMessages
}

private fun checkIfInvestmentsAreValid(investments: List<WorkPackageInvestmentData>): List<PreConditionCheckMessage> {
    val errorInvestmentsMessages = mutableListOf<PreConditionCheckMessage>()
    if (investments.isNotEmpty()) {
        investments.forEach { investment ->
            if (investment.title.isNullOrEmptyOrMissingAnyTranslation()) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.title.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.justificationExplanation.isNullOrEmptyOrMissingAnyTranslation()) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.justification.explain.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.justificationBenefits.isNullOrEmptyOrMissingAnyTranslation()) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.benefiting.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.justificationTransactionalRelevance.isNullOrEmptyOrMissingAnyTranslation()) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.transnational.relevance.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.ownershipSiteLocation.isNullOrEmptyOrMissingAnyTranslation()) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.site.owner.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.ownershipRetain.isNullOrEmptyOrMissingAnyTranslation()) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.end.project.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.ownershipMaintenance.isNullOrEmptyOrMissingAnyTranslation()) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.maintenance.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.risk.isNullOrEmptyOrMissingAnyTranslation()) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.risk.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.documentation.isNullOrEmptyOrMissingAnyTranslation()) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.documentation.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
        }
    }
    return errorInvestmentsMessages
}

private fun checkIfProjectPpartnershipIsAdded(projectPartnership: ProjectPartnershipData?) =
    when {
        projectPartnership?.partnership == null ||
        projectPartnership.partnership.isNullOrEmptyOrMissingAnyTranslation() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.partnership.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.project.partnership.passed")
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

private fun isActivitiesContentMissing(activities: List<WorkPackageActivityData>) =
    activities.isEmpty() ||
        (activities.isNotEmpty() &&
            activities.any
            {
                activity -> activity.translatedValues.isActivityNullOrEmptyOrMissingAnyDescriptionOrTitle() ||
                activity.startPeriod ?: 0 <= 0 ||
                activity.endPeriod ?: 0 <= 0 ||
                    activity.deliverables.isEmpty()
            }
        )

private fun isOutputsContentMissing(outputs: List<WorkPackageOutputData>) =
    outputs.isEmpty() ||
        (outputs.isNotEmpty() &&
            outputs.any
            {
                output -> output.translatedValues.isOutputNullOrEmptyOrMissingAnyDescriptionOrTitle() ||
                output.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO ||
                output.periodNumber ?: 0 <= 0 ||
                output.programmeOutputIndicatorId ?: 0 <= 0
            }
        )

private fun isInvestmentsContentMissing(investments: List<WorkPackageInvestmentData>) =
    investments.isEmpty() ||
        (investments.isNotEmpty() &&
            investments.any
            {
                investment -> investment.title.isNullOrEmptyOrMissingAnyTranslation() ||
                investment.justificationExplanation.isNullOrEmptyOrMissingAnyTranslation() ||
                investment.justificationBenefits.isNullOrEmptyOrMissingAnyTranslation() ||
                investment.justificationTransactionalRelevance.isNullOrEmptyOrMissingAnyTranslation() ||
                investment.ownershipSiteLocation.isNullOrEmptyOrMissingAnyTranslation() ||
                investment.ownershipRetain.isNullOrEmptyOrMissingAnyTranslation() ||
                investment.ownershipMaintenance.isNullOrEmptyOrMissingAnyTranslation() ||
                investment.risk.isNullOrEmptyOrMissingAnyTranslation() ||
                investment.documentation.isNullOrEmptyOrMissingAnyTranslation()
            }
        )
