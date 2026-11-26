package id.okejack.okedriver.core.database

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Migrations can only be tested against committed schemas, so every database version needs one. */
class RoomSchemaExportTest {
    private val source = File("src/main/java/id/okejack/okedriver/core/database/OkeDriverDatabase.kt").readText()
    private val schemaDir = File("schemas/id.okejack.okedriver.core.database.OkeDriverDatabase")

    @Test fun databaseExportsItsSchema() {
        assertTrue("OkeDriverDatabase must set exportSchema = true", Regex("""exportSchema\s*=\s*true""").containsMatchIn(source))
    }

    @Test fun everyDatabaseVersionHasACommittedSchema() {
        val current = Regex("""version\s*=\s*(\d+)""").find(source)?.groupValues?.get(1)?.toInt()
            ?: error("No version found in OkeDriverDatabase")

        for (version in 1..current) {
            assertTrue("Missing schema ${schemaDir.path}/$version.json", File(schemaDir, "$version.json").isFile)
        }
    }
}
