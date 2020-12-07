package com.example.zipunzipsample

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


/**
 * Apps targeting Android Q - API 29 by default are given a filtered view into external storage.
 * A quick fix for that is to add this code in the AndroidManifest.xml:
 * <application android:requestLegacyExternalStorage="true" ... >
 */

class UnzipUtility {


    companion object {
        private val BUFFER_SIZE = 6 * 1024

        @Throws(IOException::class)
        fun zip(files: Array<String>, zipFile: String?) {
            var origin: BufferedInputStream? = null
            val out = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))
            try {
                val data = ByteArray(BUFFER_SIZE)
                for (i in files.indices) {
                    val fi = FileInputStream(files[i])
                    origin = BufferedInputStream(fi, BUFFER_SIZE)
                    try {
                        val entry =
                            ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1))
                        out.putNextEntry(entry)
                        var count: Int
                        while (origin.read(data, 0, BUFFER_SIZE).also { count = it } != -1) {
                            out.write(data, 0, count)
                        }
                    } finally {
                        origin.close()
                    }
                }
            } finally {
                out.close()
            }
        }

        /**
         * Extracts a zip file specified by the zipFilePath to a directory specified by
         * destDirectory (will be created if does not exists)
         * @param zipFilePath
         * @param destDirectory
         * @throws IOException
         */
        @Throws(IOException::class)
        fun unzip(zipFilePath: String?, destDirectory: String?):String {
            var result = ""
            val destDir = File(destDirectory)
            if (!destDir.exists()) {
                destDir.mkdir()
            }

            try {
                val zipIn = ZipInputStream(FileInputStream(zipFilePath))
                var entry = zipIn.nextEntry;
                while (entry != null) {
                    val filePath = destDirectory + File.separator + entry.name
                    if (!entry.isDirectory) {
                        val bos = BufferedOutputStream(FileOutputStream(filePath))
                        val bytesIn = ByteArray(BUFFER_SIZE)
                        var read = 0
                        while (zipIn.read(bytesIn).also { read = it } != -1) {
                            bos.write(bytesIn, 0, read)
                        }
                        bos.close()
                    } else {
                        val dir = File(filePath)
                        dir.mkdir()
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                    result = "file extracted: ${filePath}\n"
                }
                zipIn.close()
            } catch (ex:Exception){
                ex.printStackTrace()
                result = "error\n"
            } finally {
                return result
            }
        }

        fun unzip2(zipFilePath: String?, targetDirectoryPath: String?):String {
            val zipFile = File(zipFilePath)
            val targetDirectory = File(targetDirectoryPath)
            var result = ""
            try {
                FileInputStream(zipFile).use { fis ->
                    BufferedInputStream(fis).use { bis ->
                        ZipInputStream(bis).use { zis ->
                            var ze: ZipEntry
                            var count: Int
                            val buffer =
                                ByteArray(BUFFER_SIZE)
                            while (zis.nextEntry.also {
                                    ze = it
                                } != null) {
                                val file = File(targetDirectory, ze.name)
                                val dir = if (ze.isDirectory) file else file.parentFile
                                if (!dir.isDirectory && !dir.mkdirs()) throw FileNotFoundException(
                                    "Failed to ensure directory: " + dir.absolutePath
                                )
                                if (ze.isDirectory) continue
                                FileOutputStream(file).use { fout ->
                                    while (zis.read(buffer).also {
                                            count = it
                                        } != -1) fout.write(buffer, 0, count)
                                }

                                result+="file extracted: ${file.path}\n"
                            }
                        }
                    }
                }
            } catch (ex: Exception) { //handle exception
                ex.printStackTrace()
            }
            finally {
                return result
            }
        }

        @Throws(IOException::class, FileNotFoundException::class)
        fun unzip3(
            zipFilePath: String?, targetDirectoryPath: String?,
            progress: UnzipFile_Progress?
        ):String {

            val zipFile = File(zipFilePath)
            val targetDirectory = File(targetDirectoryPath)
            val total_len = zipFile.length()
            var total_installed_len: Long = 0
            val zis = ZipInputStream(BufferedInputStream(FileInputStream(zipFile)))
            var result = ""
            try {
                var ze: ZipEntry
                var count: Int
                val buffer = ByteArray(1024)
                while (zis.nextEntry.also { ze = it } != null) {
                    if (progress != null) {
                        total_installed_len += ze.compressedSize
                        val file_name = ze.name
                        val percent = (total_installed_len * 100 / total_len).toInt()
                        progress.Progress(percent, file_name)
                    }
                    val file = File(targetDirectory, ze.name)
                    val dir = if (ze.isDirectory) file else file.parentFile
                    if (!dir.isDirectory && !dir.mkdirs()) throw FileNotFoundException("Failed to ensure directory: " + dir.absolutePath)
                    if (ze.isDirectory) continue
                    val fout = FileOutputStream(file)
                    try {
                        while (zis.read(buffer).also { count = it } != -1) fout.write(
                            buffer,
                            0,
                            count
                        )
                    } finally {
                        fout.close()
                    }
                    // if time should be restored as well
                    val time = ze.time
                    if (time > 0) file.setLastModified(time)
                    result+="file extracted to:${file.path}"
                }
            } catch (ex: Exception) { //handle exception
                ex.printStackTrace()
            } finally {
                zis.close()
                return result
            }
        }

        interface UnzipFile_Progress {
            fun Progress(percent: Int, FileName: String?)
        }
    }
}