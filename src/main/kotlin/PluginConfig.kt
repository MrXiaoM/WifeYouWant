package top.mrxiaom

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.ValueName
import net.mamoe.mirai.console.data.value

object PluginConfig : ReadOnlyPluginConfig("config") {
    @ValueName("enable-groups")
    @ValueDescription("""
        启用本插件的群聊。填写规则如下
        enable-groups:
        - 群号1
        - 群号2
        - 以此类推
    """)
    val enableGroups by value(listOf<Long>())

    @ValueName("enable-bots")
    @ValueDescription("""
        启用本插件的机器人。如果你只登录了1个机器人，那么不需要去管这个配置。
        本配置针对登录了多机器人的 mirai 使用，【留空默认为所有机器人可用】。
        填写规则如下。除了这个配置以外，权限或者 enable-groups 依然是要单独去配置的。
        enable-bots:
        # 允许机器人 12345 在所有群使用该插件
        - '12345:*'
        # 允许机器人 12345 在群 114514 使用该插件
        - '12345:114514'
        # 不允许机器人 12345 在群 114514 使用该插件
        - '12345:-114514'
    """)
    val enableBots by value(listOf<String>())

    @ValueName("cooldown")
    @ValueDescription("抽老婆/换老婆命令冷却时间，单位为秒。两个命令共用一个冷却时间，设置为0或者负数时禁用冷却时间")
    val cooldown by value(0)
    @ValueName("cooldown-all-bots")
    @ValueDescription("是否所有机器人共用一个冷却时间组")
    val cooldownAllBots by value(true)
    @ValueName("cooldown-all-groups")
    @ValueDescription("是否所有群聊共用一个冷却时间组")
    val cooldownAllGroups by value(true)
    @ValueName("cooldown-all-members")
    @ValueDescription("是否所有群员共用一个冷却时间")
    val cooldownAllMembers by value(true)
    @ValueName("cooldown-message")
    @ValueDescription("冷却中消息，\$cooldown 代表剩余秒数")
    val cooldownMessage by value("\$at 正在冷却中 (\$cooldown秒)")

    @ValueName("active-member-only")
    @ValueDescription("随机选择时仅包含最近活跃的成员")
    val activeMemberOnly by value(false)

    @ValueName("member-active-time")
    @ValueDescription("群成员活跃状态有效期,单位秒")
    val memberActiveTime by value(7 * 24 * 60 * 60)

    @ValueName("blacklist-only")
    @ValueDescription("开启后机器人会响应所有群的命令，在黑名单内的群除外")
    val blacklistOnly by value(false)

    @ValueName("blacklist-groups")
    @ValueDescription("在本插件黑名单内的群聊")
    val blacklistGroups by value(listOf<Long>())

    @ValueName("keywords-random-wife")
    @ValueDescription("「抽老婆」的关键词列表")
    val keywordsRandomWife by value(listOf("抽老婆"))

    @ValueName("keywords-change-wife")
    @ValueDescription("「换老婆」的关键词列表")
    val keywordsChangeWife by value(listOf("换老婆"))

    @ValueName("keywords-wife-list-group")
    @ValueDescription("「群老婆列表」的关键词列表")
    val keywordsWifeListGroup by value(listOf("群老婆列表"))

    @ValueName("keywords-wife-list-all")
    @ValueDescription("「老婆列表」的关键词列表")
    val keywordsWifeListAll by value(listOf("老婆列表"))

    @ValueName("wife-list-font")
    @ValueDescription("老婆列表图片字体")
    val wifeListFont by value("宋体")

    @ValueName("message-random-wife")
    @ValueDescription(
        "「抽老婆」的回复语句列表\n" +
                "其中你的变量有 \$at \$pic \$namecard \$nick \$qq\n" +
                "其中老婆的变量有 \$wife_at \$wife_pic \$wife_namecard \$wife_nick \$wife_qq"
    )
    val messagesRandomWife by value(listOf("\$at 今天你的群友亲爱的是 \$wife_pic\n【\$wife_namecard】(\$wife_qq)哒！"))

    @ValueName("messages-change-wife")
    @ValueDescription(
        "「换老婆」的回复语句列表\n" +
                "在抽老婆的变量基础上，旧老婆的变量有 \$old_wife_at \$old_wife_pic \$old_wife_namecard \$old_wife_nick \$old_wife_qq"
    )
    val messagesChangeWife by value(listOf("\$at 今天你的群友亲爱的是 \$wife_pic\n【\$wife_namecard】(\$wife_qq)哒！"))

    @ValueName("check-gender")
    @ValueDescription("""
        在抽老婆时是否检查性别 (避免大量请求群员资料导致卡顿，尽量不要开启)
        开启后只抽性别与用户相反的群友
        当群友不公开性别时，该选项失效
        开启本选项会在抽老婆时忽略掉所有不公开性别的群友
        """)
    val checkGender by value(false)

    @ValueName("check-gender-same")
    @ValueDescription("""
        同 check-gender，与 check-gender 冲突，两者都开启时，check-gender 失效，该选项生效。
        开启这个选项后，只能抽到性别与用户相同的群友
    """)
    val checkGenderSame by value(false)

    @ValueName("check-self")
    @ValueDescription("在抽老婆时是否不允许抽到自己")
    val checkSelf by value(true)

    @ValueName("check-ntr")
    @ValueDescription("在抽老婆时是否不允许抽到重复的")
    val checkNTR by value(true)

    @ValueName("message-help")
    @ValueDescription("插件帮助")
    val msgHelp by value(listOf("变态渣男插件 - 帮助", "  /wuw reload - 重载配置文件"))

    @ValueName("message-reload")
    @ValueDescription("插件重载提示")
    val msgReload by value("配置文件已重载")
}