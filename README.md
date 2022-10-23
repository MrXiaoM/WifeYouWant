# WifeYouWant

> 「你想要的插件」系列作品

本插件移植自 椛椛 ᴮᴼᵀ ([名 场 面](https://mirai.mamoe.net/assets/uploads/files/1657708242332-wifeyouwant.png))

[![](https://shields.io/github/downloads/MrXiaoM/WifeYouWant/total)](https://github.com/MrXiaoM/WifeYouWant/releases) [![](https://img.shields.io/badge/mirai--console-2.12.3-blue)](https://github.com/mamoe/mirai) [![](https://img.shields.io/badge/MiraiForum-post-yellow)](https://mirai.mamoe.net/topic/1376)

## 特性

* 每天可以从群友里随机抽一次老婆 (渣男!)
* 不想要的可以换 (渣男!!)
* 可设置只能抽和自己性别相反的人，也可以无视性别
* 可以设置能抽到自己
* 可以设置能 NTR (重复抽到群友)
* 可以设置只抽到活跃群员

## 安装

到 [Releases](https://github.com/MrXiaoM/WifeYouWant/releases) 下载插件并放入 plugins 文件夹进行安装

> 2.11 或以上下载 WifeYouWant-*.mirai2.jar
>
> 2.11 以下下载 WifeYouWant-legacy-*.mirai.jar

安装完毕后，编辑配置文件作出你想要的修改。在控制台执行 `/wuw reload` 重载配置即可~

配置文件内有详细的注释，详见 [源码](src/main/kotlin/PluginConfig.kt)

## 用法

| 命令    | 权限                                  | 描述                         |
|:------|:------------------------------------|:---------------------------|
| 抽老婆   | top.mrxiaom.wifeyouwant:use         | 随机挑选一位群友，在明天之前，无论怎么抽都是那位群友 |
| 换老婆   | top.mrxiaom.wifeyouwant:use         | 抛弃老婆，重新抽一位群友               |
| 群老婆列表 | top.mrxiaom.wifeyouwant:check.group | 查看本群群友婚姻状况                 |
| 老婆列表  | top.mrxiaom.wifeyouwant:check.all   | 查看所有人婚姻状况，不在本群的将显示QQ号      |

可使用命令 `/perm permit g114514 权限` 来给群号为 114514 的群权限

所有命令均可在配置文件修改

## 编译

```
./gradlew buildPlugin buildPluginLegacy
```
