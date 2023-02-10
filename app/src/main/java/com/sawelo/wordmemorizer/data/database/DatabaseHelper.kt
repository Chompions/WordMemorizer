package com.sawelo.wordmemorizer.data.database

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider.getUriForFile
import com.sawelo.wordmemorizer.MainApplication
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import java.io.File
import java.io.FileOutputStream

class DatabaseHelper(private val context: Context) {
    val databaseNameList = listOf(
        AppDatabase.DATABASE_NAME,
        "${AppDatabase.DATABASE_NAME}-shm",
        "${AppDatabase.DATABASE_NAME}-wal"
    )

    fun exportDb(): List<Uri> {
        context.showToast("Export database started")
        val backupDir = File("${context.filesDir}/databases_backup")
        val databaseDir = File("${context.dataDir}/databases/")
        if (!backupDir.exists()) backupDir.mkdir()

        return databaseNameList.map {
            copyWithFileUri(it, databaseDir, backupDir)
        }
    }

    private fun copyWithFileUri(fileName: String, sourceDir: File, targetDir: File): Uri {
        val targetFile = File(targetDir, fileName)
        File(sourceDir, fileName).copyTo(targetFile, true)
        return getUriForFile(context, "${MainApplication.PACKAGE_NAME}.file-provider", targetFile)
    }

    fun importDb(fileName: String, uri: Uri) {
        try {
            val targetFile = File("${context.dataDir}/databases/", fileName)
            val outputStream = FileOutputStream(targetFile)
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            throw Exception("Import failed: ${e.message}")
        }
    }

    fun checkIsOtherPackageInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= 33) {
                context.packageManager.getPackageInfo(MainApplication.OTHER_PACKAGE_NAME, PackageManager.PackageInfoFlags.of(0L))
                true
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(MainApplication.OTHER_PACKAGE_NAME, 0)
                true
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("DatabaseHelper", "Checking other package failed: ${e.message}")
            false
        }
    }
}