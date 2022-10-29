package top.mrxiaom

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import top.mrxiaom.WifeYouWant.nowTime

object UserData : AutoSavePluginData("users") {
    val users: MutableMap<Long, SingleUser> by value()

    operator fun get(id: Long): SingleUser? {
        val time = nowTime
        val wife = users[id] ?: users.values.firstOrNull { it.wifeId == id && it.time == time }
        if (wife?.time != time) return null
        return wife
    }
}

@kotlinx.serialization.Serializable
class SingleUser(var wifeId: Long = 0, var time: String = "")
