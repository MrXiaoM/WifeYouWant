package top.mrxiaom

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.net.URL

interface PrepareMessage : SingleMessage {
    suspend fun generateMessage(): SingleMessage
    override fun contentToString(): String = ""
}

class PrepareUploadImage(
    private val contact: Contact,
    private val handler: suspend () -> ExternalResource?,
    private val failedText: String = ""
) : PrepareMessage {
    override suspend fun generateMessage(): SingleMessage {
        try {
            val res = handler() ?: return PlainText(failedText)
            return contact.uploadImage(res).also { res.close() }
        } catch (_: Throwable) {
            return PlainText(failedText)
        }
    }

    override fun contentToString(): String = failedText
    override fun toString(): String = failedText

    companion object {
        @JvmStatic
        fun url(contact: Contact, link: String, failedText: String = ""): PrepareUploadImage {
            return PrepareUploadImage(contact, {
                try {
                    val conn = withContext(Dispatchers.IO) {
                        URL(link).openConnection().also { it.connect() }
                    }
                    return@PrepareUploadImage withContext(Dispatchers.IO) {
                        conn.getInputStream()
                    }.toExternalResource()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    return@PrepareUploadImage null
                }
            }, failedText)
        }

        fun Contact.prepareUploadImage(link: String, failedText: String = ""): PrepareUploadImage {
            return url(this, link, failedText)
        }

        fun Contact.prepareUploadAvatarImage(failedText: String = ""): PrepareUploadImage {
            return this.prepareUploadImage(this.avatarUrl, failedText)
        }
    }
}

suspend fun String.replace(replacements: Map<String, SingleMessage>): MessageChain {
    if (!this.contains("\$")) return PlainText(this).toMessageChain()
    val keys = replacements.keys
    val message = MessageChainBuilder()
    val s = this.split("\$").toMutableList()
    message.add(s[0])
    s.removeAt(0)
    s.forEach { text ->
        var isOriginal = true
        for (k in keys) {
            if (!text.startsWith(k)) continue
            replacements[k]?.let {
                if (it is PrepareMessage) message.add(it.generateMessage())
                else message.add (it)
            }

            message.add(text.substring(k.length))
            isOriginal = false
            break
        }
        if (isOriginal) message.add("\$$text")
    }
    return message.build()
}