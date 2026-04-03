package one.wabbit.web.brave

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking

class BraveApiSpec {
    @Test
    fun braveSearchPreservesErrorResponsePayloads() = runBlocking {
        val observedQueries = mutableListOf<String>()
        val observedTokens = mutableListOf<String>()

        val httpClient =
            HttpClient(
                MockEngine { request ->
                    observedQueries += request.url.parameters["q"].orEmpty()
                    observedTokens += request.headers["X-Subscription-Token"].orEmpty()
                    respond(
                        content = RATE_LIMIT_JSON,
                        status = HttpStatusCode.TooManyRequests,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ) {
                install(HttpTimeout)
            }

        val response = braveSearch(httpClient, "brave search", "secret")
        val error = assertIs<ErrorResponse>(response)
        assertEquals(429, error.error.status)
        assertEquals(listOf("brave search"), observedQueries)
        assertEquals(listOf("secret"), observedTokens)
    }

    @Test
    fun braveSearchHandlesUnknownStructuredFields() {
        runBlocking {
            val httpClient =
                HttpClient(
                    MockEngine {
                        respond(
                            content = SEARCH_WITH_UNKNOWN_FIELDS_JSON,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    },
                ) {
                    install(HttpTimeout)
                }

            val response = assertIs<WebSearchApiResponse>(braveSearch(httpClient, "ada", "secret"))
            assertIs<JsonObject>(response.discussions)
            assertIs<JsonObject>(response.infobox)
            assertIs<JsonArray>(response.locations)
            assertIs<JsonObject>(response.query.language)

            val searchResult = assertIs<SearchResult>(response.web.results[0])
            assertIs<JsonObject>(searchResult.locations)
            assertIs<JsonObject>(searchResult.organization?.contactPoints?.single())

            val locationResult = assertIs<LocationResult>(response.web.results[1])
            assertIs<JsonObject>(locationResult.categories.single())
        }
    }

    @Test
    fun braveApiInvokesSearchTraceOnSuccess() = runBlocking {
        val traces = mutableListOf<BraveApi.SearchTrace>()
        val httpClient =
            HttpClient(
                MockEngine {
                    respond(
                        content = SEARCH_WITH_UNKNOWN_FIELDS_JSON,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ) {
                install(HttpTimeout)
            }

        val api =
            KtorBraveApi(
                httpClient,
                BraveApi.Config(subscriptionToken = "secret", onSearchTrace = { traces += it }),
            )

        val response = assertIs<WebSearchApiResponse>(api.search("ada"))
        assertEquals("ada", response.query.original)

        val trace = assertNotNull(traces.singleOrNull())
        assertEquals("ada", trace.query)
        assertEquals(200, trace.status)
        assertNull(trace.error)
        assertNotNull(trace.responseBody)
        assertTrue(trace.responseBody.contains("\"type\": \"search\""))
    }

    @Test
    fun braveApiInvokesSearchTraceOnDecodeFailure() = runBlocking {
        val traces = mutableListOf<BraveApi.SearchTrace>()
        val httpClient =
            HttpClient(
                MockEngine {
                    respond(
                        content = INVALID_SEARCH_JSON,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ) {
                install(HttpTimeout)
            }

        val api =
            KtorBraveApi(
                httpClient,
                BraveApi.Config(subscriptionToken = "secret", onSearchTrace = { traces += it }),
            )

        assertFailsWith<BraveError.Decode> {
            api.search("broken query")
        }

        val trace = assertNotNull(traces.singleOrNull())
        assertEquals("broken query", trace.query)
        assertEquals(200, trace.status)
        assertIs<BraveError.Decode>(trace.error)
        assertNotNull(trace.responseBody)
        assertTrue(trace.responseBody.contains("\"type\": \"search\""))
    }

    private companion object {
        const val RATE_LIMIT_JSON =
            """
            {
              "type": "ErrorResponse",
              "error": {
                "id": "a048a67a-877a-4310-9347-9bb0d2c17390",
                "status": 429,
                "code": "RATE_LIMITED",
                "detail": "Request rate limit exceeded for plan.",
                "meta": {
                  "plan": "Free",
                  "rate_limit": 1,
                  "rate_current": 1.09,
                  "quota_limit": 2000,
                  "quota_current": 13,
                  "component": "rate_limiter"
                }
              },
              "time": 1696011501
            }
            """

        const val SEARCH_WITH_UNKNOWN_FIELDS_JSON =
            """
            {
              "type": "search",
              "discussions": {
                "results": [
                  {
                    "title": "Thread"
                  }
                ]
              },
              "infobox": {
                "title": "Ada Lovelace",
                "description": "Mathematician"
              },
              "locations": [
                {
                  "title": "London"
                }
              ],
              "mixed": {
                "type": "mixed",
                "main": [],
                "top": [],
                "side": []
              },
              "query": {
                "original": "ada",
                "show_strict_warning": false,
                "is_navigational": false,
                "is_news_breaking": false,
                "language": {
                  "code": "en"
                },
                "spellcheck_off": true,
                "country": "us",
                "bad_results": false,
                "should_fallback": false,
                "postal_code": "",
                "city": "",
                "state": "",
                "header_country": "",
                "more_results_available": true
              },
              "web": {
                "type": "search",
                "family_friendly": true,
                "results": [
                  {
                    "type": "search_result",
                    "subtype": "generic",
                    "meta_url": {
                      "scheme": "https",
                      "netloc": "example.com",
                      "hostname": "example.com",
                      "favicon": "https://example.com/favicon.ico",
                      "path": "› example"
                    },
                    "language": "en",
                    "locations": {
                      "pins": [
                        {
                          "lat": 1.0
                        }
                      ]
                    },
                    "organization": {
                      "type": "organization",
                      "name": "Example Org",
                      "contact_points": [
                        {
                          "label": "support",
                          "email": "support@example.com"
                        }
                      ]
                    },
                    "title": "Example",
                    "url": "https://example.com",
                    "is_source_local": false,
                    "is_source_both": false,
                    "description": "Example result",
                    "family_friendly": true
                  },
                  {
                    "type": "location_result",
                    "title": "Cafe Example",
                    "url": "https://example.com/cafe",
                    "is_source_local": false,
                    "is_source_both": false,
                    "description": "Coffee",
                    "language": "en",
                    "categories": [
                      {
                        "kind": "cafe"
                      }
                    ],
                    "family_friendly": true
                  }
                ]
              }
            }
            """

        const val INVALID_SEARCH_JSON =
            """
            {
              "type": "search",
              "query": {
                "original": "broken query"
              }
            }
            """
    }
}
