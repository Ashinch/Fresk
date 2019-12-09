package com.ashinch.reader.model

data class Config(
        // 字体大小
        var textSize: Int,
        // 首行缩进
        var firstLineIndent: Boolean,
        // 行间距
        var lineSpacing: Float,
        // 段落间隔
        var paragraphInterval: Int,
        // 水平间距
        var horizontalMargin: Int,
        // 顶部间距
        var topMargin: Int,
        // 底部间距
        var bottomMargin: Int
)