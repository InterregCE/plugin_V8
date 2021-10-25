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

fun getTitle(projectData: ProjectData, version: String?) =
    (version ?: (projectData.versions.maxByOrNull { it.createdAt }?.version?.toFloatOrNull()?.plus(1)
        ?: 1.0).toString()).let { versionToShow ->
        "${projectData.sectionA?.customIdentifier} - ${projectData.sectionA?.acronym} - V$versionToShow - ${
            ZonedDateTime.now().format(DateTimeFormatter.ofPattern("y.d.M - H:m:ss"))
        }"
    }

fun getPartnerHeaders(
    isNameInOriginalLanguageVisible: Boolean, isNameInEnglishVisible: Boolean, isCountryAndNutsVisible: Boolean,
    messageSource: MessageSource, exportLocale: Locale
) =
    mutableListOf<String>().also {
        it.addAll(
            getMessagesWithoutArgs(
                messageSource, exportLocale, "jems.standard.budget.export.partner.number",
                "jems.standard.budget.export.partner.abbreviation"
            )
        )
        if (isNameInOriginalLanguageVisible)
            it.add(
                getMessage(
                    "jems.standard.budget.export.partner.original.language",
                    exportLocale,
                    messageSource
                )
            )
        if (isNameInEnglishVisible)
            it.add(
                getMessage(
                    "jems.standard.budget.export.partner.name.in.english",
                    exportLocale,
                    messageSource
                )
            )
        if (isCountryAndNutsVisible)
            it.addAll(
                getMessagesWithoutArgs(
                    messageSource, exportLocale,
                    "jems.standard.budget.export.partner.country",
                    "jems.standard.budget.export.partner.nuts.3",
                    "jems.standard.budget.export.partner.nuts.2",
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