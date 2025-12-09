package com.greencopper.testmocks

import com.greencopper.toolkit.httpclient.APIProvider
import java.time.Duration

public class MockAPIProvider<API : Any>(public val api: API) : APIProvider<API> {

    override fun api(timeout: Duration?): API = api
}
