import com.github.purofle.quotebot.render.QuoteDraw
import org.drinkless.tdlib.TdApi
import java.nio.file.Files
import kotlin.io.path.Path

fun main() {
    val stubMessage = TdApi.Message().apply {
        content = TdApi.MessageText().apply {
            text = TdApi.FormattedText().apply {
                text = "Hello, world!"
            }
            forwardInfo = TdApi.MessageForwardInfo().apply {
                origin = TdApi.MessageOriginHiddenUser().apply {
                    senderName = "ACh Sulfate -Xzygote -Xusejit:true"
                }
            }
        }
    }
    val q = QuoteDraw(listOf(stubMessage), font = "C:\\Windows\\Fonts\\msyh.ttc")
    val data = q.encodeWebp()
    Files.write(Path("out.webp"), data.bytes)
}
