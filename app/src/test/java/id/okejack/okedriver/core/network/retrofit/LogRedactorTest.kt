package id.okejack.okedriver.core.network.retrofit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LogRedactorTest {
    @Test fun apiTokenInQueryIsRedactedWhateverItsPosition() {
        assertEquals(
            "--> POST https://api.okejack.id/v2/driver/update-location?api_token=<redacted>",
            LogRedactor.redact("--> POST https://api.okejack.id/v2/driver/update-location?api_token=NyXhlJakabF7xfJJGes201dxi6Suz7il")
        )
        assertEquals(
            "https://x/y?page=2&api_token=<redacted>&limit=10",
            LogRedactor.redact("https://x/y?page=2&api_token=abc123&limit=10")
        )
    }

    @Test fun passwordInJsonBodyIsRedacted() {
        val redacted = LogRedactor.redact("""{"email":"driver@okejack.id","password":"rahasia \"123\"","x":1}""")

        assertFalse(redacted.contains("rahasia"))
        assertEquals("""{"email":"driver@okejack.id","password":"<redacted>","x":1}""", redacted)
    }

    @Test fun tokenAndPasswordInFormBodiesAreRedacted() {
        assertEquals("email=a%40b.c&password=<redacted>", LogRedactor.redact("email=a%40b.c&password=p%40ss"))
        assertEquals("api_token=<redacted>", LogRedactor.redact("api_token=abc"))
    }

    @Test fun otherLogLinesAreUntouched() {
        val line = "<-- 200 https://api.okejack.id/v2/driver/orders (210ms)"

        assertEquals(line, LogRedactor.redact(line))
    }
}
