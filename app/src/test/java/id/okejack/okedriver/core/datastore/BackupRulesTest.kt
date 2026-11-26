package id.okejack.okedriver.core.datastore

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Session token and cached orders (customer data) must never leave the device through backup. */
class BackupRulesTest {
    private val xmlDir = File("src/main/res/xml")

    private fun excluded(file: String): List<Pair<String, String>> =
        Regex("""<exclude\s+domain="(\w+)"\s+path="([^"]*)"\s*/>""")
            .findAll(File(xmlDir, file).readText().replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), ""))
            .map { it.groupValues[1] to it.groupValues[2] }
            .toList()

    private fun assertSensitiveDataExcluded(rules: List<Pair<String, String>>, where: String) {
        assertTrue("$where must exclude datastore/", "file" to "datastore/" in rules)
        assertTrue("$where must exclude shared preferences", "sharedpref" to "." in rules)
        assertTrue("$where must exclude the Room database", "database" to "." in rules)
    }

    @Test fun legacyFullBackupExcludesSensitiveData() =
        assertSensitiveDataExcluded(excluded("backup_rules.xml"), "backup_rules.xml")

    @Test fun cloudBackupAndDeviceTransferExcludeSensitiveData() {
        val text = File(xmlDir, "data_extraction_rules.xml").readText().replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        for (section in listOf("cloud-backup", "device-transfer")) {
            val body = Regex("<$section[^>]*>(.*?)</$section>", RegexOption.DOT_MATCHES_ALL).find(text)?.groupValues?.get(1)
                ?: error("data_extraction_rules.xml has no <$section>")
            val rules = Regex("""<exclude\s+domain="(\w+)"\s+path="([^"]*)"\s*/>""").findAll(body)
                .map { it.groupValues[1] to it.groupValues[2] }.toList()
            assertSensitiveDataExcluded(rules, section)
        }
    }
}
