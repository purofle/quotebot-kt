# QuoteBot-kt

尝试用 Kotlin 重写的 [quote-bot](https://github.com/LyoSU/quote-bot)。

## 编译 & 运行

需要手动编译带有 JNI 的 [tdlib](https://tdlib.github.io/td/build.html?language=Java) 并将生成的动态链接库位置添加到 `java.library.path` 中，或手动设置 `java.library.path` 指向动态链接库的所在目录。

运行需要设置以下环境变量：
- `TELEGRAM_BOT_TOKEN`: Telegram 机器人的 Token。
- `TELEGRAM_API_ID`: Telegram API 的 ID。
- `TELEGRAM_API_HASH`: Telegram API 的 Hash。

Telegram API 需要前往 [my.telegram.org](https://my.telegram.org) 创建应用以获取 API ID 和 Hash。

## 实现进度
### Bot 基础部分
- [x] 获取消息
- [ ] 贴纸包管理（`/qs`）
### 绘制部分

- [x] 绘制基本文字
- [ ] 绘制头像，处理大小变换
- [ ] 处理 Markdown 格式
- [ ] 处理图片与贴纸
- [ ] 处理回复消息
### 贴纸包管理部分
- [ ] 创建贴纸包
- [ ] 添加贴纸到贴纸包
- [ ] 从贴纸包删除贴纸

其他：咕咕咕

写的很烂，欢迎 PR/Issue 指正。