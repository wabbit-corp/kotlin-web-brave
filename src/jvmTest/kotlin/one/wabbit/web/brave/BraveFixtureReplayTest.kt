package one.wabbit.web.brave

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlinx.serialization.json.Json
import kotlin.io.path.extension
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class BraveFixtureReplayTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun decodesAllRecordedBraveFixtures() {
        val fixtures = fixtureFiles()
        assertTrue(fixtures.isNotEmpty(), "expected at least one recorded Brave fixture")

        val failures = mutableListOf<String>()
        fixtures.forEach { fixture ->
            val body = Files.readString(fixture)
            runCatching {
                json.decodeFromString<SearchResponse>(body)
            }.onFailure { error ->
                failures += "${fixture.fileName}: ${error::class.simpleName}: ${error.message}"
            }
        }

        if (failures.isNotEmpty()) {
            fail(
                buildString {
                    appendLine("Failed to decode ${failures.size} Brave fixture(s).")
                    failures.take(10).forEach(::appendLine)
                },
            )
        }
    }

    private fun fixtureFiles(): List<Path> =
        Files.list(Paths.get("src/jvmTest/resources/brave"))
            .use { stream ->
                stream
                    .filter { Files.isRegularFile(it) && it.extension == "json" }
                    .sorted()
                    .toList()
            }
}
