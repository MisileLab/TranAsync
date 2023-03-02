package xyz.misilelaboratory.tranasync

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.server.BroadcastMessageEvent
import org.bukkit.plugin.java.JavaPlugin

private val BroadcastMessageEvent.msg: String
    get() {
        val a = message() as TextComponent
        return a.content()
    }
private val Sign.content: String
    get() {
        var a = ""
        for (i in lines()) {
            val i2 = i as TextComponent
            a += i2.content().replace("\n", "") + "\n"
        }
        return a
    }
const val CLIENT_ID = ""
const val CLIENT_SECRET = ""

@Serializable
data class LangDetectResponse(val langCode: String)

@Serializable
data class TranslateResponse(val srcLangCode: String, val tarLangType: String, val translatedText: String)

@Suppress("unused")
class CustomException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

suspend fun detectLang(string: String): LangDetectResponse {
    val client = HttpClient().use {r ->
        r.get {
            url{
                url("https://openapi.naver.com/v1/papago/detectLangs")
                parameters.append("data", string.encodeURLPath())
            }
            header("X-Naver-Client-Id", CLIENT_ID)
            header("X-Naver-Client-Secret", CLIENT_SECRET)
        }
    }
    if (client.status.value != 200) {
        throw CustomException("no 200 status code, message: ${client.bodyAsText()}")
    }
    return Json.decodeFromString(client.bodyAsText())
}

suspend fun translate(summary: String, source: String, target: String): TranslateResponse {
    val client = HttpClient().use {r ->
        r.get {
            url{
                url("https://openapi.naver.com/v1/papago/n2mt")
                parameters.append("source", source)
                parameters.append("target", target)
                parameters.append("text", summary.encodeURLPath())
            }
            header("X-Naver-Client-Id", CLIENT_ID)
            header("X-Naver-Client-Secret", CLIENT_SECRET)
        }
    }
    if (client.status.value != 200) {
        throw CustomException("no 200 status code, message: ${client.bodyAsText()}")
    }
    return Json.decodeFromString(client.bodyAsText())
}

@Suppress("unused")
class TranAsync: JavaPlugin() {
    override fun onEnable() {
        server.pluginManager.registerEvents(TranAsyncEvent(), this)
    }
}

class TranAsyncEvent: Listener {
    @EventHandler
    suspend fun placeEvent(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock!! is Sign) {
            val sign = e.clickedBlock as Sign
            val content = sign.content
            e.player.sendMessage(Component.text(content.translate()))
        }
    }

    @EventHandler
    suspend fun chatEvent(e: BroadcastMessageEvent) {
        val content = e.msg
        e.message(Component.text(content.translate()))
    }
}

private suspend fun String.translate(): String {
    return translate(this, detectLang(this).langCode,"ko").translatedText
}
