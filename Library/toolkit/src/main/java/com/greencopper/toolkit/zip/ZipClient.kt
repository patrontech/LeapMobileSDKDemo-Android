package com.greencopper.toolkit.zip

import java.io.File

public interface ZipClient {
    /**
     * Zip a directory with an optional password
     *
     * @param originDirectory The directory containing the files
     * @param destination The destination and name of the zip file
     * @param password The password to encrypt the zip
     * @return A [File] with the destination
     */
    public suspend fun zip(originDirectory: File, destination: File, password: String?): File

    /**
     * UnZip an encrypted directory with a password
     *
     * @param originZip The zip file to unzip.
     * @param destination The destination folder.
     * @param password The password to decrypt the zip.
     * @param allowFailure If true, any exception occuring during the process will be caught.
     * @return A [File] with the destination
     */
    public suspend fun unZipEncryptedFile(
        originZip: File,
        destination: File,
        password: String,
        allowFailure: Boolean = true
    ): File

    /**
     * UnZip a directory
     *
     * @param originZip The zip file to unzip.
     * @param destination The destination folder.
     * @param allowFailure If true, any exception occuring during the process will be caught.
     * @return A [File] with the destination
     */
    public suspend fun unZipFile(
        originZip: File,
        destination: File,
        allowFailure: Boolean = true
    ): File
}
