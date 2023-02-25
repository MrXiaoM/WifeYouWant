package top.mrxiaom

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.console.permission.PermitteeId
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import top.mrxiaom.PrepareUploadImage.Companion.prepareUploadAvatarImage
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO
import kotlin.math.roundToInt

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
    private lateinit var PERM_USE: Permission
    private lateinit var PERM_CHECK_GROUP: Permission
    private lateinit var PERM_CHECK_ALL: Permission
    val nowTime: String
        get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    override fun onEnable() {
        PERM_USE = PermissionService.INSTANCE.register(PermissionId(id, "use"), "使用抽老婆/换老婆命令")
        PERM_CHECK_GROUP = PermissionService.INSTANCE.register(PermissionId(id, "check.group"), "使用群老婆列表命令")
        PERM_CHECK_ALL = PermissionService.INSTANCE.register(PermissionId(id, "check.all"), "使用老婆列表命令")

        PluginConfig.reload()
        UserData.reload()

        PluginCommand.register()
        this.globalEventChannel().subscribeAlways<GroupMessageEvent> { it ->
            if (PluginConfig.blacklistOnly) {
                if (PluginConfig.blacklistGroups.contains(group.id)) return@subscribeAlways
            } else if (!PluginConfig.enableGroups.contains(group.id) && !anyHasPerm(
                    PERM_USE,
                    group,
                    sender
                )
            ) return@subscribeAlways
            val sender = if (it.sender is NormalMember) it.sender as NormalMember else return@subscribeAlways
            val time = nowTime

            if (PluginConfig.messagesRandomWife.isNotEmpty() && PluginConfig.keywordsRandomWife.contains(it.message.content)) {
                val user = UserData[sender.id] ?: SingleUser()
                if (user.time != time) user.wifeId = random(sender).id
                val wife: User =
                    group[user.wifeId] ?: bot.getMember(user.wifeId) ?: bot.getStranger(user.wifeId) ?: random(sender)
                user.wifeId = wife.id
                user.time = time
                UserData.users[sender.id] = user
                val s = PluginConfig.messagesRandomWife.random()
                group.sendMessage(genRandomWifeMessage(s, sender, wife))
            } else if (PluginConfig.messagesChangeWife.isNotEmpty() && PluginConfig.keywordsChangeWife.contains(it.message.content)) {
                val user = UserData[sender.id] ?: SingleUser()
                val oldWife = group[user.wifeId] ?: bot.getMember(user.wifeId) ?: bot.getStranger(user.wifeId) ?: sender
                val wife = random(sender, oldWife.id)
                user.wifeId = wife.id
                user.time = time
                UserData.users[sender.id] = user
                val s = PluginConfig.messagesChangeWife.random()
                group.sendMessage(genChangeWifeMessage(s, sender, oldWife, wife))
            } else if (PluginConfig.keywordsWifeListAll.contains(it.message.content)) {
                var list = UserData.users.filter { it.value.time == time }.map { entry ->
                    Pair(entry.key, entry.value.wifeId)
                }
                // 去重
                list = list.filter { one ->
                    list.all { two ->
                        two.first != one.second
                    }
                }
                val msg = "老婆列表:\n" + list.joinToString("\n") { couple ->
                    val first = couple.first.toString() + " " + (group[couple.first]?.nameCardOrNick ?: "").limit(10)
                    val second = couple.second.toString() + " " + (group[couple.second]?.nameCardOrNick ?: "").limit(10)
                    "$first <--> $second"
                }
                group.sendForwardOrPlain(msg)

            } else if (PluginConfig.keywordsWifeListGroup.contains(it.message.content)) {
                var list = UserData.users.filter { it.value.time == time }.map { entry ->
                    Pair(entry.key, entry.value.wifeId)
                }
                list = list.filter {
                    group.contains(it.first) && group.contains(it.second)
                }
                // 去重
                list = list.filter { one ->
                    list.all { two ->
                        two.first != one.second
                    }
                }
                val msg = "群老婆列表:\n" + list.joinToString("\n") { couple ->
                    val first = couple.first.toString() + " " + (group[couple.first]?.nameCardOrNick ?: "").limit(10)
                    val second = couple.second.toString() + " " + (group[couple.second]?.nameCardOrNick ?: "").limit(10)
                    "$first <--> $second"
                }
                group.sendForwardOrPlain(msg)
            }
        }
        logger.info { "Plugin loaded" }
    }

    private suspend fun Contact.sendForwardOrPlain(s: String) {
        try{
            sendMessage(PlainText(s).toForwardMessage(bot.asFriend))
        } catch (_: IllegalStateException) {
            val font = Font(PluginConfig.wifeListFont, Font.PLAIN, 16)
            val fontHeight = 20
            val width = 800
            val height = 10 + (fontHeight) * (s.count { it == '\n' } + 2)
            val image = BufferedImage(width, height, TYPE_INT_RGB)
            val g = image.createGraphics()
            g.color = Color.WHITE
            g.fillRect(0, 0, width, height)
            g.color = Color.BLACK
            g.font = font
            s.split("\n").forEachIndexed { i, str ->
                g.drawString(str, 5,5 + fontHeight * (i + 1))
            }
            g.dispose()
            withContext(Dispatchers.IO) {
                val outputStream = ByteArrayOutputStream()
                ImageIO.write(image, "png", outputStream)
                val img = uploadImage(outputStream.toByteArray().also { outputStream.close() }.toExternalResource().toAutoCloseable())
                sendMessage(img)
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
            "${prefix}at" to At(sender.id),
            "${prefix}namecard" to PlainText(sender.nameCardOrNick),
            "${prefix}nick" to PlainText(sender.nick),
            "${prefix}qq" to PlainText(sender.id.toString()),
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
        var members: List<NormalMember> = group.members.filter { it.id != excludeId }

        if (PluginConfig.activeMemberOnly) {
            val timeLim = System.currentTimeMillis() / 1000 - PluginConfig.memberActiveTime
            members = members.filter { it.lastSpeakTimestamp >= timeLim }
        }

        if (PluginConfig.checkGender) {
            var gender = sender.queryProfile().sex
            if (gender != UserProfile.Sex.UNKNOWN) {
                if (gender == UserProfile.Sex.MALE) gender = UserProfile.Sex.FEMALE
                else if (gender == UserProfile.Sex.FEMALE) gender = UserProfile.Sex.MALE
                members = members.filter { it.queryProfile().sex == gender }
            }
        }

        if (!PluginConfig.checkSelf) {
            members = members.toMutableList().also {
                it.add(sender.group.botAsMember)
            }
        }

        if (PluginConfig.checkNTR) {
            val time = nowTime
            val existsWives = mutableSetOf<Long>().also{ set ->
                set.addAll(UserData.users.keys)
                set.addAll(UserData.users.values.filter { time == nowTime }.map { it.wifeId })
            }
            members = members.filter { !existsWives.contains(it.id) }
        }

        if (members.isEmpty()) return group.botAsMember
        return members.random()
    }
}

val Contact.permitteeIdOrNull: PermitteeId?
    get() = when (this) {
        is User -> this.permitteeId
        is Group -> this.permitteeId
        else -> null
    }

fun anyHasPerm(p: Permission, vararg users: Contact): Boolean = users.any {
    p.testPermission(it.permitteeIdOrNull ?: return@any false)
}

fun String.limit(lim: Int, suffix: String="…"): String = if (length <= lim) this else substring(0, lim) + suffix