package com.greencopper.testmocks.core

import android.annotation.SuppressLint
import java.io.*
import java.nio.file.Files

public object MockFiles {

    /**
     * Function used to transpose files from resources to actual files (available to use with the class [File][java.io.File].
     * To use this method, place your files and directories into "test/resources" or "androidTest/resources"
     * They will be copied and placed in a temporary file that must be deleted at the end of your test with [File.deleteRecursively]
     *
     * @param obj Used to access resources, usually the test class (this)
     * @param filesPath List of files' path that will be copied. Must start with '/', the root being the "resources" directory
     *
     * @return The root directory that can be found in the temp files.
     */
    public fun createTempFileFrom(obj: Any, filesPath: List<String>): File {
        val tempDir = createTempDirectory()

        try {
            copyResourceToDir(obj, tempDir, filesPath)
        } catch(throwable: Throwable) {
            tempDir.deleteRecursively()
            throw throwable
        }

        return tempDir
    }

    @SuppressLint("NewApi")
    public fun createTempDirectory(dirPrefix: String = "tempFiles"): File = Files.createTempDirectory(dirPrefix).toFile()

    /**
     * Function used to transpose files from resources to actual files (available to use with the class [File][java.io.File].
     * To use this method, place your files and directories into "test/resources" or "androidTest/resources"
     * They will be copied and placed in a temporary file that must be deleted at the end of your test with [File.deleteRecursively]
     *
     * @param obj Used to access resources, usually the test class (this)
     * @param directory The root of the destination file
     * @param filesPath List of files' path that will be copied. Must start with '/', the root being the "resources" directory
     */
    public fun copyResourceToDir(obj: Any, directory: File, filesPath: List<String>) {
        if (filesPath.any { !it.startsWith("/") }) throw IllegalArgumentException("Path should start with '/'")

        filesPath.forEach {
            val inStreamFile = getInputStreamOfResource(obj, it)
            val outFile = File("${directory.path}$it")
            outFile.parentFile?.mkdirs()

            inStreamFile.copyTo(FileOutputStream(outFile))
            inStreamFile.close()
        }
    }

    /**
     * Function used to get a file as an [InputStream]
     * To use this method, place your file into "test/resources" or "androidTest/resources"
     *
     * @param obj Used to access resources, usually the test class (this)
     * @param resourcePath The file to open. Must start with '/', the root being the "resources" directory
     *
     * @return The [InputStream] of the file
     */
    public fun getInputStreamOfResource(obj: Any, resourcePath: String): InputStream =
        obj::class.java.getResourceAsStream(resourcePath)!!

}
