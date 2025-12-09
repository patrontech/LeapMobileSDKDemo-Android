package com.greencopper.toolkit.httpclient

import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File

public fun Response<ResponseBody>.saveToFile(url: String, downloadDirectory: File): File {
    val body = body() ?: throw HttpException(this)
    val fileName = guessFileName(url)
    val destination = File(downloadDirectory, fileName)
    val destinationTemp = File(downloadDirectory, fileName + "_temp")

    destinationTemp.apply {
        parentFile?.mkdirs()
        createNewFile()
    }

    val instream = body.byteStream()
    destinationTemp.outputStream().use { outstream ->
        val buffer = ByteArray(4 * 1024)
        var read: Int
        while (instream.read(buffer).also { read = it } != -1) {
            outstream.write(buffer, 0, read)
        }
        outstream.flush()
    }

    destinationTemp.renameTo(destination)

    return destination
}

public fun guessFileName(url: String): String {
    var filename: String? = null
    var cleanedUrl = url
    val queryIndex = cleanedUrl.indexOf('?')
    // If there is a query string strip it, same as desktop browsers
    if (queryIndex > 0) {
        cleanedUrl = cleanedUrl.substring(0, queryIndex)
    }
    if (!cleanedUrl.endsWith("/")) {
        val index = cleanedUrl.lastIndexOf('/') + 1
        if (index > 0) {
            filename = cleanedUrl.substring(index)
        }
    }
    if (filename == null) {
        filename = "downloadFile"
    }
    return filename
}
