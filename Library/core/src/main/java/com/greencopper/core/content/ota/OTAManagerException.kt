package com.greencopper.core.content.ota

public sealed class OTAManagerException(cause: Throwable? = null) : Throwable(cause) {
    public class MissingUrlException(private val otaContent: OTAContent) : OTAManagerException() {
        override val message: String
            get() {
                return "[OTAManagerException] Couldn't retrieve OTAContent with missing URL: $otaContent"
            }
    }

    public class DownloadFailedException(cause: Throwable) : OTAManagerException(cause) {
        override val message: String
            get() {
                return "[OTAManagerException] Couldn't download OTA content: $cause"
            }
    }
}
