package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
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

private var _lifecycleData: ProjectLifecycleData? = null
private var _callData: CallDetailData? = null

fun checkSectionC(
    sectionCData: ProjectDataSectionC?, lifecycleData: ProjectLifecycleData, callData: CallDetailData
): PreConditionCheckMessage {
    _lifecycleData = lifecycleData
    _callData = callData

    return buildPreConditionCheckMessage(
        messageKey = "$SECTION_C_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c1", messageArgs = emptyMap(),
            checkIfProjectOverallObjectiveIsProvided(sectionCData?.projectOverallObjective)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c2", messageArgs = emptyMap(),

            checkIfTerritorialChallengeGroupIsProvided(callData.inputLanguages, sectionCData?.projectRelevance?.territorialChallenge),

            checkIfCommonChallengeGroupIsProvided(callData.inputLanguages, sectionCData?.projectRelevance?.commonChallenge),

            checkIfTransnationalCooperationGroupIsProvided(callData.inputLanguages, sectionCData?.projectRelevance?.transnationalCooperation),

            checkIfAtLeastOneTargetGroupIsAdded(sectionCData?.projectRelevance?.projectBenefits),

            checkIfSpecificationIsProvidedForAllTargetGroups(callData.inputLanguages, sectionCData?.projectRelevance?.projectBenefits),

            checkIfAtLeastOneStrategyIsAdded(sectionCData?.projectRelevance?.projectStrategies),

            checkIfSpecificationIsProvidedForAllStrategies(callData.inputLanguages, sectionCData?.projectRelevance?.projectStrategies),

            checkIfSynergiesAreNotEmpty(callData.inputLanguages, sectionCData?.projectRelevance?.projectSynergies),

            checkIfAvailableKnowledgeAreNotEmpty(callData.inputLanguages, sectionCData?.projectRelevance?.availableKnowledge)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c3", messageArgs = emptyMap(),

            checkIfProjectPpartnershipIsAdded(callData.inputLanguages, sectionCData?.projectPartnership)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c4", messageArgs = emptyMap(),

            checkIfAtLeastOneWorkPackageIsAdded(sectionCData?.projectWorkPackages),

            checkIfNamesOfWorkPackagesAreProvided(callData.inputLanguages, sectionCData?.projectWorkPackages),

            checkIfObjectivesOfWorkPackagesAreProvided(callData.inputLanguages, sectionCData?.projectWorkPackages),

            checkIfAtLeastOneOutputForEachWorkPackageIsAdded(sectionCData?.projectWorkPackages),

            checkIfWorkPackageContentIsProvided(callData.inputLanguages, sectionCData?.projectWorkPackages)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c5", messageArgs = emptyMap(),
            checkIfAtLeastOneResultIsAdded(sectionCData?.projectResults),

            checkIfResultContentIsProvided(callData.inputLanguages, sectionCData)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c7", messageArgs = emptyMap(),

            checkIfCoordinateProjectIsValid(callData.inputLanguages, sectionCData?.projectManagement),

            checkIfMeasuresQualityIsValid(callData.inputLanguages, sectionCData?.projectManagement),

            checkIfCommunicationIsValid(callData.inputLanguages, sectionCData?.projectManagement),

            checkIfFinancialManagementIsProvided(callData.inputLanguages, sectionCData?.projectManagement?.projectFinancialManagement),

            checkIfSelectedCooperationCriteriaAreValid(sectionCData?.projectManagement?.projectCooperationCriteria),

            checkIfDescriptionForAllSelectedCooperationCriteriaIsProvided(callData.inputLanguages, sectionCData?.projectManagement),

            checkIfTypeOfContributionsAreProvided(sectionCData?.projectManagement?.projectHorizontalPrinciples),

            checkIfDescriptionForTypeOfContributionIsProvided(callData.inputLanguages, sectionCData?.projectManagement)
        ),

        buildPreConditionCheckMessage(
            messageKey = "$SECTION_C_INFO_MESSAGES_PREFIX.project.c8", messageArgs = emptyMap(),

            checkIfOwnershipIsValid(callData.inputLanguages, sectionCData?.projectLongTermPlans),

            checkIfDurabilityIsValid(callData.inputLanguages, sectionCData?.projectLongTermPlans),

            checkIfTransferabilityIsValid(callData.inputLanguages, sectionCData?.projectLongTermPlans)
        )
    )
}

private fun checkIfProjectOverallObjectiveIsProvided(projectOverallObjectiveData: ProjectOverallObjectiveData?) =
    when (projectOverallObjectiveData) {
        null -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.overall.objective.should.be.provided")
        else -> null
    }

private fun checkIfTerritorialChallengeGroupIsProvided(mandatoryLanguages: Set<SystemLanguageData>, territorialChallenge: Set<InputTranslationData>?) =
    when {
        territorialChallenge.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.territorial.challenge.is.not.provided")
        else -> null
    }

private fun checkIfCommonChallengeGroupIsProvided(mandatoryLanguages: Set<SystemLanguageData>, commonChallenge: Set<InputTranslationData>?) =
    when {
        commonChallenge.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.common.challenges.is.not.provided")
        else -> null
    }

private fun checkIfTransnationalCooperationGroupIsProvided(mandatoryLanguages: Set<SystemLanguageData>, transnationalCooperation: Set<InputTranslationData>?) =
    when {
        transnationalCooperation.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.transnational.cooperation.is.not.provided")
        else -> null
    }

private fun checkIfAtLeastOneTargetGroupIsAdded(projectBenefits: List<ProjectRelevanceBenefitData>?) =
    when {
        projectBenefits.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.target.group.should.be.added")
        else -> null
    }

private fun checkIfSpecificationIsProvidedForAllTargetGroups(mandatoryLanguages: Set<SystemLanguageData>, projectBenefits: List<ProjectRelevanceBenefitData>?) =
    when {
        projectBenefits.isNullOrEmpty() -> null
        projectBenefits.any { it.specification.isNotFullyTranslated(mandatoryLanguages) }
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.specifications.for.all.target.groups.should.be.added")
        else -> null
    }

private fun checkIfAtLeastOneStrategyIsAdded(projectStrategies: List<ProjectRelevanceStrategyData>?) =
    when {
        projectStrategies.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.strategy.should.be.added")
        else -> null
    }

private fun checkIfSpecificationIsProvidedForAllStrategies(mandatoryLanguages: Set<SystemLanguageData>, projectStrategies: List<ProjectRelevanceStrategyData>?) =
    when {
        projectStrategies.isNullOrEmpty() -> null
        projectStrategies.any { it.specification.isNotFullyTranslated(mandatoryLanguages) }
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.specifications.for.all.strategies.should.be.added")
        else -> null
    }

private fun checkIfSynergiesAreNotEmpty(mandatoryLanguages: Set<SystemLanguageData>, relevanceSynergies: List<ProjectRelevanceSynergyData>?) =
    when {
        relevanceSynergies.isNullOrEmpty() -> buildInfoPreConditionCheckMessage("$SECTION_C_INFO_MESSAGES_PREFIX.synergies.are.not.added")
        relevanceSynergies.any { it.synergy.isNotFullyTranslated(mandatoryLanguages) }
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.synergies.should.be.provided")
        else -> null
    }

private fun checkIfAvailableKnowledgeAreNotEmpty(mandatoryLanguages: Set<SystemLanguageData>, availableKnowledge: Set<InputTranslationData>?) =
    when {
        availableKnowledge == null ||
        availableKnowledge.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.build.available.knowledge.is.not.provided")
        else -> null
    }

private fun checkIfWorkPackageContentIsProvided(mandatoryLanguages: Set<SystemLanguageData>, workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages != null &&
                workPackages.any { workPackage ->
                    isActivitiesContentMissing(mandatoryLanguages, workPackage.activities) ||
                    isOutputsContentMissing(mandatoryLanguages, workPackage.outputs)
                } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            workPackages.forEach { workPackage ->
                if (workPackage.activities.isEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.work.package.activity.should.be.added",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation()))
                        )
                    )
                }
                val errorActivitiesMessages = checkIfActivitiesAreValid(mandatoryLanguages, workPackage.activities)
                if (errorActivitiesMessages.isNotEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessages(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation())),
                            errorActivitiesMessages
                        )
                    )
                }
                val errorOutputMessages = checkIfOutputsAreValid(mandatoryLanguages, workPackage.outputs)
                if (errorOutputMessages.isNotEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessages(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.output",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation())),
                            errorOutputMessages
                        )
                    )
                }
                val errorInvestmentsMessages = checkIfInvestmentsAreValid(mandatoryLanguages, workPackage.investments)
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
        else -> null
    }

private fun checkIfNamesOfWorkPackagesAreProvided(mandatoryLanguages: Set<SystemLanguageData>, workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages.isNullOrEmpty() -> null
        workPackages.any { it.name.isNotFullyTranslated(mandatoryLanguages) }
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.names.of.work.packages.should.be.added")
        else -> null
    }

private fun checkIfObjectivesOfWorkPackagesAreProvided(mandatoryLanguages: Set<SystemLanguageData>, workPackages: List<ProjectWorkPackageData>?) =
    when {
        workPackages.isNullOrEmpty() -> null
        workPackages.any { it.objectiveAndAudience.isNotFullyTranslated(mandatoryLanguages) ||
                it.specificObjective.isNotFullyTranslated(mandatoryLanguages) } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            workPackages.forEach { workPackage ->
                if (workPackage.objectiveAndAudience.isNotFullyTranslated(mandatoryLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.audience.objective.is.not.provided",
                            mapOf("id" to (workPackage.name.getFirstOrDefaultTranslation()))
                        )
                    )
                }
                if (workPackage.specificObjective.isNotFullyTranslated(mandatoryLanguages)) {
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
        else -> null
    }

private fun checkIfAtLeastOneResultIsAdded(results: List<ProjectResultData>?) =
    when {
        results.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.at.least.one.result.should.be.added")
        else -> null
    }

private fun checkIfResultContentIsProvided(mandatoryLanguages: Set<SystemLanguageData>, projectData: ProjectDataSectionC?) =
    when {
        projectData?.projectResults != null &&
            projectData.projectResults.any { result ->
            result.programmeResultIndicatorId ?: 0 <= 0 ||
            result.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO ||
            result.periodNumber ?: 0 <= 0 ||
            result.translatedValues.isResultNullOrEmptyOrMissingAnyDescription()
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            projectData.projectResults.forEach { result ->
                if (result.programmeResultIndicatorId ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.indicator.is.not.provided",
                            mapOf("name" to (result.resultNumber.toString()))
                        )
                    )
                }
                if (result.targetValue ?: BigDecimal.ZERO <= BigDecimal.ZERO) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.target.is.not.provided",
                            mapOf("name" to (result.resultNumber.toString()))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PROJECT_RESULTS_DELIVERY_PERIOD) && result.periodNumber ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.delivery.is.not.provided",
                            mapOf("name" to (result.resultNumber.toString()))
                        )
                    )
                }
                var numberOfLanguages = projectData.projectOverallObjective?.overallObjective?.count() ?: 0
                if (result.translatedValues.count() > numberOfLanguages || result.translatedValues.isResultNullOrEmptyOrMissingAnyDescription()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_C_ERROR_MESSAGES_PREFIX.project.result.description.is.not.provided",
                            mapOf("name" to (result.resultNumber.toString()))
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

private fun checkIfFinancialManagementIsProvided(mandatoryLanguages: Set<SystemLanguageData>, financialManagement: Set<InputTranslationData>?) =
    when {
        financialManagement.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.financial.management.should.be.provided")
        else -> null
    }

private fun checkIfSelectedCooperationCriteriaAreValid(cooperationCriteria: ProjectCooperationCriteriaData?) =
    when {
        cooperationCriteria == null || !cooperationCriteria.projectJointDevelopment || !cooperationCriteria.projectJointImplementation ||
                !(cooperationCriteria.projectJointFinancing || cooperationCriteria.projectJointStaffing) ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.selected.cooperation.criteria.are.not.valid")
        else -> null
    }

private fun checkIfDescriptionForAllSelectedCooperationCriteriaIsProvided(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData?) =
    when {
        projectManagement?.projectCooperationCriteria == null -> null
        isJointDevelopmentSelectedAndHasMissingTranslation(mandatoryLanguages, projectManagement) ||
                isJointImplementationSelectedAndHasMissingTranslation(mandatoryLanguages, projectManagement) ||
                isJointStaffingSelectedAndHasMissingTranslation(mandatoryLanguages, projectManagement) ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.description.for.selected.cooperation.criteria.is.not.provided")
        else -> null
    }

private fun checkIfTypeOfContributionsAreProvided(projectHorizontalPrinciples: ProjectHorizontalPrinciplesData?) =
    when {
        projectHorizontalPrinciples?.sustainableDevelopmentCriteriaEffect == null ||
                projectHorizontalPrinciples.equalOpportunitiesEffect == null ||
                projectHorizontalPrinciples.sexualEqualityEffect == null ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.type.of.contribution.in.horizontal.principles.are.not.valid")
        else -> null
    }

private fun checkIfDescriptionForTypeOfContributionIsProvided(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData?) =
    when {
        isSustainableDevelopmentCriteriaEffectNotNeutralAndHasMissingTranslation(mandatoryLanguages, projectManagement) ||
                isEqualOpportunitiesEffectNotNeutralAndHasMissingTranslation(mandatoryLanguages, projectManagement) ||
                isSexualEqualityEffectNotNeutralAndHasMissingTranslation(mandatoryLanguages, projectManagement) ->
            buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.description.for.type.of.contribution.in.horizontal.principles.is.not.provided")
        else -> null
    }

private fun checkIfCoordinateProjectIsValid(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData?) =
    when {
        projectManagement == null || projectManagement.projectCoordination.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c71.is.not.provided")
        else -> null
    }

private fun checkIfMeasuresQualityIsValid(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData?) =
    when {
        projectManagement == null || projectManagement.projectQualityAssurance.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c72.is.not.provided")
        else -> null
    }

private fun checkIfCommunicationIsValid(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData?) =
    when {
        projectManagement == null || projectManagement.projectCommunication.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c73.is.not.provided")
        else -> null
    }

private fun checkIfOwnershipIsValid(mandatoryLanguages: Set<SystemLanguageData>, projectLongTermPlans: ProjectLongTermPlansData?) =
    when {
        projectLongTermPlans == null || projectLongTermPlans.projectOwnership.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c81.is.not.provided")
        else -> null
    }

private fun checkIfDurabilityIsValid(mandatoryLanguages: Set<SystemLanguageData>, projectLongTermPlans: ProjectLongTermPlansData?) =
    when {
        projectLongTermPlans == null || projectLongTermPlans.projectDurability.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c82.is.not.provided")
        else -> null
    }

private fun checkIfTransferabilityIsValid(mandatoryLanguages: Set<SystemLanguageData>, projectLongTermPlans: ProjectLongTermPlansData?) =
    when {
        projectLongTermPlans == null || projectLongTermPlans.projectTransferability.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.c83.is.not.provided")
        else -> null
    }

private fun checkIfActivitiesAreValid(mandatoryLanguages: Set<SystemLanguageData>, activities: List<WorkPackageActivityData>): List<PreConditionCheckMessage> {
    val errorActivitiesMessages = mutableListOf<PreConditionCheckMessage>()
    if (activities.isNotEmpty()) {
        activities.forEach { activity ->
            if (activity.translatedValues.isActivityNullOrEmptyOrMissingAnyDescriptionOrTitle()) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.title.or.description.is.not.provided",
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
            if (activity.deliverables.any {deliverable -> deliverable.period == null || deliverable.translatedValues.isDeliverableNullOrEmptyOrMissingAnyDescriptionOrTitle() }) {
                errorActivitiesMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.activity.delivery.period.or.description.is.not.provided",
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

private fun checkIfOutputsAreValid(mandatoryLanguages: Set<SystemLanguageData>, outputs: List<WorkPackageOutputData>): List<PreConditionCheckMessage> {
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

private fun checkIfInvestmentsAreValid(mandatoryLanguages: Set<SystemLanguageData>, investments: List<WorkPackageInvestmentData>): List<PreConditionCheckMessage> {
    val errorInvestmentsMessages = mutableListOf<PreConditionCheckMessage>()
    if (investments.isNotEmpty()) {
        investments.forEach { investment ->
            if (investment.title.isNotFullyTranslated(mandatoryLanguages)) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.title.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.justificationExplanation.isNotFullyTranslated(mandatoryLanguages)) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.justification.explain.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.justificationBenefits.isNotFullyTranslated(mandatoryLanguages)) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.benefiting.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.justificationTransactionalRelevance.isNotFullyTranslated(mandatoryLanguages)) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.transnational.relevance.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.ownershipSiteLocation.isNotFullyTranslated(mandatoryLanguages)) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.site.owner.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.ownershipRetain.isNotFullyTranslated(mandatoryLanguages)) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.end.project.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.ownershipMaintenance.isNotFullyTranslated(mandatoryLanguages)) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.maintenance.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.risk.isNotFullyTranslated(mandatoryLanguages)) {
                errorInvestmentsMessages.add(
                    buildErrorPreConditionCheckMessage(
                        "$SECTION_C_ERROR_MESSAGES_PREFIX.project.work.package.investments.risk.is.not.provided",
                        mapOf("id" to (investment.investmentNumber.toString()))
                    )
                )
            }
            if (investment.documentation.isNotFullyTranslated(mandatoryLanguages)) {
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

private fun checkIfProjectPpartnershipIsAdded(mandatoryLanguages: Set<SystemLanguageData>, projectPartnership: ProjectPartnershipData?) =
    when {
        projectPartnership?.partnership == null ||
        projectPartnership.partnership.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_C_ERROR_MESSAGES_PREFIX.project.partnership.is.not.provided")
        else -> null
    }

private fun isSustainableDevelopmentCriteriaEffectNotNeutralAndHasMissingTranslation(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData?) =
    projectManagement?.projectHorizontalPrinciples?.sustainableDevelopmentCriteriaEffect != ProjectHorizontalPrinciplesEffectData.Neutral
            && projectManagement?.sustainableDevelopmentDescription.isNotFullyTranslated(mandatoryLanguages)

private fun isEqualOpportunitiesEffectNotNeutralAndHasMissingTranslation(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData?) =
    projectManagement?.projectHorizontalPrinciples?.equalOpportunitiesEffect != ProjectHorizontalPrinciplesEffectData.Neutral
            && projectManagement?.equalOpportunitiesDescription.isNotFullyTranslated(mandatoryLanguages)

private fun isSexualEqualityEffectNotNeutralAndHasMissingTranslation(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData?) =
    projectManagement?.projectHorizontalPrinciples?.sexualEqualityEffect != ProjectHorizontalPrinciplesEffectData.Neutral
            && projectManagement?.sexualEqualityDescription.isNotFullyTranslated(mandatoryLanguages)

private fun isJointDevelopmentSelectedAndHasMissingTranslation(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData) =
    projectManagement.projectCooperationCriteria?.projectJointDevelopment == true
            && projectManagement.projectJointDevelopmentDescription.isNotFullyTranslated(mandatoryLanguages)

private fun isJointImplementationSelectedAndHasMissingTranslation(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData) =
    projectManagement.projectCooperationCriteria?.projectJointImplementation == true
            && projectManagement.projectJointImplementationDescription.isNotFullyTranslated(mandatoryLanguages)

private fun isJointStaffingSelectedAndHasMissingTranslation(mandatoryLanguages: Set<SystemLanguageData>, projectManagement: ProjectManagementData) =
    projectManagement.projectCooperationCriteria?.projectJointStaffing == true
            && projectManagement.projectJointStaffingDescription.isNotFullyTranslated(mandatoryLanguages)

private fun isActivitiesContentMissing(mandatoryLanguages: Set<SystemLanguageData>, activities: List<WorkPackageActivityData>) =
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

private fun isOutputsContentMissing(mandatoryLanguages: Set<SystemLanguageData>, outputs: List<WorkPackageOutputData>) =
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

private fun isInvestmentsContentMissing(mandatoryLanguages: Set<SystemLanguageData>, investments: List<WorkPackageInvestmentData>) =
    investments.isEmpty() ||
        (investments.isNotEmpty() &&
            investments.any
            {
                investment -> investment.title.isNotFullyTranslated(mandatoryLanguages) ||
                investment.justificationExplanation.isNotFullyTranslated(mandatoryLanguages) ||
                investment.justificationBenefits.isNotFullyTranslated(mandatoryLanguages) ||
                investment.justificationTransactionalRelevance.isNotFullyTranslated(mandatoryLanguages) ||
                investment.ownershipSiteLocation.isNotFullyTranslated(mandatoryLanguages) ||
                investment.ownershipRetain.isNotFullyTranslated(mandatoryLanguages) ||
                investment.ownershipMaintenance.isNotFullyTranslated(mandatoryLanguages) ||
                investment.risk.isNotFullyTranslated(mandatoryLanguages) ||
                investment.documentation.isNotFullyTranslated(mandatoryLanguages)
            }
        )

private fun isFieldVisible(fieldId: ApplicationFormFieldId): Boolean {
    return isFieldVisible(fieldId, _lifecycleData!!, _callData!!)
}
