package one.wabbit.web.brave

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BraveLiveFuzzTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun liveRandomQueriesDecodeAndPersistFailingPayloads() {
        runBlocking {
            val config = loadLiveBraveConfigOrNull() ?: return@runBlocking
            val nouns = loadWordList("/wordlists/brave_live_nouns_top5000.txt")
            val verbs = loadWordList("/wordlists/brave_live_verbs_top5000.txt")
            val fixtureQueries = loadRecordedFixtureQueries()
            val queries = generateQueries(nouns, verbs, fixtureQueries, config.queryCount, config.seed)
            assertTrue(queries.isNotEmpty(), "expected at least one generated Brave query")

            val traces = mutableListOf<BraveApi.SearchTrace>()
            val client =
                HttpClient(CIO) {
                    install(HttpTimeout)
                }

            try {
                val api =
                    KtorBraveApi(
                        httpClient = client,
                        config =
                            BraveApi.Config(
                                subscriptionToken = config.apiKey,
                                onSearchTrace = { traces += it },
                            ),
                    )

                var successfulResponses = 0
                queries.forEachIndexed { index, query ->
                    println("Brave live query ${index + 1}/${queries.size}: $query")
                    try {
                        when (val response = api.search(query)) {
                            is WebSearchApiResponse -> successfulResponses += 1
                            is ErrorResponse -> {
                                val trace = traces.lastOrNull { it.query == query }
                                saveFailureArtifacts(
                                    query = query,
                                    index = index,
                                    trace = trace,
                                    message = "Decoded Brave error ${response.error.status} ${response.error.code}: ${response.error.detail}",
                                )
                                error("Brave returned an error response for \"$query\": ${response.error.status} ${response.error.code}")
                            }
                        }
                    } catch (t: Throwable) {
                        val trace = traces.lastOrNull { it.query == query }
                        saveFailureArtifacts(
                            query = query,
                            index = index,
                            trace = trace,
                            message = "${t::class.simpleName}: ${t.message}",
                        )
                        throw t
                    }
                }

                assertTrue(successfulResponses > 0, "expected at least one successful Brave web response")
            } finally {
                client.close()
            }
        }
    }

    private fun loadWordList(resourcePath: String): List<String> =
        javaClass.getResource(resourcePath)?.readText()
            ?.lineSequence()
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() && !it.startsWith("#") }
            ?.toList()
            ?: error("Missing word list resource: $resourcePath")

    private fun loadRecordedFixtureQueries(): Set<String> {
        val fixtureDir = Paths.get("src/jvmTest/resources/brave")
        if (!Files.exists(fixtureDir)) return emptySet()

        return Files.list(fixtureDir)
            .use { stream ->
                stream
                    .filter(Files::isRegularFile)
                    .toList()
                    .asSequence()
                    .map { Files.readString(it) }
                    .mapNotNull { body ->
                        runCatching {
                            val obj = json.parseToJsonElement(body).jsonObject
                            if (obj["type"]?.jsonPrimitive?.contentOrNull != "search") {
                                return@runCatching null
                            }
                            obj["query"]
                                ?.jsonObject
                                ?.get("original")
                                ?.jsonPrimitive
                                ?.contentOrNull
                        }.getOrNull()
                    }
                    .filter { !it.isNullOrBlank() }
                    .toSet()
            }
    }

    private fun generateQueries(
        nouns: List<String>,
        verbs: List<String>,
        fixtureQueries: Set<String>,
        count: Int,
        seed: Long,
    ): List<String> {
        val random = Random(seed)
        val templates =
            listOf<(String, String) -> String>(
                { verb, noun -> "how to $verb $noun" },
                { verb, noun -> "best way to $verb $noun" },
                { verb, noun -> "$verb $noun guide" },
                { verb, noun -> "$verb $noun tutorial" },
                { verb, noun -> "$verb $noun tips" },
            )

        val queries = LinkedHashSet<String>()
        var attempts = 0
        val maxAttempts = count * 40
        while (queries.size < count && attempts < maxAttempts) {
            attempts += 1
            val query =
                templates.random(random).invoke(
                    verbs[random.nextInt(verbs.size)],
                    nouns[random.nextInt(nouns.size)],
                )
            if (query !in fixtureQueries) {
                queries += query
            }
        }
        return queries.toList()
    }

    private fun saveFailureArtifacts(
        query: String,
        index: Int,
        trace: BraveApi.SearchTrace?,
        message: String,
    ) {
        val directory = Paths.get("build/brave-live-failures")
        Files.createDirectories(directory)

        val slug =
            query
                .lowercase()
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .ifBlank { "query" }
                .take(80)
        val stem = "%03d_%s_%s".format(index + 1, Instant.now().toString().replace(':', '-'), slug)

        trace?.responseBody?.let { body ->
            val jsonPath = directory.resolve("$stem.json")
            Files.writeString(jsonPath, body)
            println("Saved failing Brave payload to $jsonPath")
        }

        val metaPath = directory.resolve("$stem.txt")
        Files.writeString(
            metaPath,
            buildString {
                appendLine("query=$query")
                appendLine("message=$message")
                appendLine("url=${trace?.url.orEmpty()}")
                appendLine("status=${trace?.status?.toString().orEmpty()}")
                appendLine("duration=${trace?.duration?.toString().orEmpty()}")
                appendLine("error=${trace?.error?.toString().orEmpty()}")
            },
        )
        println("Saved Brave failure metadata to $metaPath")
    }
}
