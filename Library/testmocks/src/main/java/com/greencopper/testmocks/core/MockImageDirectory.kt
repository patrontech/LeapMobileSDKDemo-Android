package com.greencopper.testmocks.core

import java.io.File

public class MockImageDirectory(private val imagesList: List<String>) : File("") {
    override fun list(): Array<String> = imagesList.toTypedArray()
}
