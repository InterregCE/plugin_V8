package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.ApplicationFormFieldConfigurationData
import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.call.CallStatusData
import io.cloudflight.jems.plugin.contract.models.call.FieldVisibilityStatusData
import io.cloudflight.jems.plugin.contract.models.call.flatrate.FlatRateSetupData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.priority.ProgrammePriorityDataSimple
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.ProjectDataSectionA
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA3.ProjectCoFinancingCategoryOverviewData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA3.ProjectCoFinancingOverviewData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA4.ProjectResultIndicatorOverview
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ChecksSectionA {

    companion object {
        val mandatoryLanguages = setOf(
            SystemLanguageData.EN,
            SystemLanguageData.DE,
            SystemLanguageData.RO)
        val onceStepCallData = CallDetailData(
            id = 0,
            name = "UT Call",
            status = CallStatusData.PUBLISHED,
            startDateTime = ZonedDateTime.of(2020, 12, 3, 12, 20, 59, 90000, ZoneId.systemDefault()),
            endDateTimeStep1 = null,
            endDateTime = ZonedDateTime.of(2027, 12, 3, 12, 20, 59, 90000, ZoneId.systemDefault()),
            isAdditionalFundAllowed = true,
            isDirectContributionsAllowed = false,
            lengthOfPeriod = 12,
            description = emptySet(),
            objectives  = emptyList(),
            strategies = emptyList(),
            funds = emptyList(),
            flatRates = FlatRateSetupData(null, null, null, null, null),
            lumpSums = emptyList(),
            unitCosts  = emptyList(),
            applicationFormFieldConfigurations =
                mutableSetOf(
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_TITLE.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_ACRONYM.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_DURATION.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_PRIORITY.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_OBJECTIVE.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_TITLE.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                    ApplicationFormFieldConfigurationData(ApplicationFormFieldId.PROJECT_SUMMARY.id, FieldVisibilityStatusData.STEP_ONE_AND_TWO),
                ),
            inputLanguages = mandatoryLanguages)
        val projectLifecycleData = ProjectLifecycleData(status = ApplicationStatusData.DRAFT)
        val coFinancingOverview = ProjectCoFinancingOverviewData(
            projectManagementCoFinancing = ProjectCoFinancingCategoryOverviewData(
                emptyList(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO),
            projectSpfCoFinancing = ProjectCoFinancingCategoryOverviewData()
        )
        val resultIndicatorOverview = ProjectResultIndicatorOverview(emptyList())
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro  = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
    }

    @Test
    fun `Project EN Intro Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
            {message ->
                message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.in.en.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Intro Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Title Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.title.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Acronym Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.acronym.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Duration Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.duration.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Priority Is not Provided`() {
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.messageType == MessageType.ERROR).isTrue
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.programme.priority.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Intro in English Is not Provided`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro  = setOf(
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
            ),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.in.en.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Intro in English Is empty`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro  = setOf(
                InputTranslationData(SystemLanguageData.EN, ""),
            ),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.in.en.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Intro in other language than English Is not fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro  = setOf(
                InputTranslationData(SystemLanguageData.DE, "Test data"),
            ),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Intro in other language than English Is not fully Provided due to empty text`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro  = setOf(
                InputTranslationData(SystemLanguageData.DE, ""),
                InputTranslationData(SystemLanguageData.RO, "test data")
            ),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Title Is not fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = setOf(
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
            ),
            intro = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.title.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project Title Is not fully Provided due to empty text`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = setOf(
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
                InputTranslationData(SystemLanguageData.EN, ""),
            ),
            intro = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.title.is.not.provided" }
        ).isTrue
    }

    @Test
    fun `Project EN Intro Is Provided`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro  = setOf(InputTranslationData(SystemLanguageData.EN, "TEST DATA")),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.in.en.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Intro Is fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro  = setOf(
                InputTranslationData(SystemLanguageData.EN, "TEST DATA"),
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
                InputTranslationData(SystemLanguageData.RO, "TEST DATA")
            ),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.intro.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Title Is fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = setOf(
                InputTranslationData(SystemLanguageData.EN, "TEST DATA"),
                InputTranslationData(SystemLanguageData.DE, "TEST DATA"),
                InputTranslationData(SystemLanguageData.RO, "TEST DATA")
            ),
            intro = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.title.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Acronym Is fully Provided`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro = emptySet(),
            acronym = "TEST DATA",
            duration = null,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.acronym.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Duration Is Provided`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro = emptySet(),
            acronym = null,
            duration = 12,
            specificObjective = null,
            programmePriority = null,
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.duration.is.not.provided" }
        ).isFalse
    }

    @Test
    fun `Project Priority Is Provided`() {
        val sectionAData = ProjectDataSectionA(
            customIdentifier = "BP4500492",
            title = emptySet(),
            intro = emptySet(),
            acronym = null,
            duration = null,
            specificObjective = null,
            programmePriority = ProgrammePriorityDataSimple(code = "TEST_DATA"),
            coFinancingOverview = coFinancingOverview,
            resultIndicatorOverview = resultIndicatorOverview
        )
        CallDataContainer.set(onceStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val verification = checkSectionA(sectionAData)
        assertThat(verification.subSectionMessages.any
        {message ->
            message.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.a.error.programme.priority.is.not.provided" }
        ).isFalse
    }
}