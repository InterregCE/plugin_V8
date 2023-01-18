package io.cloudflight.jems.plugin.standard.common

import io.cloudflight.jems.plugin.contract.models.project.sectionB.associatedOrganisation.ProjectAssociatedOrganizationData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerContactData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectContactTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.PartnerSubTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.NaceGroupLevelData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectTargetGroupData

const val CLF_PARTNER_UTILS = "clfPartnerUtils"

class PartnerUtils {

    fun sortPartnersBySortNumber(partners: Set<ProjectPartnerData>) =
        partners.sortedBy { it.sortNumber }

    fun getLeadPartner(partners: Set<ProjectPartnerData>) = partners.firstOrNull { it.role.isLead }

    fun sortAssociatedOrganizationsBySortNumber(associatedOrganizations: Set<ProjectAssociatedOrganizationData>) =
        associatedOrganizations.sortedBy { it.sortNumber }

    fun getMainAddress(addresses: List<ProjectPartnerAddressData>) =
        addresses.firstOrNull { it.type == ProjectPartnerAddressTypeData.Organization }

    fun getLegalRepresentative(contacts: List<ProjectPartnerContactData>) =
        contacts.firstOrNull { it.type === ProjectContactTypeData.LegalRepresentative }

    fun getContactPerson(contacts: List<ProjectPartnerContactData>) =
        contacts.firstOrNull { it.type === ProjectContactTypeData.ContactPerson }

    fun getDepartmentAddress(addresses: List<ProjectPartnerAddressData>) =
        addresses.firstOrNull { it.type == ProjectPartnerAddressTypeData.Department }

    fun getPartnerTypeTranslationString(partnerType: ProjectTargetGroupData?): String {
        if (partnerType != null) {
            return "project.application.form.relevance.target.group.$partnerType"
        }
        return ""
    }

    fun getPartnerSubTypeTranslationString(partnerSubType: PartnerSubTypeData?): String {
        if (partnerSubType != null) {
            return "project.application.form.relevance.target.group.$partnerSubType"
        }
        return ""
    }

    fun getPartnerNaceTranslationString(nace: NaceGroupLevelData?): String {
        if (nace != null) {
            return nace.toString().replace('_', '.')
        }
        return ""
    }

    fun getPartnerListForActivity(activityPartnerIds:Set<Long>, partners:Set<ProjectPartnerData>): Set<ProjectPartnerData> =
        partners.filter { activityPartnerIds.contains(it.id) }.toSet()

    fun getPartnerSpfBeneficiaryTypeTranslationKey(partnerType: ProjectTargetGroupData?): String {
        val partnerTypeTranslationPrefix = "project.application.form.relevance.target.group"
        return when (partnerType) {
            ProjectTargetGroupData.Egtc -> "$partnerTypeTranslationPrefix.${ProjectTargetGroupData.Egtc.name}"
            ProjectTargetGroupData.CrossBorderLegalBody -> "$partnerTypeTranslationPrefix.${ProjectTargetGroupData.CrossBorderLegalBody.name}"
            else -> "spf.beneficiary.type.input.option"
        }
    }

    fun getPartnerNumber(partnerRole: ProjectPartnerRoleData, partnerSortNumber: Int): String =
        if (partnerRole.isLead) "LP${partnerSortNumber}" else "PP${partnerSortNumber}"
}