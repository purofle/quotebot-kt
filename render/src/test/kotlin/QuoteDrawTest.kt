import com.github.purofle.quotebot.render.QuoteDraw
import java.nio.file.Files

fun main() {
    val q = QuoteDraw("我草")
    val data = q.encodeWebp(512, 256, 90)
    Files.write(java.nio.file.Path.of("out.webp"), data.bytes)
}
