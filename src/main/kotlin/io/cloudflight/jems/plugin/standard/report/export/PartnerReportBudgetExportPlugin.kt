package io.cloudflight.jems.plugin.standard.report.export

import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.partner.report.PartnerReportExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.services.report.ReportPartnerDataProvider
import io.cloudflight.jems.plugin.standard.common.PartnerUtils
import io.cloudflight.jems.plugin.standard.common.excel.ExcelService
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.ExcelData
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import io.cloudflight.jems.plugin.standard.common.toLocale
import io.cloudflight.jems.plugin.standard.programme_data_export.programme_partner_data_export.toProjectCellData
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class PartnerReportBudgetExportPlugin(
    private val reportPartnerDataProvider: ReportPartnerDataProvider,
    private val messageSource: MessageSource,
    private val excelService: ExcelService,
): PartnerReportExportPlugin {

    override fun export(
        partnerId: Long,
        reportId: Long,
        exportLanguage: SystemLanguageData,
        dataLanguage: SystemLanguageData,
        logo: String?,
        localDateTime: LocalDateTime?
    ): ExportResult {
        val exportLocale = exportLanguage.toLocale()
        val partnerReportData = reportPartnerDataProvider.get(partnerId, reportId)
        val reportExpenditures = reportPartnerDataProvider.getExpenditureCosts(partnerId, reportId)

        val excelData = ExcelData()
        val sheet = excelData.addSheet(""" 
            List_of_expenditures_R${partnerReportData.reportNumber}_
            ${PartnerUtils().getPartnerNumber(partnerReportData.partnerRole, partnerReportData.partnerNumber)}_
            ${partnerReportData.projectIdentifier}
        """.trimIndent())

        sheet.addRow(
            CellData(
                getTitle(
                    partnerReportData.projectIdentifier,
                    partnerReportData.partnerAbbreviation,
                    partnerReportData.reportNumber,
                    localDateTime ?: LocalDateTime.now()
                )
            )
        )


        sheet.addRow(
            CellData(getMessage("project.application.partner.report.expenditures.tab.cost.ID", exportLocale, messageSource)),
            CellData(getMessage("project.application.partner.report.expenditures.tab.cost.category", exportLocale, messageSource)),
            CellData(getMessage("project.application.partner.report.expenditures.tab.internal.reference.number", exportLocale, messageSource)),
            CellData(getMessage("project.application.partner.report.expenditures.tab.total.value.invoice", exportLocale, messageSource)),
            CellData(getMessage("project.application.partner.report.expenditures.tab.description", exportLocale, messageSource)),
            CellData(getMessage("project.application.partner.report.expenditures.tab.comment", exportLocale, messageSource)),
            CellData(getMessage("project.application.partner.report.attachments", exportLocale, messageSource))
        )
        sheet.addRows(reportExpenditures.map { row ->
            mutableListOf<CellData>().also {
                it.add(row.id.toProjectCellData())
                it.add(
                    getMessage(
                        "project.application.partner.report.expenditures.cost.category.${row.costCategory}",
                        exportLocale,
                        messageSource
                    ).toProjectCellData()
                )
                it.add(row.internalReferenceNumber.toProjectCellData())
                it.add(row.totalValueInvoice.toProjectCellData())
                it.add(row.description.getTranslationFor(dataLanguage).toProjectCellData())
                it.add(row.comment.getTranslationFor(dataLanguage).toProjectCellData())
                it.add(row.attachment?.name.toProjectCellData())
            }.toTypedArray()
        })


        return ExportResult(
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            content =  excelService.generateExcel(excelData),
            fileName = getFileName(
                partnerReportData.projectIdentifier,
                partnerReportData.partnerRole,
                partnerReportData.partnerNumber,
                partnerReportData.reportNumber
            )
        )
    }


    override fun getDescription() = "Standard partner report (example) budget export plugin"
    override fun getName() = "Partner Report budget (Example) export"
    override fun getKey() = "standard-partner-report-export-budget-plugin"
    override fun getVersion() = "1.0.1"

    private fun getTitle(
        projectIdentifier: String?,
        partnerAbbreviation: String,
        reportNumber: Int,
        exportationDateTime: LocalDateTime
    ) =
        "$projectIdentifier - $partnerAbbreviation - R.${reportNumber} - ${
            exportationDateTime.format(
                DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss")
            )
        }"
    private fun getFileName(
        projectIdentifier: String?, partnerRole: ProjectPartnerRoleData, partnerNumber: Int, reportNumber: Int
    ): String =
        "Report_export_${projectIdentifier}_${PartnerUtils().getPartnerNumber(partnerRole, partnerNumber)}_R${reportNumber}.xlsx"
}