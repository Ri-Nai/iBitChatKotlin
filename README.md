# iBitChat Kotlin

这是一个使用Kotlin实现的北理工(BIT)聊天API客户端，基于Deepseek API流式传输聊天功能。

## 功能特点

- 基于Kotlin协程的异步流式传输聊天
- 多种登录方式支持：
  - 使用已有的badge和cookie直接访问
  - 通过北理工统一身份认证（学号密码）登录
- 支持历史消息上下文（最近5轮对话）
- 简单易用的API接口
- 增强的错误处理机制
- 兼容Java 8运行环境
- 支持从配置文件加载参数

## 项目结构

```
src/main/kotlin/com/ibit/chat/
├── chat/                       # 聊天功能实现
│   ├── IBitChatClient.kt       # 主要API实现
│   └── BITLoginApi.kt          # 统一身份认证登录实现
├── model/                      # 数据模型
│   ├── Message.kt              # 消息模型
│   ├── ChatRequest.kt          # 聊天请求模型
│   └── ...                     # 其他模型
├── config/                     # 配置文件
│   └── ConfigLoader.kt         # 配置加载工具
├── login/                      # 登录功能实现
│    ├── BITLoginService.kt     # 统一身份认证登录实现
│    ├── http/                  # HTTP相关实现
│    │   ├── CookieJarImpl.kt   # Cookie处理实现
│    │   └── HttpClient.kt      # HTTP客户端
│    └── util/                  # 工具类
│        └── AESUtils.kt        # AES加密工具
└── Main.kt                     # 测试主函数

```

### 前提条件

- JDK 8 或更高版本
- Gradle 7.0 或更高版本

### 配置

1. 复制 `local.properties.example` 文件为 `local.properties`
2. 在 `local.properties` 中可以配置以下参数：

```properties
# 方式1：使用badge和cookie（优先使用）
badge=your_badge_value_here
cookie=badge_2=your_badge_value_here%3D

# 方式2：使用学号密码
username=your_student_id
password=your_password
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

程序会按以下顺序尝试登录：
1. 首先尝试使用配置文件中的badge和cookie
2. 如果未找到badge和cookie，则尝试使用配置文件中的学号密码
3. 如果以上都未配置，将提示手动输入学号密码

### 使用示例

```kotlin
import com.ibit.chat.api.IBitChatClient
import com.ibit.chat.api.BITLoginApi
import com.ibit.chat.model.Message
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // 方式1：使用badge和cookie
    val client = IBitChatClient(badge, cookie)
    
    // 方式2：使用学号密码登录
    val loginApi = BITLoginApi()
    val client = loginApi.login(username, password)
    
    // 准备历史消息
    val history = mutableListOf(
        Message(role = "user", content = "你好，请介绍一下自己")
    )
    
    // 发送消息并流式接收回复
    client.chatStream("你好", history)
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

| 参数名 | 类型 | 描述 |
| --- | --- | --- |
| query | String | 用户的问题内容 |
| history | List<Message> | 历史消息列表（可选） |

## 错误处理

库提供了多层次的错误处理机制：

1. 登录相关错误处理
   - 统一身份认证登录失败
   - Cookie/Badge无效或过期
2. API调用层面的错误处理
   - 网络请求失败
   - 服务器响应异常
3. 聊天流处理的错误捕获
   - 使用Kotlin Flow的catch操作符处理流中的错误
   - 自动重试机制

使用示例：

```kotlin
try {
    client.chatStream(query, history)
        .catch { e -> 
            // 处理流错误
            println("聊天出错: ${e.message}")
        }
        .collect { chunk ->
            // 处理响应
            print(chunk)
        }
} catch (e: Exception) {
    // 处理其他错误
    println("系统错误: ${e.message}")
}
```

## 注意事项

- 本项目仅供学习和研究使用
- 请勿频繁进行登录操作，避免账号被限制
- 敏感信息（如学号、密码、badge和cookie）应保存在local.properties中且不要提交到版本控制系统
- API可能随时变更，请注意更新
- 使用Java 8运行时，确保使用兼容的依赖版本

## 致谢

本项目参考了以下开源项目：
- [open_ibit](https://github.com/yht0511/open_ibit)
- [BIT_Deepseek_API](https://github.com/5Breeze/BIT_Deepseek_API)
- [BIT101-Android](https://github.com/BIT101-dev/BIT101-Android)

感谢这些项目的贡献者提供的宝贵参考和灵感。

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。
