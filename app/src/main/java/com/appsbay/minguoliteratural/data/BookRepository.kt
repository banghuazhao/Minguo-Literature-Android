package com.appsbay.minguoliteratural.data

import android.content.Context
import com.appsbay.minguoliteratural.Model.Book
import com.appsbay.minguoliteratural.Model.BookCategory
import com.appsbay.minguoliteratural.Model.BookCategoryStore
import com.appsbay.minguoliteratural.Model.BookChapter
import com.appsbay.minguoliteratural.Model.BookStore
import com.appsbay.minguoliteratural.Tools.ReadingProgressHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/** All Minguo editions ship in assets and are available offline. */
class BookRepository private constructor(context: Context) {
    private val appContext = context.applicationContext

    fun getTopLevelBooks(): List<Book> = BookStore.shared.getBooks(appContext)
    fun getAllBooks(): List<Book> = BookStore.shared.getAllBooks(appContext)
    fun getCategories(): List<BookCategory> = BookCategoryStore.shared.getCategories()
    fun getBooksForCollection(book: Book): List<Book> =
        BookStore.shared.getBooksForCollection(book)
    fun isAvailableOffline(book: Book): Boolean = true

    suspend fun loadChapters(book: Book): Resource<List<BookChapter>> = withContext(Dispatchers.IO) {
        val json = try {
            appContext.assets.open("files/${book.fileName}.json").use {
                it.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: IOException) {
            return@withContext Resource.Error(BookLoadError.CONTENT_UNAVAILABLE, e.message)
        }
        val chapters = try {
            parseChapters(json, book.name)
        } catch (e: JSONException) {
            return@withContext Resource.Error(BookLoadError.CONTENT_UNAVAILABLE, e.message)
        }
        if (chapters.isEmpty()) {
            Resource.Error(BookLoadError.CONTENT_UNAVAILABLE)
        } else {
            ReadingProgressHelper.saveTotalChapters(appContext, book, chapters.size)
            Resource.Success(chapters)
        }
    }

    private fun parseChapters(jsonString: String, bookName: String): List<BookChapter> {
        val obj = JSONObject(jsonString)
        val array = when {
            obj.has(bookName) -> obj.getJSONArray(bookName)
            obj.keys().hasNext() -> obj.getJSONArray(obj.keys().next())
            else -> return emptyList()
        }
        val chapters = ArrayList<BookChapter>(array.length())
        for (i in 0 until array.length()) {
            val chapter = array.getJSONObject(i)
            chapters.add(
                BookChapter(
                    chapterField(chapter, "章节", "章節"),
                    chapterField(chapter, "章节名称", "章節名稱"),
                    chapterField(chapter, "章节内容", "章節內容")
                )
            )
        }
        return chapters
    }

    private fun chapterField(chapter: JSONObject, simplified: String, traditional: String): String =
        when {
            chapter.has(simplified) -> chapter.getString(simplified)
            chapter.has(traditional) -> chapter.getString(traditional)
            else -> throw JSONException("Missing chapter field: $simplified / $traditional")
        }

    companion object {
        @Volatile private var instance: BookRepository? = null
        @JvmStatic fun getInstance(context: Context): BookRepository =
            instance ?: synchronized(this) {
                instance ?: BookRepository(context.applicationContext).also { instance = it }
            }
    }
}
