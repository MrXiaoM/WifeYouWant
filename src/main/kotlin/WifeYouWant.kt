package top.mrxiaom

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.info
import top.mrxiaom.PrepareUploadImage.Companion.prepareUploadAvatarImage
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

        PluginCommand.register()
        this.globalEventChannel().subscribeAlways<GroupMessageEvent> {
            if (PluginConfig.blacklistOnly && PluginConfig.blacklistGroups.contains(group.id)) return@subscribeAlways
            else if (!PluginConfig.enableGroups.contains(group.id)) return@subscribeAlways
            val sender = if (it.sender is NormalMember) it.sender as NormalMember else return@subscribeAlways

            if (PluginConfig.messagesRandomWife.isNotEmpty() && PluginConfig.keywordsRandomWife.contains(it.message.content)) {
                val time = nowTime
                val user = UserData.users.getOrDefault(sender.id, SingleUser())
                if (user.time != time) user.wifeId = random(sender).id
                val wife: User = group[user.wifeId] ?: bot.getMember(user.wifeId) ?: bot.getStranger(user.wifeId) ?: random(sender)
                user.wifeId = wife.id
                user.time = time
                UserData.users[sender.id] = user
                val s = PluginConfig.messagesRandomWife.random()
                group.sendMessage(genRandomWifeMessage(s, sender, wife))
            } else if (PluginConfig.messagesChangeWife.isNotEmpty() && PluginConfig.keywordsChangeWife.contains(it.message.content)) {
                val time = nowTime
                val user = UserData.users.getOrDefault(sender.id, SingleUser())
                val oldWife = group[user.wifeId] ?: bot.getMember(user.wifeId) ?: bot.getStranger(user.wifeId) ?: sender
                val wife = random(sender, oldWife.id)
                user.wifeId = wife.id
                user.time = time
                UserData.users[sender.id] = user
                val s = PluginConfig.messagesChangeWife.random()
                group.sendMessage(genChangeWifeMessage(s, sender, oldWife, wife))
            }
        }
    }

    private fun Bot.getMember(id: Long) : NormalMember?{
        return groups.asSequence().flatMap { it.members.asSequence() }.firstOrNull { it.id == id }
    }

    private fun genUserReplacement(
        identity: String,
        sender: User,
        connect: String = "_"
    ): MutableMap<String, SingleMessage> {
        val prefix = identity + connect
        return mutableMapOf(
            "${prefix}at" to At(sender.id), "${prefix}namecard" to PlainText(sender.nameCardOrNick),
            "${prefix}nick" to PlainText(sender.nick), "${prefix}qq" to PlainText(sender.id.toString()),
            "${prefix}pic" to sender.prepareUploadAvatarImage()
        )
    }
    private suspend fun genRandomWifeMessage(
        s: String,
        sender: NormalMember,
        wife: User
    ): MessageChain {
        val map = genUserReplacement("", sender, "")
        map.putAll(genUserReplacement("wife", wife))
        return s.replace(map)
    }

    private suspend fun genChangeWifeMessage(
        s: String,
        sender: NormalMember,
        oldWife: User,
        wife: User
    ): MessageChain {
        val map = genUserReplacement("", sender, "")
        map.putAll(genUserReplacement("wife", wife))
        map.putAll(genUserReplacement("old_wife", oldWife))
        return s.replace(map)
    }

    /**
     * 随机获取一个老婆，在抽不到人的时候返回机器人自身
     * @param sender 请求者
     * @param excludeId 排除群员qq (换老婆时需要)
     */
    private suspend fun random(sender: NormalMember, excludeId: Long? = null): NormalMember {
        val group = sender.group
        var members: List<NormalMember> = group.members.filter { it.id != group.bot.id && it.id != excludeId }

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

        if (PluginConfig.checkNTR) {
            val existsWives = UserData.users.map { it.value.wifeId }
            members.filter { !existsWives.contains(it.id) }
        }

        if (members.isEmpty()) return group.botAsMember
        return members.random()
    }
}