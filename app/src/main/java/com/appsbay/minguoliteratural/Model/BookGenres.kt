package com.appsbay.minguoliteratural.Model

import android.content.Context
import com.appsbay.minguoliteratural.R

/** Stable keys for the five author shelves, independent of display script. */
object BookGenres {
    const val ALL = ""
    const val ROMANCE = "Shen Congwen"
    const val REALISTIC = "Lu Xun"
    const val FANTASY = "Zhang Henshui"
    const val GOTHIC = "Ba Jin"
    const val CRIME = "Other"

    @JvmField
    val ALL_NAMES: List<String> = listOf(ROMANCE, REALISTIC, FANTASY, GOTHIC, CRIME)

    @JvmStatic
    fun shortLabelRes(categoryName: String): Int = when (categoryName) {
        ROMANCE -> R.string.genre_romance
        REALISTIC -> R.string.genre_realistic
        FANTASY -> R.string.genre_fantasy
        GOTHIC -> R.string.genre_gothic
        CRIME -> R.string.genre_crime
        else -> R.string.genre_all
    }

    @JvmStatic
    fun displayName(context: Context, categoryName: String?): String {
        if (categoryName.isNullOrEmpty()) return context.getString(R.string.genre_all)
        val res = when (categoryName) {
            ROMANCE -> R.string.genre_romance_full
            REALISTIC -> R.string.genre_realistic_full
            FANTASY -> R.string.genre_fantasy_full
            GOTHIC -> R.string.genre_gothic_full
            CRIME -> R.string.genre_crime_full
            else -> return categoryName
        }
        return context.getString(res)
    }

    @JvmStatic
    fun matches(book: Book, categoryName: String?): Boolean =
        categoryName.isNullOrEmpty() || categoryNameFor(book.bookType) == categoryName

    @JvmStatic
    fun categoryNameFor(type: BookType): String = when (type) {
        BookType.shenCongWen, BookType.shenCongWen_Fan -> ROMANCE
        BookType.luXun, BookType.luXun_Fan -> REALISTIC
        BookType.zhangHenShui, BookType.zhangHenShui_Fan -> FANTASY
        BookType.baJin, BookType.baJin_Fan -> GOTHIC
        BookType.other, BookType.other_Fan -> CRIME
    }
}
