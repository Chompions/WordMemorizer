package com.sawelo.wordmemorizer.data.database


object UnzipUtils {

//    fun unzip(path: String, zipName: String) {
//        try {
//            val inputStream = FileInputStream(path + zipName)
//            val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))
//            val zipEntry = zipInputStream.nextEntry
//            val buffer = ByteArray(1024)
//            var count: Int
//            while (zipEntry != null) {
//                val fileName = zipEntry.name
//                if (zi )
//            }
//        }
//    }
//
//    fun undwazip(zipFilePath: File, destDirectory: String) {
//        File(destDirectory).run {
//            if (!exists()) {
//                mkdirs()
//            }
//        }
//
//        ZipFile(zipFilePath).use { zip ->
//            zip.entries().asSequence().forEach { entry ->
//                zip.getInputStream(entry).use { input ->
//                    val filePath = destDirectory + File.separator + entry.name
//                    if (!entry.isDirectory) {
//                        extractFile(input, filePath)
//                    } else {
//                        val dir = File(filePath)
//                        dir.mkdir()
//                    }
//                }
//            }
//        }
//    }
//
//    private fun extractFile(inputStream: InputStream, destFilePath: String) {
//        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
//        val bytesIn = ByteArray(4096)
//        var read: Int
//        while (inputStream.read(bytesIn).also { read = it } != -1) {
//            bos.write(bytesIn, 0, read)
//        }
//        bos.close()
//    }

}