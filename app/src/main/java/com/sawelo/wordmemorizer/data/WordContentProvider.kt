package com.sawelo.wordmemorizer.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.sawelo.wordmemorizer.data.data_class.Word

class WordContentProvider: ContentProvider {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val code = MATCHER.match(uri)
        if (code == CODE_WORD_DIR || code == CODE_WORD_ITEM) {
            val context = context ?: return null
            val wordDao = AppDatabase.getInstance(context).wordDao()
            val cursor = if (code == CODE_WORD_DIR) {
                wordDao.
            }
        }
    }

    override fun getType(p0: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    companion object {
        private const val AUTHORITY = "com.sawelo.android.wordmemorizer.provider"
        /** Match code for an some items in Word table */
        private const val CODE_WORD_DIR = 1
        /** Match code for an item in Word table */
        private const val CODE_WORD_ITEM = 2
        /** URI for Word table */
        private val URI_WORD = Uri.parse("content:://$AUTHORITY/${Word.TABLE_NAME}")
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, Word.TABLE_NAME, CODE_WORD_DIR)
            addURI(AUTHORITY, "${Word.TABLE_NAME}/*", CODE_WORD_ITEM)
        }
    }
}