package io.cloudflight.jems.plugin.standard.programme_data_export.model

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.versions.ProjectVersionData

open class ProjectAndCallData(
    val projectData: ProjectData,
    val callDetailData: CallDetailData,
    val projectVersion: ProjectVersionData
)
