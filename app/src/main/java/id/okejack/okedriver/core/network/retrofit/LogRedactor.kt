package id.okejack.okedriver.core.network.retrofit

/** Keeps credentials out of HTTP logs; the backend still expects them in the query and body. */
object LogRedactor {
    private const val MASK = "<redacted>"
    private val queryOrFormValue = Regex("""\b(api_token|password)=[^&\s"]+""")
    private val jsonPassword = Regex(""""password"\s*:\s*"(?:\\.|[^"\\])*"""")

    fun redact(message: String): String =
        message
            .replace(queryOrFormValue) { "${it.groupValues[1]}=$MASK" }
            .replace(jsonPassword, "\"password\":\"$MASK\"")
}
