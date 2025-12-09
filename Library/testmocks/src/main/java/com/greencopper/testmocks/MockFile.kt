package com.greencopper.testmocks

import java.io.*

public class MockFile(
    private val fileName: String = "test",
    private val listFilesValue: Array<File> = emptyArray(),
    private val isDirectoryValue: Boolean = false,
) : File(fileName) {

    override fun listFiles(): Array<File> = listFilesValue

    override fun listFiles(filter: FilenameFilter?): Array<File> = listFilesValue

    override fun listFiles(filter: FileFilter?): Array<File> = listFilesValue

    override fun isDirectory(): Boolean = isDirectoryValue
}
