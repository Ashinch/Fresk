package com.ashinch.reader.model

data class Page(
        // 段落单元
        var cells: MutableList<Cell>? = null,
        // 章节名
        var chapter: String? = null,
        // 起始位置
        var start: Int? = null,
        // 结束位置
        var end: Int? = null,
        // 视图高度
        var height: Int = 0
)