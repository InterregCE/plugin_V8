package io.cloudflight.jems.plugin.standard.common.excel.model

class Color(val red: Short, val green: Short,val blue: Short) {
    companion object {
        val WHITE = Color(255,255,255)
        val BLACK = Color(0,0,0)
        val LIGHT_BLUE = Color(220,240,255)
        val LIGHT_GREEN = Color(230,240,220)
        val LIGHT_RED = Color(250,230,220)
        val LIGHT_ORANGE = Color(255,240,200)
    }
}