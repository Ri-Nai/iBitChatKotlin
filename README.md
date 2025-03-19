# iBitChat Kotlin

这是一个使用Kotlin实现的北理工(BIT)聊天API客户端，基于Deepseek API流式传输聊天功能。

## 功能特点

- 基于Kotlin协程的异步流式传输聊天
- 简单易用的API接口
- 支持历史消息上下文
- 无需登录，使用现有cookie直接访问
- 增强的错误处理机制
- 兼容Java 8运行环境
- 支持从配置文件加载参数

## 项目结构

```
src/main/kotlin/com/ibit/chat/
├── api/                   # API客户端实现
│   └── IBitChatClient.kt  # 主要API实现
├── model/                 # 数据模型
│   ├── Message.kt         # 消息模型
│   ├── ChatRequest.kt     # 聊天请求模型
│   └── ...
├── util/                  # 工具类
│   └── ConfigLoader.kt    # 配置加载工具
└── Main.kt                # 测试主函数
```

## 快速开始

### 前提条件

- JDK 8 或更高版本
- Gradle 7.0 或更高版本

### 配置

1. 复制 `local.properties.example` 文件为 `local.properties`
2. 在 `local.properties` 中填入你的 badge 和 cookie 值:

```properties
badge=your_badge_value_here
cookie=badge_2=your_badge_value_here%3D
```

### 构建项目

```bash
# 克隆项目
git clone https://github.com/yourusername/iBitChatKotlin.git
cd iBitChatKotlin

# 构建项目
./gradlew build
```

### 运行演示程序

```bash
# 构建并运行
./gradlew run
```

程序会自动读取配置文件中的badge和cookie，然后你可以开始聊天。

### 使用示例

```kotlin
import com.ibit.chat.IBitChat
import com.ibit.chat.model.Message
import com.ibit.chat.util.ConfigLoader
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // 加载配置
    ConfigLoader.load()
    val badge = ConfigLoader.getBadge()
    val cookie = ConfigLoader.getCookie()
    
    // 创建客户端
    val chatClient = IBitChat(badge, cookie)
    
    // 准备消息列表
    val messages = listOf(
        Message(role = "user", content = "你好，请介绍一下自己")
    )
    
    // 流式接收回复并处理错误
    chatClient.chatStream(messages)
        .catch { e -> println("发生错误: ${e.message}") }
        .collect { chunk ->
            print(chunk) // 打印每个回复片段
        }
}
```

## 参数说明

### Message

| 参数名 | 类型 | 描述 |
| --- | --- | --- |
| role | String | 消息角色，可以是 "user", "assistant" 或 "system" |
| content | String | 消息内容 |

### chatStream 方法

| 参数名 | 类型 | 默认值 | 描述 |
| --- | --- | --- | --- |
| messages | List<Message> | - | 消息列表，最后一条必须是用户消息 |
| temperature | Double | 0.7 | 温度参数，控制回复的随机性 |
| topK | Int | 3 | Top-K参数 |
| scoreThreshold | Double | 0.5 | 分数阈值 |

## 错误处理

库提供了多层次的错误处理机制：

1. API调用层面的错误处理 - 请求失败时会抛出异常
2. Flow流处理的错误捕获 - 使用catch操作符处理流中的错误
3. 内容过滤 - 过滤掉"<think>"思考过程和空内容

使用时推荐添加自己的错误处理逻辑：

```kotlin
chatClient.chatStream(messages)
    .catch { e -> 
        // 处理错误
        println("聊天出错: ${e.message}")
    }
    .collect { ... }
```

## 注意事项

- 本项目仅供学习和研究使用
- 需要自行获取有效的badge和cookie，本项目不提供登录功能
- API可能随时变更，请注意更新
- 使用Java 8运行时，确保使用兼容的依赖版本
- 敏感信息（如badge和cookie）应保存在local.properties中且不要提交到版本控制系统

## 致谢

本项目参考了以下开源项目：
- [open_ibit](https://github.com/yht0511/open_ibit)
- [BIT_Deepseek_API](https://github.com/5Breeze/BIT_Deepseek_API)

感谢这些项目的贡献者提供的宝贵参考和灵感。
