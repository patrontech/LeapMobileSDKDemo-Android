package com.greencopper.toolkit.zip

import kotlinx.coroutines.*
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.io.inputstream.SplitInputStream
import net.lingala.zip4j.io.inputstream.ZipInputStream
import net.lingala.zip4j.model.*
import net.lingala.zip4j.util.InternalZipConstants
import net.lingala.zip4j.util.UnzipUtil
import java.io.*
import java.nio.charset.Charset
import java.util.regex.Matcher

@Throws(ZipException::class)
public suspend fun ZipFile.extractAll(
    coroutineScope: CoroutineScope,
    destinationPath: String,
    password: String? = null,
    allowFailure: Boolean
) {
    val destinationFile = File(destinationPath)
    TKZipExtractor(coroutineScope, this, password?.toCharArray(), destinationFile).extract(
        allowFailure
    )
}

internal class TKZipExtractor(
    private val coroutineScope: CoroutineScope,
    private val zipFile: ZipFile,
    private val password: CharArray? = null,
    private val destinationPath: File
) {

    //Value determined through testing and experimentation.
    //A value too low prevents certain values to be unzipped correctly
    //while values too high can obstruct memory and add time overhead.
    //This is equal to 262 144 bytes, keeping it as a multiple of 32Ko
    //makes it easier to understand.
    private val bufferSize = 32768 * 8

    @Throws(IOException::class)
    suspend fun extract(allowFailure: Boolean) {
        val jobs: ArrayList<Deferred<Unit>> = arrayListOf()
        initializeZipModel()
        val zipModel =
            ZipFile::class.java
                .getDeclaredField("zipModel")
                .apply { isAccessible = true }
                .get(zipFile) as ZipModel
        zipModel.centralDirectory.fileHeaders.forEach { encryptedHeaderFile ->
            jobs.add(
                createUnzipJob(
                    zipModel,
                    encryptedHeaderFile,
                    allowFailure,
                )
            )
        }
        //We have to wait for all jobs to finish because we cannot guarantee the order in which
        //important files such as content json files and placeholder image will be unzipped
        try {
            jobs.awaitAll()
        } catch (t: Throwable) {
            if (t !is CancellationException) {
                throw t
            }
        }
    }

    //This function initialize the zipModel field within zipFile.
    //There is no ini function or setter for this object but the fileHeaders getter
    //also initialize the zipModel by reading the zip file info so it's the quickest and safest way
    //of doing it through the library.
    private fun initializeZipModel() {
        zipFile.fileHeaders
    }

    private fun createUnzipJob(
        zipModel: ZipModel,
        encryptedHeaderFile: FileHeader,
        allowFailure: Boolean,
    ) = coroutineScope.async {
        var splitInputStream: SplitInputStream? = null
        var zipInputStream: ZipInputStream? = null
        var outputStream: OutputStream? = null
        try {
            //We create a SplitInputStream to handle each file stream but it needs to be wrapped
            //into a ZipInputStream to get the right byte offsets and ensure the stream is carefully
            //decrypted and uncompressed.
            //We create a new one for each file because it cannot be used concurrently by multiple coroutines
            splitInputStream = UnzipUtil.createSplitInputStream(zipModel)
            zipInputStream = ZipInputStream(
                splitInputStream,
                password,
                Zip4jConfig(Charset.defaultCharset(), bufferSize)
            )

            //Those steps are essential to prepare the stream for extraction,
            //DO NOT change the order of those calls
            splitInputStream.prepareExtractionForFileHeader(encryptedHeaderFile)
            zipInputStream.getNextEntry(encryptedHeaderFile)

            val decryptedFile = determineOutputFile(
                encryptedHeaderFile,
                destinationPath.absolutePath,
                encryptedHeaderFile.fileName
            )

            if (encryptedHeaderFile.isDirectory) {
                if (!decryptedFile.exists() && !decryptedFile.mkdirs()) {
                    throw ZipException("Could not create directory: $decryptedFile")
                }
            } else {
                checkOutputDirectoryStructure(decryptedFile)
                val unZippedByteArray = ByteArray(bufferSize)
                outputStream = FileOutputStream(decryptedFile)
                var readLength = zipInputStream.read(unZippedByteArray)
                while (readLength != -1) {
                    outputStream.write(unZippedByteArray, 0, readLength)
                    readLength = zipInputStream.read(unZippedByteArray)
                }
            }
            UnzipUtil.applyFileAttributes(encryptedHeaderFile, decryptedFile)
        } catch (throwable: Throwable) {
            if (!allowFailure) {
                throw throwable
            }
        } finally {
            splitInputStream?.close()
            zipInputStream?.close()
            outputStream?.close()
        }
    }

    private fun determineOutputFile(
        fileHeader: FileHeader,
        outputPath: String,
        newFileName: String
    ): File {
        val outputFileName: String = if (newFileName.isNotEmpty()) {
            newFileName
        } else {
            // replace all slashes with file separator
            getFileNameWithSystemFileSeparators(fileHeader.fileName)
        }
        return File(outputPath + InternalZipConstants.FILE_SEPARATOR + outputFileName)
    }

    private fun getFileNameWithSystemFileSeparators(fileNameToReplace: String): String {
        return fileNameToReplace.replace(
            "[/\\\\]".toRegex(),
            Matcher.quoteReplacement(InternalZipConstants.FILE_SEPARATOR)
        )
    }

    @Throws(ZipException::class)
    @Synchronized
    private fun checkOutputDirectoryStructure(outputFile: File) {
        outputFile.parentFile?.let {
            if (!it.exists() && !it.mkdirs()) {
                throw ZipException("Unable to create parent directories: $it")
            }
        }
    }
}
