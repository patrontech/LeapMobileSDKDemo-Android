package com.greencopper.testmocks.toolkit

import com.greencopper.toolkit.zip.ZipClient
import java.io.File

public class MockZipClient(
    private val zip: () -> File = { File("") },
    private val unZipFile: () -> File = { File("") },
    private val unZipEncryptedFile: () -> File = { File("") },
) : ZipClient {

    public var zipCount: Int = 0
        private set

    public var unZipFileCount: Int = 0
        private set

    public var unZipEncryptedFileCount: Int = 0
        private set

    override suspend fun zip(
        originDirectory: File,
        destination: File,
        password: String?
    ): File = zip().also { zipCount += 1 }

    override suspend fun unZipFile(
        originZip: File,
        destination: File,
        allowFailure: Boolean
    ): File = unZipFile().also { unZipFileCount += 1 }

    override suspend fun unZipEncryptedFile(
        originZip: File,
        destination: File,
        password: String,
        allowFailure: Boolean
    ): File = unZipEncryptedFile().also { unZipEncryptedFileCount += 1 }
}
