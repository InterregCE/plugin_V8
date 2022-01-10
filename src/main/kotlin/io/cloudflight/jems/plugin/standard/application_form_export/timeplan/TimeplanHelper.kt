package io.cloudflight.jems.plugin.standard.application_form_export.timeplan

import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.ProjectPeriodData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.results.ProjectResultData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.ProjectWorkPackageData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageActivityData
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import kotlin.text.Typography.nbsp

private const val TABLE_WIDTH = 1000
private const val LEFT_BAR_WIDTH = 200
private const val DATA_SPACE_WIDTH = TABLE_WIDTH - LEFT_BAR_WIDTH
private const val LINE_HEIGHT = 25
private const val LEFT_TEXT_MARGIN = 5
private const val LEFT_TEXT_MAX_LENGTH = 45
private const val RECT_BIG_MARGIN = 3
private const val RECT_SMALL_MARGIN = 4
private const val TEXT_HEIGHT_COMPENSATOR = 16

fun getTimeplanData(
    periods: List<ProjectPeriodData>,
    workPackages: List<ProjectWorkPackageData>,
    results: List<ProjectResultData>,
    language: SystemLanguageData,
): TimeplanData? {
    if (periods.isEmpty() || (workPackages.isEmpty() && results.isEmpty()))
        return null

    val columnWidth = DATA_SPACE_WIDTH / periods.size.toFloat()

    var currentTableHeight = LINE_HEIGHT
    val rowLines = mutableListOf<String>()
    val titles = mutableListOf<TranslatedText>()
    // bigger rectangles needs to be generated first to not hide wrapped smaller ones
    val rectanglesBig = mutableListOf<Rectangle>()
    val rectanglesSmall = mutableListOf<Rectangle>()

    // initialize table with header
    rowLines.addLineAt(currentTableHeight = 0, lineHeight = LINE_HEIGHT)

    workPackages.forEachIndexed { wpIndex, wp ->
        val allUsedPeriods = wp.activities.map { it.getAllUsedPeriods() }.flatten().toSet()
            .plus(wp.outputs.mapNotNull { it.periodNumber })

        // generate first work package line itself (wrapper for all activities)
        rowLines.addLineAt(currentTableHeight = currentTableHeight, lineHeight = LINE_HEIGHT)
        rectanglesBig.addWorkPackageOverallRectangleIfNeeded(
            startPeriod = allUsedPeriods.minOrNull(),
            endPeriod = allUsedPeriods.maxOrNull(),
            columnWidth, currentTableHeight, colorIndex = wpIndex,
        )
        titles.addMajorTitle(y = currentTableHeight, title = "WP${wp.workPackageNumber} ${wp.name.getTranslationFor(language, false)}")
        currentTableHeight += LINE_HEIGHT

        // activities
        wp.activities.forEach { activity ->
            val deliverables = activity.deliverables.groupBy { it.period }.filter { it.key != null }
            val maxAmountOfDeliverablesInPeriod = deliverables.values.maxOfOrNull { it.size } ?: 0
            val activityLineHeight = maxOf(LINE_HEIGHT, maxAmountOfDeliverablesInPeriod * LINE_HEIGHT)

            // add line for activity
            rowLines.addLineAt(currentTableHeight = currentTableHeight, lineHeight = activityLineHeight)

            // add activity
            if (activity.startPeriod != null && activity.endPeriod != null) {
                rectanglesBig.add(
                    getRectangle(
                        startPeriod = activity.startPeriod!!,
                        spanPeriods = activity.endPeriod!! - activity.startPeriod!! + 1,
                        spanRows = maxOf(1, maxAmountOfDeliverablesInPeriod),
                        columnWidth = columnWidth,
                        currentTableHeight = currentTableHeight,
                        colorIndex = wpIndex,
                    )
                )
            }

            titles.addMinorTitle(y = currentTableHeight, title = "A${wp.workPackageNumber}.${activity.activityNumber} ${activity.title.getTranslationFor(language, false)}")

            // add deliverables for this particular activity
            deliverables.forEach { (_, deliverablesWithinPeriod) ->
                rectanglesSmall.addAll(
                    deliverablesWithinPeriod.mapIndexed { index, it -> getRectangle(
                        startPeriod = it.period!!,
                        columnWidth = columnWidth,
                        isSmall = true,
                        currentTableHeight = currentTableHeight,
                        index = index,
                        colorIndex = wpIndex,
                        text = "D${wp.workPackageNumber}.${activity.activityNumber}.${it.deliverableNumber}",
                    ) }
                )
            }
            currentTableHeight += activityLineHeight
        }

        val outputsByIndicator = wp.outputs.groupBy { it.programmeOutputIndicatorIdentifier }
            .toSortedMap(Comparator.nullsLast(Comparator.naturalOrder<String>()))

        outputsByIndicator.forEach { (indicatorTitle, outputs) ->
            val outputsByPeriod = outputs.groupBy { it.periodNumber }.filter { it.key != null }
            val outputsHeight = (outputsByPeriod.values.maxOfOrNull { it.size } ?: 0) * LINE_HEIGHT
            if (outputsHeight == 0)
                return@forEach

            // add line for output indicator
            rowLines.addLineAt(currentTableHeight = currentTableHeight, lineHeight = outputsHeight)
            titles.addMinorTitle(y = currentTableHeight, title = indicatorTitle)

            outputsByPeriod.forEach { (_, outputsWithinPeriod) ->
                outputsWithinPeriod.forEachIndexed { index, it ->
                    rectanglesSmall.add(
                        getRectangle(
                            startPeriod = it.periodNumber!!,
                            columnWidth = columnWidth,
                            isSmall = true,
                            currentTableHeight = currentTableHeight,
                            index = index,
                            colorIndex = wpIndex,
                            text = "O${wp.workPackageNumber}.${it.outputNumber}",
                        )
                    )
                }
            }
            currentTableHeight += outputsHeight
        }
    }

    var resultTitleYCoordinate: Int? = null

    val resultsByIndicator = results.filter { it.periodNumber != null }
        .groupBy { it.programmeResultIndicatorIdentifier }
        .toSortedMap(Comparator.nullsLast(Comparator.naturalOrder<String>()))
    if (resultsByIndicator.isNotEmpty()) {
        resultTitleYCoordinate = currentTableHeight + TEXT_HEIGHT_COMPENSATOR
        rowLines.addLineAt(currentTableHeight = currentTableHeight, lineHeight = LINE_HEIGHT)
        currentTableHeight += LINE_HEIGHT

        resultsByIndicator.forEach { (resultTitle, resultsPerLine) ->
            val resultsByPeriod = resultsPerLine.groupBy { it.periodNumber!! }
            val resultsHeight = (resultsByPeriod.values.maxOfOrNull { it.size } ?: 0) * LINE_HEIGHT

            if (resultsHeight == 0)
                return@forEach

            // add line for result indicator
            rowLines.addLineAt(currentTableHeight = currentTableHeight, lineHeight = resultsHeight)
            titles.addMinorTitle(y = currentTableHeight, title = resultTitle)

            resultsByPeriod.forEach { (_, resultsWithinPeriod) ->
                resultsWithinPeriod.forEachIndexed { index, it ->
                    rectanglesSmall.add(
                        getRectangle(
                            startPeriod = it.periodNumber!!,
                            columnWidth = columnWidth,
                            isSmall = true,
                            currentTableHeight = currentTableHeight,
                            index = index,
                            colorIndex = workPackages.size,
                            text = "R${it.resultNumber}",
                        )
                    )
                }
            }
            currentTableHeight += resultsHeight
        }
    }

    rowLines.removeLast() // last line should be border itself

    return TimeplanData(
        periodColumns = periods.toCoordinates(columnWidth),
        columnDividers = periods.drop(1).toColumnDividers(columnWidth, currentTableHeight),
        rowDividers = rowLines,
        width = TABLE_WIDTH,
        height = currentTableHeight,
        rowHeight = LINE_HEIGHT,
        borderAround = generateBordersAroundAndSidenavDivider(currentTableHeight),
        viewBox = "0 -1 $TABLE_WIDTH ${currentTableHeight + 2}",
        data = rectanglesBig.plus(rectanglesSmall),
        texts = titles,
        resultTitleYCoordinate = resultTitleYCoordinate,
    )
}




private val colorsDark = arrayListOf(
    "#FFA726",
    "#546E7A",
    "#7CB342",
    "#795548",
    "#AB47BC",
    "#00BCD4",
    "#EC407A",
    "#1E88E5",
)
private val colorsLight = arrayListOf(
    "#FFE0B2",
    "#A7C0CD",
    "#BEF67A",
    "#D3B8AE",
    "#FFC4FF",
    "#CCF2F6",
    "#FFC1E3",
    "#81D4FA",
)

private fun getColorForWp(wpIndex: Int, isDark: Boolean = true): String {
    if (isDark)
        return colorsDark[wpIndex % colorsDark.size]
    else
        return colorsLight[wpIndex % colorsDark.size]
}

private fun getRectangle(
    startPeriod: Int,
    spanPeriods: Int = 1,
    spanRows: Int = 1,
    columnWidth: Float,
    isSmall: Boolean = false,
    currentTableHeight: Int,
    index: Int = 0,
    colorIndex: Int,
    text: String = "",
): Rectangle {
    return Rectangle(
        x = LEFT_BAR_WIDTH + (startPeriod - 1) * columnWidth + getMargin(isSmall),
        y = currentTableHeight + (index * LINE_HEIGHT) + getMargin(isSmall),
        width = spanPeriods * columnWidth - 2 * getMargin(isSmall),
        height = (spanRows * LINE_HEIGHT) - 2 * getMargin(isSmall),
        rx = if (isSmall) 2 else 3,
        fill = getColorForWp(colorIndex, !isSmall),
        stroke = getColorForWp(colorIndex, true),
        strokeWidth = if (isSmall) 1 else 0,
    ).apply { this.text = if (text.isBlank()) null else RectangleText(
        x = this.x - getMargin(isSmall) + columnWidth / 2,
        y = this.y - getMargin(isSmall) + TEXT_HEIGHT_COMPENSATOR,
        value = text,
    ) }
}

private fun getMargin(isSmall: Boolean) = if (isSmall) RECT_SMALL_MARGIN else RECT_BIG_MARGIN

private fun MutableList<String>.addLineAt(currentTableHeight: Int, lineHeight: Int) = this.add(
    "M0 ${currentTableHeight + lineHeight} L${TABLE_WIDTH} ${currentTableHeight + lineHeight}"
)

private fun MutableList<Rectangle>.addWorkPackageOverallRectangleIfNeeded(
    startPeriod: Int?,
    endPeriod: Int?,
    columnWidth: Float,
    tableHeight: Int,
    colorIndex: Int,
) {
    if (startPeriod != null && endPeriod != null)
        this.add(
            getRectangle(
                startPeriod = startPeriod,
                spanPeriods = endPeriod - startPeriod + 1,
                columnWidth = columnWidth,
                currentTableHeight = tableHeight,
                colorIndex = colorIndex,
            )
        )
}

private fun List<ProjectPeriodData>.toColumnDividers(columnWidth: Float, tableHeight: Int) =
    mapIndexed { index, _ -> 200 + ((index + 1) * columnWidth) }
        .map { "M$it 0 L$it $tableHeight" }

private fun generateBordersAroundAndSidenavDivider(tableHeight: Int) = listOf(
    "M0 0 L$TABLE_WIDTH 0",
    "M0 0 L0 $tableHeight",
    "M$TABLE_WIDTH 0 L$TABLE_WIDTH $tableHeight",
    "M0 $tableHeight L$TABLE_WIDTH $tableHeight",
    // sidenav divider
    "M$LEFT_BAR_WIDTH 0 L$LEFT_BAR_WIDTH $tableHeight",
)

private fun List<ProjectPeriodData>.toCoordinates(columnWidth: Float) = mapIndexed { index, projectPeriodData -> PeriodColumn(
    number = projectPeriodData.number,
    xTextMiddle = LEFT_BAR_WIDTH + ((index + 0.5f) * columnWidth),
) }

private fun MutableList<TranslatedText>.addMajorTitle(y: Int, title: String) = this.add(
    TranslatedText(
        x = LEFT_TEXT_MARGIN.toFloat(),
        y = y + TEXT_HEIGHT_COMPENSATOR,
        value = title.truncateLong(),
        fontWeight = 700,
    )
)

private fun MutableList<TranslatedText>.addMinorTitle(y: Int, title: String?) =
    if (title.isNullOrEmpty())
        false
    else
        this.add(
            TranslatedText(
                x = LEFT_TEXT_MARGIN.toFloat(),
                y = y + TEXT_HEIGHT_COMPENSATOR,
                value = "$nbsp$nbsp$nbsp$nbsp$title".truncateLong(),
                fontStyle = "italic",
                fill = "#333333",
            )
        )

private fun String.truncateLong() =
    if (this.length > LEFT_TEXT_MAX_LENGTH)
        "${substring(0, LEFT_TEXT_MAX_LENGTH - 1)}…"
    else
        this

private fun WorkPackageActivityData.getAllUsedPeriods(): Set<Int> {
    val allPeriods = this.deliverables.mapNotNull { it.period }.toMutableSet()
    if (this.startPeriod != null)
        allPeriods.add(this.startPeriod!!)
    if (this.endPeriod != null)
        allPeriods.add(this.endPeriod!!)
    return allPeriods.toSet()
}
