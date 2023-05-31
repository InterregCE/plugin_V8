package io.cloudflight.jems.plugin.standard.programme_data_export.model.json_and_xml_export

import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.ProgrammeInfoData
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class JsonAndXmlExtractorModel(val contentType: KindOfExtractor) {
    enum class KindOfExtractor {
        JSON,
        XML
    }

    fun extract(
        myModel: MainModel,
        exportLanguage: SystemLanguageData,
        dataLanguage: SystemLanguageData,
        infoData: ProgrammeInfoData
    ): ExportResult {
        val startTime = ZonedDateTime.now()
        return when (contentType) {
            KindOfExtractor.JSON -> ExportResult(
                contentType = "application/json",
                fileName = getFileName(infoData.title, startTime, exportLanguage, dataLanguage),
                content = myModel.buildMyModel().extractToJsonString().toByteArray(Charsets.UTF_8),
                startTime = startTime,
                endTime = ZonedDateTime.now()
            )

            KindOfExtractor.XML -> ExportResult(
                contentType = "application/xml",
                fileName = getFileName(infoData.title, startTime, exportLanguage, dataLanguage),
                content = myModel.buildMyModel().extractToXmlString().toByteArray(Charsets.UTF_8),
                startTime = startTime,
                endTime = ZonedDateTime.now()
            )
        }
    }

    private fun MainModel.buildMyModel(): Model {
        val arrayListCall = ArrayList<Call>()
        val arrayListProject = ArrayList<Project>()
        this.basicModelList.distinctBy { it.buildOneCall().serialNumber }.forEach { modelInjected ->
            arrayListCall.add(modelInjected.buildOneCall())
        }
        this.basicModelList.forEach { modelInjected ->
            arrayListProject.add(modelInjected.buildOneProject())
        }
        return Model(this.buildOneProgram(arrayListCall.toList(), arrayListProject.toList()))
    }

    private fun getFileName(
        programmeTitle: String?,
        exportationDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ): String {
        val extension = if (contentType == KindOfExtractor.JSON) "json" else "xml"
        return "${
            if (programmeTitle.isNullOrBlank()) "programme"
            else
                programmeTitle
        }_project_${exportLanguage.name.lowercase()}_${dataLanguage.name.lowercase()}_" +
                "${exportationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.$extension"
    }
}
