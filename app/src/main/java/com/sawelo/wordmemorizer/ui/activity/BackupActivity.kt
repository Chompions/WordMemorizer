package com.sawelo.wordmemorizer.ui.activity

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.database.DatabaseHelper

class BackupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        val databaseHelper = DatabaseHelper(this)
        val exportedUris = databaseHelper.exportDb()

        val clipData = ClipData.newRawUri(null, exportedUris[0])
        clipData.addItem(ClipData.Item(exportedUris[1]))
        clipData.addItem(ClipData.Item(exportedUris[2]))
        val resultIntent = Intent()
        resultIntent.clipData = clipData
        resultIntent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        setResult(Activity.RESULT_OK, resultIntent)

        finish()
    }
}