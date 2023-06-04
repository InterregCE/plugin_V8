package io.cloudflight.jems.plugin.standard.programme_data_export.programme_keep_data_export_xml

import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.ProgrammeDataExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProgrammeDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.contract.services.report.ReportPartnerDataProvider
import io.cloudflight.jems.plugin.standard.programme_data_export.model.json_and_xml_export.JsonAndXmlExtractorModel
import io.cloudflight.jems.plugin.standard.programme_data_export.model.json_and_xml_export.getProjectVersionsToExport
import io.cloudflight.jems.plugin.standard.programme_data_export.model.json_and_xml_export.toMainModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Suppress("UNUSED")
@Service
class ProgrammeKeepDataExportDefaultImplXml(
    private val programmeDataProvider: ProgrammeDataProvider,
    private val projectDataProvider: ProjectDataProvider,
    private val callDataProvider: CallDataProvider,
    private val reportPartnerDataProvider: ReportPartnerDataProvider,
) : ProgrammeDataExportPlugin {
    override fun getDescription(): String = "keep.eu implementation for programme data exportation xml format"
    override fun getKey() = "keep-programme-data-export-xml-plugin"
    override fun getName() = "keep.eu programme data export xml"
    override fun getVersion(): String = "1.0.1"
    override fun export(
        exportLanguage: SystemLanguageData,
        dataLanguage: SystemLanguageData
    ): ExportResult {
        val programmeData = programmeDataProvider.getProgrammeData()
        val mainModel = projectDataProvider
            .getAllProjectVersions()
            .getProjectVersionsToExport()
            .toMainModel(
                programmeDataProvider,
                projectDataProvider,
                callDataProvider,
                reportPartnerDataProvider
            )
        return JsonAndXmlExtractorModel(JsonAndXmlExtractorModel.KindOfExtractor.XML)
            .extract(mainModel, exportLanguage, dataLanguage, programmeData)
    }

    /*
    companion object {
        private val logger = LoggerFactory.getLogger(ProgrammeKeepDataExportDefaultImplXml::class.java)
    }*/
}