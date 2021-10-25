package io.cloudflight.jems.plugin.standard.budget_export.models

data class PartnerInfo(
    val partnerNumber: String?,
    val partnerAbbreviation: String?,
    val partnerNameInOriginalLanguage: String?,
    val partnerNameInEnglish: String?,
    val partnerCountry: String?,
    val partnerNuts3: String?,
    val partnerNuts2: String?
) {
    fun toStringList(
        isNameInOriginalLanguageVisible: Boolean, isNameInEnglishVisible: Boolean, isCountryAndNutsVisible: Boolean
    ) =
        mutableListOf<String>().also {
            it.add(partnerNumber ?: "")
            it.add(partnerAbbreviation ?: "")
            if (isNameInOriginalLanguageVisible)
                it.add(partnerNameInOriginalLanguage ?: "")
            if (isNameInEnglishVisible)
                it.add(partnerNameInEnglish ?: "")
            if (isCountryAndNutsVisible)
                it.addAll(listOf(partnerCountry ?: "", partnerNuts3 ?: "", partnerNuts2 ?: ""))
        }
}