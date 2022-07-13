package top.mrxiaom

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object WifeYouWant : KotlinPlugin(
    JvmPluginDescription(
        id = "top.mrxiaom.wifeyouwant",
        name = "抽老婆",
        version = "0.1.0",
    ) {
        author("MrXiaoM")
        info("""嘻嘻，一天换一个老婆""")
    }
) {
    private val nowTime: String
        get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        PluginConfig.reload()
        UserData.reload()

        this.globalEventChannel().subscribeAlways<GroupMessageEvent> {
            val sender = if(it.sender is NormalMember) it.sender as NormalMember else return@subscribeAlways
            val group = it.group
            if (PluginConfig.messagesRandomWife.isNotEmpty() && PluginConfig.keywordsRandomWife.contains(it.message.content)) {
                val time = nowTime
                val user = UserData.users.getOrDefault(sender.id, SingleUser())
                if (user.time != time) user.wifeId = random(sender).id
                val wife = group[user.wifeId] ?: random(sender)
                user.wifeId = wife.id
                user.time = time
                UserData.users[sender.id] = user
                val s = PluginConfig.messagesRandomWife.random()
                group.sendMessage(genRandomWifeMessage(s, sender, wife))
            } else if (PluginConfig.messagesChangeWife.isNotEmpty() && PluginConfig.keywordsChangeWife.contains(it.message.content)) {
                val time = nowTime
                val user = UserData.users.getOrDefault(sender.id, SingleUser())
                val oldWife = group[user.wifeId] ?: sender
                val wife = random(sender, oldWife.id)
                user.wifeId = wife.id
                user.time = time
                UserData.users[sender.id] = user
                val s = PluginConfig.messagesChangeWife.random()
                group.sendMessage(genChangeWifeMessage(s, sender, oldWife, wife))
            }
        }
    }

    private suspend fun genMemberReplacement(s: String, identity: String, sender: NormalMember, connect: String = "_", atOverride: String? = null) :MutableMap<String, SingleMessage> {
        val prefix = identity + connect
        val map : MutableMap<String,SingleMessage> = mutableMapOf(
            (atOverride ?: identity) to At(sender), "${prefix}namecard" to PlainText(sender.nameCardOrNick),
            "${prefix}nick" to PlainText(sender.nick), "${prefix}qq" to PlainText(sender.id.toString())
        )
        val pic = "${prefix}pic"
        if (s.contains("\$$pic")) {
            try {
                val conn = withContext(Dispatchers.IO) {
                    URL(sender.avatarUrl).openConnection().also { it.connect() }
                }
                val res = withContext(Dispatchers.IO) {
                    conn.getInputStream()
                }.toExternalResource()
                map[pic] = sender.group.uploadImage(res)
                withContext(Dispatchers.IO) {
                    res.close()
                }
            } catch(t: Throwable) {
                map[pic] = PlainText("头像获取失败")
                t.printStackTrace()
            }
        }
        return map
    }

    private suspend fun genRandomWifeMessage(s: String, sender: NormalMember, wife: NormalMember) : MessageChain {
        val map = genMemberReplacement(s, "", sender, "", "you")
        map.putAll(genMemberReplacement(s, "wife", wife))
        return s.replace(map)
    }

    private suspend fun genChangeWifeMessage(s : String, sender: NormalMember, oldWife: NormalMember, wife: NormalMember) : MessageChain {
        val map = genMemberReplacement(s, "", sender, "", "you")
        map.putAll(genMemberReplacement(s, "wife", wife))
        map.putAll(genMemberReplacement(s, "old-wife", oldWife))
        return s.replace(map)
    }

    /**
     * 随机获取一个老婆，在抽不到人的时候返回机器人自身
     * @param sender 请求者
     * @param excludeId 排除群员qq (换老婆时需要)
     */
    private suspend fun random(sender: NormalMember, excludeId: Long? = null) : NormalMember {
        val group = sender.group
        var members :List<NormalMember> = group.members.filter { it.id != group.bot.id && it.id != excludeId }

        if (PluginConfig.checkGender) {
            var gender = sender.queryProfile().sex
            if (gender != UserProfile.Sex.UNKNOWN) {
                if (gender == UserProfile.Sex.MALE) gender = UserProfile.Sex.FEMALE
                else if (gender == UserProfile.Sex.FEMALE) gender = UserProfile.Sex.MALE
                members = members.filter { it.queryProfile().sex == gender }
            }
        }

        if (!PluginConfig.checkSelf) {
            members = members.filter { it.id != sender.id }
        }

        if (members.isEmpty()) return group.botAsMember
        return members.random()
    }
}

fun String.replace(replacements: Map<String, SingleMessage>) : MessageChain {
    if (!this.contains("\$")) return PlainText(this).toMessageChain()
    val keys = replacements.keys
    val message = MessageChainBuilder()
    val s = this.split("\\\$").toMutableList()
    message.add(s[0])
    s.removeAt(0)
    s.forEach {
        var text = it
        var isOriginal = true
        for (k in keys) {
            if (text.startsWith(k)) {
                text = text.substring(k.length)
                replacements[k]?.let { single -> message.add(single) }
                message.add(text)
                isOriginal = false
                break
            }
        }
        if (isOriginal) message.add("\$$text")
    }
    return message.build()
}