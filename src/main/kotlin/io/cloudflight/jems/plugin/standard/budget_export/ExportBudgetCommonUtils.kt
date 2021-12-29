package io.cloudflight.jems.plugin.standard.budget_export

import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.standard.budget_export.models.PartnerInfo
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getMessagesWithoutArgs
import org.springframework.context.MessageSource
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val FALL_BACK_LANGUAGE = SystemLanguageData.EN
const val PREPARATION_PERIOD = 0
const val CLOSURE_PERIOD = 255

fun getTitle(projectData: ProjectData, version: String?, exportationDateTime: ZonedDateTime) =
    (version ?: (projectData.versions.maxByOrNull { it.createdAt }?.version?.toFloatOrNull()?.plus(1)
        ?: 1.0).toString()).let { versionToShow ->
        "${projectData.sectionA?.customIdentifier} - ${projectData.sectionA?.acronym} - V$versionToShow - ${
            exportationDateTime.format(
                DateTimeFormatter.ofPattern("y.M.d - H:m:ss")
            )
        }"
    }

fun getFileName(projectAcronym: String?, projectCustomIdentifier: String?, exportationDateTime: ZonedDateTime): String =
    "${projectCustomIdentifier}_${projectAcronym}_Budget_${exportationDateTime.format(DateTimeFormatter.ofPattern("yyMd_Hmss"))}.csv"

fun getPartnerHeaders(
    isNameInOriginalLanguageVisible: Boolean, isNameInEnglishVisible: Boolean, isCountryAndNutsVisible: Boolean,
    messageSource: MessageSource, exportLocale: Locale
) =
    mutableListOf<String>().also {
        it.addAll(
            getMessagesWithoutArgs(
                messageSource, exportLocale, "project.application.form.partner.table.number",
                "project.application.form.partner.table.name"
            )
        )
        if (isNameInOriginalLanguageVisible)
            it.add(
                getMessage("project.organization.original.name.label", exportLocale, messageSource)
            )
        if (isNameInEnglishVisible)
            it.add(
                getMessage("project.organization.english.name.label", exportLocale, messageSource)
            )
        if (isCountryAndNutsVisible)
            it.addAll(
                getMessagesWithoutArgs(
                    messageSource, exportLocale,
                    "project.partner.main-address.country",
                    "project.partner.main-address.region3",
                    "project.partner.main-address.region2",
                )
            )
    }

fun getPartnerInfo(partner: ProjectPartnerData): PartnerInfo =
    partner.addresses.firstOrNull { it.type === ProjectPartnerAddressTypeData.Organization }.let { address ->
        PartnerInfo(
            if (partner.role.isLead) "LP${partner.sortNumber.toString()}" else "PP${partner.sortNumber.toString()}",
            partner.abbreviation,
            partner.nameInOriginalLanguage,
            partner.nameInEnglish,
            address?.country, address?.nutsRegion3, address?.nutsRegion2
        )
    }