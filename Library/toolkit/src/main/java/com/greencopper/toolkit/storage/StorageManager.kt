package com.greencopper.toolkit.storage

import java.io.File

public interface StorageManager {

    /** Project tag used to redirect files and cache storage for multi-project applications **/
    public var projectTag: String

    /** Get files storage for this specific project **/
    public suspend fun getProjectFilesStorage(projectTag: String? = null): File

    /** Get global files storage **/
    public suspend fun getFilesStorage(): File

    /** Get cache storage for this specific project **/
    public suspend fun getProjectCacheStorage(projectTag: String? = null): File

    /** Get global cache storage **/
    public suspend fun getCacheStorage(): File

    /** Get an asset as a [File] **/
    public suspend fun getAssetAsFile(assetPath: String): File

    public suspend fun deleteFile(file: File)

    public companion object {
        public const val defaultProjectTag: String = "defaultProjectTag"
    }
}
