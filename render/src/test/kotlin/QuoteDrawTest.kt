import com.github.purofle.quotebot.render.QuoteDraw
import com.github.purofle.quotebot.tdlibhelper.QuoteUser
import org.drinkless.tdlib.TdApi
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.readBytes

fun main() {
    val stubMessage = listOf(TdApi.Message().apply {
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
    }, TdApi.Message().apply {
        content = TdApi.MessageText().apply {
            text = TdApi.FormattedText().apply {
                text = "Hello, world2!"
            }
            forwardInfo = null
        }
    })

    val stubQuoteUser = listOf(
        QuoteUser(
            id = 123456789L,
            fullName = "ACh Sulfate -Xzygote -Xusejit:true",
            avatar = Path("render/src/test/resources/avatar.jpg").readBytes()
        ),
        QuoteUser(
            id = 123456788L,
            fullName = "日落果",
            avatar = Path("render/src/test/resources/avatar.jpg").readBytes()
        )
    )
    val q = QuoteDraw(
        stubQuoteUser.zip(stubMessage).toMap(),
        fontFile = "C:\\Windows\\Fonts\\HarmonyOS_Sans_SC_Regular.ttf"
    )
    val data = q.encodeWebp()
    Files.write(Path("out.webp"), data.bytes)
}
