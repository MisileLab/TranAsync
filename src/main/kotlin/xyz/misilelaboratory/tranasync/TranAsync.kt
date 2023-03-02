package xyz.misilelaboratory.tranasync

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

const val CLIENT_ID = ""
const val CLIENT_SECRET = ""

@Suppress("unused")
class CustomException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

suspend fun detectLang(string: String): String {
    val client = HttpClient().use {r ->
        r.get {
            url{
                url("https://openapi.naver.com/v1/papago/detectLangs")
                parameters.append("data", string)
            }
            header("X-Naver-Client-Id", CLIENT_ID)
            header("X-Naver-Client-Secret", CLIENT_SECRET)
        }
    }
    if (client.status.value != 200) {
        throw CustomException("no 200 status code")
    }
    return client.bodyAsText()
}

@Suppress("unused")
class TranAsync {
}