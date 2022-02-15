package io.cloudflight.jems.plugin.standard.programme_data_export.model

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.contract.models.project.versions.ProjectVersionData

data class ProjectAndCallAndPartnerData(
    val projectVersionData: ProjectVersionData,
    val projectData: ProjectData,
    val callDetailData: CallDetailData,
    val projectPartnerData: ProjectPartnerData
)
