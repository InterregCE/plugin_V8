package io.cloudflight.jems.plugin.standard.common.excel.model

enum class BorderStyle(val code: Short) {
    NONE(0x0),
    THIN(0x1),
    MEDIUM(0x2),
    DASHED(0x3),
    DOTTED(0x4),
    THICK(0x5),
    DOUBLE(0x6),
    HAIR(0x7),
    MEDIUM_DASHED(0x8),
    DASH_DOT(0x9),
    MEDIUM_DASH_DOT(0xA),
    DASH_DOT_DOT(0xB),
    MEDIUM_DASH_DOT_DOT(0xC),
    SLANTED_DASH_DOT(0xD);
}
