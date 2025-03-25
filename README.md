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
├── service/                    # 服务层实现
│   ├── ServiceManager.kt       # 服务管理器
│   ├── base/                  # 基础服务实现
│   └── util/                  # 工具类
├── network/                   # 网络相关实现
│   └── ...                    # 网络请求相关类
├── model/                     # 数据模型
│   ├── Message.kt             # 消息模型
│   └── ...                    # 其他模型
├── config/                    # 配置文件
│   └── ConfigLoader.kt        # 配置加载工具
└── Main.kt                    # 测试主函数

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
import com.ibit.chat.model.Message
import com.ibit.chat.service.ServiceManager
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // 初始化配置（如果需要）
    val properties = loadProperties(File("local.properties"))
    if (!isConfigValid(properties)) {
        initializeConfig(properties, File("local.properties"))
    }

    // 获取用户名和密码
    val username = properties.getProperty("username")
    val password = properties.getProperty("password")

    // 执行登录
    ServiceManager.loginService.login(username, password)

    // 准备消息历史
    var messages = listOf<Message>()

    // 发送消息并流式接收回复
    while (true) {
        val message = readln("请输入消息（输入exit退出）： ")
        if (message == "exit") break

        messages += Message(role = "user", content = message)
        var totalContent = ""
        
        ServiceManager.iBitService.streamChat(messages).collect { result ->
            result.onSuccess {
                totalContent += it
                print(it)
            }
            result.onFailure {
                println("聊天失败: ${it.message}")
            }
        }
        
        messages += Message(role = "assistant", content = totalContent)
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
