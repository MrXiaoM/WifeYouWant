package top.mrxiaom

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import top.mrxiaom.WifeYouWant.reload

object PluginCommand : SimpleCommand(
    owner = WifeYouWant,
    primaryName = "wifeyouwant",
    secondaryNames = arrayOf("wuw"),
    description = "变态渣男插件"
) {
    @Handler
    suspend fun CommandSender.handle(operation: String) {
        if (operation.equals("reload", true)) {
            PluginConfig.reload()
            sendMessage(PluginConfig.msgReload)
        }
    }

    @Handler
    suspend fun CommandSender.handle() {
        sendMessage(java.lang.String.join("\n", PluginConfig.msgHelp))
    }
}