package com.ffst.dustbinbrain.kotlin_mvp.bean

/**
 * Created by LiuJW
 *on 2021/6/15
 */
enum class DustbinENUM {
    KITCHEN {
        override fun fetchAlias(): String = "厨余垃圾"
    },HARMFUL {
        override fun fetchAlias(): String = "有害垃圾"
    },
    WASTE_PAPER {
        override fun fetchAlias(): String = "纸片"
    },
    BOTTLE {
        override fun fetchAlias(): String = "瓶子"
    },
    OTHER {
        override fun fetchAlias(): String = "其它垃圾"
    },
    RECYCLABLES {
        override fun fetchAlias(): String = "可回收垃圾"
    };
    abstract fun fetchAlias(): String
}