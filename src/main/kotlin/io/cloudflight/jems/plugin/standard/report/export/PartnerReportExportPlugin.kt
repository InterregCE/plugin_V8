package io.cloudflight.jems.plugin.standard.report.export

import io.cloudflight.jems.plugin.config.PLUGIN_DEFAULT_TEMPLATE_ENGINE
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.partner.report.PartnerReportExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.services.report.ReportPartnerDataProvider
import io.cloudflight.jems.plugin.standard.common.toLocale
import io.cloudflight.jems.plugin.standard.common.DATA_LANGUAGE
import io.cloudflight.jems.plugin.standard.common.PartnerUtils
import io.cloudflight.jems.plugin.standard.common.pdf.PdfService
import io.cloudflight.jems.plugin.standard.common.template.CLF_UTILS
import io.cloudflight.jems.plugin.standard.common.template.TemplateUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime

@Service
class PartnerReportExportPlugin(
    private val reportPartnerDataProvider: ReportPartnerDataProvider,
    private val pdfService: PdfService,
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE)
    val templateEngine: ITemplateEngine
): PartnerReportExportPlugin {
    companion object {
        const val PARTNER_REPORT_DATA = "partnerReportData"
        const val PROGRAMME_LOGO = "logo"
    }
    override fun export(
        partnerId: Long,
        reportId: Long,
        exportLanguage: SystemLanguageData,
        dataLanguage: SystemLanguageData,
        logo: String?,
        localDateTime: LocalDateTime?
    ): ExportResult {
        val partnerReportData = reportPartnerDataProvider.get(partnerId, reportId)

        return ExportResult(
            contentType = MediaType.APPLICATION_PDF_VALUE,
            content =  pdfService.generatePdfFromHtml(
                templateEngine.process(
                    "partner-report/partner-report-template",
                    Context().also {
                        it.locale = exportLanguage.toLocale()
                        it.setVariable(DATA_LANGUAGE, dataLanguage)
                        it.setVariable(PARTNER_REPORT_DATA, partnerReportData)
                        it.setVariable(CLF_UTILS, TemplateUtils())
                        it.setVariable(PROGRAMME_LOGO, logo)
                    }
                )
            ),
            fileName = getFileName(
                partnerReportData.projectIdentifier,
                partnerReportData.partnerRole,
                partnerReportData.partnerNumber,
                partnerReportData.reportNumber
            )
        )
    }
    override fun getDescription() = "Standard partner report (example) export plugin"
    override fun getName() = "Partner Report (Example) export"
    override fun getKey() = "standard-partner-report-export-plugin"
    override fun getVersion() = "1.0.0"

    private fun getFileName(
        projectIdentifier: String?, partnerRole: ProjectPartnerRoleData, partnerNumber: Int, reportNumber: Int
    ): String =
        "Report_export_${projectIdentifier}_${PartnerUtils().getPartnerNumber(partnerRole, partnerNumber)}_R${reportNumber}.pdf"



}