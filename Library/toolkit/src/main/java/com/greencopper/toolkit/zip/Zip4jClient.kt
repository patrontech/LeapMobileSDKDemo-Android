package com.greencopper.toolkit.zip

import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.i
import kotlinx.coroutines.CoroutineScope
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import net.lingala.zip4j.exception.ZipException as Zip4JException

public class Zip4jClient(
    private val coroutineScope: CoroutineScope,
) : ZipClient {

    override suspend fun zip(
        originDirectory: File,
        destination: File,
        password: String?,
    ): File {
        try {
            val zipFile = ZipFile(destination)
            val zipParameters = ZipParameters()
            password?.let {
                zipFile.setPassword(password.toCharArray())
                zipParameters.isEncryptFiles = true
                zipParameters.encryptionMethod = EncryptionMethod.AES
            }
            zipFile.addFolder(originDirectory, zipParameters)
            App.log.i("Zipped ${originDirectory.name} to ${destination.name}")
        } catch (e: Zip4JException) {
            val standardError = when {
                isInputError(e) -> ZipException.InputException(originDirectory)
                else -> ZipException.UnknownException(e)
            }
            throw standardError
        }
        return destination
    }

    private fun isInputError(e: Zip4JException): Boolean {
        val messages = listOf(
            "input path is null, cannot add folder to zip file",
            "folder does not exist",
            "input folder is not a directory",
            "cannot read input folder"
        )
        return e.message in messages
    }

    override suspend fun unZipEncryptedFile(
        originZip: File,
        destination: File,
        password: String,
        allowFailure: Boolean,
    ): File {
        try {
            if (password.isEmpty()) {
                throw IllegalArgumentException("Password is blank. Please provide a valid password.")
            }

            unZip(originZip, destination, password, allowFailure)
        } catch (t: Throwable) {
            val standardError = if (t is Zip4JException && t.type == Zip4JException.Type.WRONG_PASSWORD) {
                UnZipException.WrongPasswordException()
            } else {
                UnZipException.UnknownException(t)
            }
            throw standardError
        }
        return destination
    }

    override suspend fun unZipFile(
        originZip: File,
        destination: File,
        allowFailure: Boolean,
    ): File {
        try {
            unZip(originZip, destination, null, allowFailure)
        } catch (e: Zip4JException) {
            throw UnZipException.UnknownException(e)
        }
        return destination
    }

    private suspend fun unZip(
        originZip: File,
        destination: File,
        password: String?,
        allowFailure: Boolean,
    ) {
        if (!originZip.exists()) {
            throw IllegalArgumentException("Provided file doesn't exist")
        }

        val zipFile = ZipFile(originZip)
        zipFile.extractAll(
            coroutineScope,
            destination.absolutePath,
            password,
            allowFailure
        )
        App.log.i("Unzipped ${originZip.name} to ${destination.name}")
    }
}
