package io.cloudflight.jems.plugin.standard.common

import io.cloudflight.jems.plugin.contract.models.programme.fund.ProgrammeFundTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.ProjectPeriodData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA3.ProjectCoFinancingByFundOverviewData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA4.IndicatorOverviewLine
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerStateAidData

const val CLF_PROJECT_UTILS = "clfProjectUtils"
const val MAX_INDICATOR_ID_FROM_DB = 1_000_000_000_000
const val MAX_INDICATOR_FAKE_ID = 3_000_000_000_000

class ProjectUtils {

    fun getEuFundsOverview(fundOverviews: List<ProjectCoFinancingByFundOverviewData>) =
        fundOverviews.filter { it.fundType != ProgrammeFundTypeData.OTHER }
            .sortedWith(compareBy ({it.fundType?.name}, {it.fundId}))

    fun getOtherFundsOverview(fundOverviews: List<ProjectCoFinancingByFundOverviewData>) =
        fundOverviews.filter { it.fundType == ProgrammeFundTypeData.OTHER }.sortedBy { it.fundId }

    fun getLinesSorted(resultLines: List<IndicatorOverviewLine>) =
        transformIdsOfIndicatorsToRowIds(resultLines)
            .sortedWith(compareBy({it.resultIndicatorId}, {it.outputIndicatorId}))

    fun getResultIndicatorSpan(data: List<IndicatorOverviewLine>) =
        getRowSpanPlanForIndicatorOverviewLines(listOf("resultIndicatorId"), data)

    fun getOutputIndicatorSpan(data: List<IndicatorOverviewLine>) =
        getRowSpanPlanForIndicatorOverviewLines(listOf("resultIndicatorId","outputIndicatorId"), data)

    fun getStateAidCheckResultTranslationKey(stateAidData: ProjectPartnerStateAidData) =
        if (stateAidData.answer1 == null || stateAidData.answer2 == null || stateAidData.answer3 == null || stateAidData.answer4 == null)
            "project.partner.state.aid.complete.form.first"
        else if (stateAidData.answer1 == true && stateAidData.answer2 == true && stateAidData.answer3 == true && stateAidData.answer4 == true)
            "project.partner.state.aid.risk.of.state.aid"
        else if ((stateAidData.answer1 == false || stateAidData.answer2 == false || stateAidData.answer3 == false) && stateAidData.answer4 == true)
            "project.partner.state.aid.risk.of.indirect.aid"
        else
            "project.partner.state.aid.no.risk.of.state.aid"

    fun getPeriod(periodNumber: Number, periods: List<ProjectPeriodData>): ProjectPeriodData? =
        periods.filter { it.number == periodNumber }.firstOrNull()



    private fun transformIdsOfIndicatorsToRowIds(data: List<IndicatorOverviewLine>): List<IndicatorOverviewLine> =
        data.mapIndexed {
                index, indicatorOverviewLine ->
            generateIdsForNotExistingIds(indicatorOverviewLine, index) }

    private fun generateIdsForNotExistingIds(
        indicatorOverviewLine: IndicatorOverviewLine,
        index: Int
    ): IndicatorOverviewLine {
        if (indicatorOverviewLine.onlyResultWithoutOutputs) {
            return indicatorOverviewLine.copy(
                outputIndicatorId = MAX_INDICATOR_FAKE_ID + index,
                resultIndicatorId = MAX_INDICATOR_FAKE_ID + index,
            )
        }

        val outputIndicatorId = indicatorOverviewLine.outputIndicatorId ?: (MAX_INDICATOR_ID_FROM_DB + index)

        return indicatorOverviewLine.copy(
            outputIndicatorId = outputIndicatorId,
            resultIndicatorId = indicatorOverviewLine.resultIndicatorId ?: (MAX_INDICATOR_ID_FROM_DB + outputIndicatorId)
        )
    }

    private fun getRowSpanPlanForIndicatorOverviewLines(attributes: List<String>, data: List<IndicatorOverviewLine>): List<Long> {

        // if we do not go deeper, stop recursion and fill row-spans for this group
        if (attributes.isEmpty()) {
            return data.mapIndexed { index, _ -> if (index == 0) data.size.toLong() else 0}
        }

        val attributesToFollow = attributes.subList(1, attributes.size)
        val attribute = attributes.first()
        val uniqueRowsWithinGroup = data.associateBy { getValueForIndicatorIds(attribute, it) }.values

        return uniqueRowsWithinGroup
            .map {
                this.getRowSpanPlanForIndicatorOverviewLines(
                    attributesToFollow,
                    data.filter {
                            item -> this.getValueForIndicatorIds(attribute, it) == this.getValueForIndicatorIds(attribute, item)
                    })
            }
            .flatten()
    }

    private fun getValueForIndicatorIds(attribute: String, from: IndicatorOverviewLine): Long? =
        when (attribute) {
            "outputIndicatorId" ->
                from.outputIndicatorId;
            "resultIndicatorId" ->
                from.resultIndicatorId;
            else ->
                -1
        }

}