package top.mrxiaom

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object UserData : AutoSavePluginData("users") {
    val users: MutableMap<Long, SingleUser> by value()
}

@kotlinx.serialization.Serializable
class SingleUser (var wifeId: Long = 0, var time: String = "")
