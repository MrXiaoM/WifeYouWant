package top.mrxiaom

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.ValueName
import net.mamoe.mirai.console.data.value

object PluginConfig : ReadOnlyPluginConfig("config") {
    @ValueName("enable-groups")
    @ValueDescription("启用本插件的群聊")
    val enableGroups by value(listOf<Long>())
    @ValueName("keywords-random-wife")
    @ValueDescription("「抽老婆」的关键词列表")
    val keywordsRandomWife by value(listOf("抽老婆"))
    @ValueName("keywords-change-wife")
    @ValueDescription("「换老婆」的关键词列表")
    val keywordsChangeWife by value(listOf("换老婆"))
    @ValueName("message-random-wife")
    @ValueDescription("「抽老婆」的回复语句列表\n" +
            "其中你的变量有 @you \$pic \$namecard \$nick \$qq\n" +
            "其中老婆的变量有 @wife \$wife_pic \$wife_namecard \$wife_nick \$wife_qq")
    val messagesRandomWife by value(listOf("@you 今天你的群友亲爱的是 \$pic\n【\$nick】(\$qq)哒！"))
    @ValueName("messages-change-wife")
    @ValueDescription("「换老婆」的回复语句列表\n" +
            "在抽老婆的变量基础上，旧老婆的变量有 @old_wife \$old_wife_pic \$old_wife_namecard \$old_wife_nick \$old_wife_qq")
    val messagesChangeWife by value(listOf("@you 今天你的群友亲爱的是 \$pic\n【\$nick】(\$qq)哒！"))
    @ValueName("check-gender")
    @ValueDescription("在抽老婆时是否检查性别 (避免大量请求群员资料导致卡顿，尽量不要开启)\n" +
            "开启后只抽性别与用户相反的群友\n" +
            "当群友不公开性别时，该选项失效\n" +
            "开启本选项会在抽老婆时忽略掉所有不公开性别的群友")
    val checkGender by value(false)
    @ValueName("check-self")
    @ValueDescription("在抽老婆时是否允许抽到自己")
    val checkSelf by value(true)
    @ValueName("message-help")
    @ValueDescription("插件帮助")
    val msgHelp by value(listOf("变态渣男插件 - 帮助", " /wuw reload - 重载配置文件"))
    @ValueName("message-reload")
    @ValueDescription("插件重载提示")
    val msgReload by value("配置文件已重载")
}