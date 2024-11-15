package one.wabbit.web.brave

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.io.File
import java.net.URLEncoder

@Serializable
@JsonClassDiscriminator("type")
sealed class SearchResponse

@Serializable(TestClass.Serializer::class) @JvmInline value class TestClass(val __this_field_doesnt_exist: String) {
    class Serializer : KSerializer<TestClass> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("TestClass", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): TestClass {
            val element = decoder.decodeSerializableValue(JsonElement.serializer())
            System.err.println("UNKNOWN_JSON: $element")
            TODO()
        }
        override fun serialize(encoder: Encoder, value: TestClass) = TODO()

    }
}
typealias UNKNOWN_JSON = TestClass

// # WebSearchApiResponse
//Top level response model for successful Web Search API requests. The response will include the relevant keys based on the plan subscribed, query relevance or applied result_filter as a query parameter. The API can also respond back with an error response based on invalid subscription keys and rate limit events.
//
//FIELD	TYPE	DESCRIPTION
//type	"search"	The type of web search api result. The value is always search.
//discussions	Discussions	Discussions clusters aggregated from forum posts that are relevant to the query.
//faq	FAQ	Frequently asked questions that are relevant to the search query.
//infobox	GraphInfobox	Aggregated information on an entity showable as an infobox.
//locations	Locations	Places of interest (POIs) relevant to location sensitive queries.
//mixed	MixedResponse	Preferred ranked order of search results
//news	News	News results relevant to the query.
//query	Query	Search query string and its modifications that are used for search.
//videos	Videos	Videos relevant to the query.
//web	Search	Web search results relevant to the query.
@Serializable
@SerialName("search")
data class WebSearchApiResponse(
//    @SerialName("type") val type: String = "search",
    @SerialName("discussions") val discussions: Discussions? = null,
    @SerialName("faq") val faq: FAQ? = null,
    @SerialName("infobox") val infobox: GraphInfobox? = null,
    @SerialName("locations") val locations: Locations? = null,
    @SerialName("mixed") val mixed: MixedResponse,
    @SerialName("news") val news: News? = null,
    @SerialName("query") val query: Query,
    @SerialName("videos") val videos: Videos? = null,
    @SerialName("web") val web: Search
) : SearchResponse()

// {
//  "type": "ErrorResponse",
//  "error": {
//    "id": "a048a67a-877a-4310-9347-9bb0d2c17390",
//    "status": 429,
//    "code": "RATE_LIMITED",
//    "detail": "Request rate limit exceeded for plan.",
//    "meta": {
//      "plan": "Free",
//      "rate_limit": 1,
//      "rate_current": 1.09,
//      "quota_limit": 2000,
//      "quota_current": 13,
//      "component": "rate_limiter"
//    }
//  },
//  "time": 1696011501
//}
@Serializable @SerialName("ErrorResponse")
data class ErrorResponse(
//    @SerialName("type") val type: String = "ErrorResponse",
    @SerialName("error") val error: Error,
    @SerialName("time") val time: Long
) : SearchResponse()

// # Error
@Serializable
data class Error(
    @SerialName("id") val id: String,
    @SerialName("status") val status: Int,
    @SerialName("code") val code: String,
    @SerialName("detail") val detail: String,
    @SerialName("meta") val meta: JsonObject
)

// FIXME: Temporary
typealias Discussions = UNKNOWN_JSON
typealias GraphInfobox = UNKNOWN_JSON
typealias Locations = UNKNOWN_JSON

// # News
//A model representing news results.
//
//FIELD	TYPE	DESCRIPTION
//type	news	The type representing the news. The value is always news.
//results	list [ NewsResult ]	A list of news results.
//mutated_by_goggles	bool	Whether the news result where changed by a goggle.
@Serializable
@SerialName("news")
data class News(
    @SerialName("type") val type: String = "news",
    @SerialName("results") val results: List<NewsResult>,
    @SerialName("mutated_by_goggles") val mutatedByGoggles: Boolean
)

// # MetaUrl
//Aggregated information about a url.
//
//FIELD	TYPE	DESCRIPTION
//scheme	string	The protocol scheme extracted from the url.
//netloc	string	The network location part extracted from the url.
//hostname	string	The lowercased domain name extracted from the url.
//favicon	string	The favicon used for the url.
//path	string	The hierarchical path of the url useful as a display string.
@Serializable
data class MetaUrl(
    @SerialName("scheme") val scheme: String,
    @SerialName("netloc") val netloc: String,
    @SerialName("hostname") val hostname: String,
    @SerialName("favicon") val favicon: String,
    @SerialName("path") val path: String
)

// # FAQ
//Frequently asked questions relevant to the search query term.
//
//FIELD	TYPE	DESCRIPTION
//type	"faq"	The FAQ result type identifier. The value is always faq.
//results	list [ QA ]	A list of aggregated question answer results relevant to the query.
@Serializable
@SerialName("faq")
data class FAQ(
    @SerialName("type") val type: String = "faq",
    @SerialName("items") val results: List<QA> // DOCUMENTED AS "results" BUT ACTUALLY "items"
)

// # QA
//A question answer result.
//
//FIELD	TYPE	DESCRIPTION
//question	string	The question being asked.
//answer	string	The answer to the question.
//title	string	The title of the post.
//url	string	The url pointing to the post.
//meta_url	MetaUrl	Aggregated information about the url.
@Serializable
data class QA(
    @SerialName("question") val question: String,
    @SerialName("answer") val answer: String,
    @SerialName("title") val title: String,
    @SerialName("url") val url: String,
    @SerialName("meta_url") val metaUrl: MetaUrl
)

// # Query
//A model representing information gathered around the requested query.
//
//FIELD	TYPE	DESCRIPTION
//original	string	The original query that was requested.
//show_strict_warning	bool	Whether there is more content available for query, but the response was restricted due to safesearch.
//altered	string	The altered query for which the search was performed.
//safesearch	bool	Whether safesearch was enabled.
//is_navigational	bool	Whether the query is a navigational query to a domain.
//is_geolocal	bool	Whether the query has location relevance.
//local_decision	string	Whether the query was decided to be location sensitive.
//local_locations_idx	int	The index of the location.
//is_trending	bool	Whether the query is trending.
//is_news_breaking	bool	Whether the query has news breaking articles relevant to it.
//ask_for_location	bool	Whether the query requires location information for better results.
//language	Language	The language information gathered from the query.
//spellcheck_off	bool	Whether the spellchecker was off.
//country	string	The country that was used.
//bad_results	bool	Whether there are bad results for the query.
//should_fallback	bool	Whether the query should use a fallback.
//lat	string	The gathered location latitutde associated with the query.
//long	string	The gathered location longitude associated with the query.
//postal_code	string	The gathered postal code associated with the query.
//city	string	The gathered city associated with the query.
//state	string	The gathered state associated with the query.
//header_country	string	The country for the request origination.
//more_results_available	bool	Whether more results are available for the given query.
//custom_location_label	string	Any custom location labels attached to the query.
//reddit_cluster	string	Any reddit cluster associated with the query.
@Serializable
data class Query(
    @SerialName("original") val original: String,
    @SerialName("show_strict_warning") val showStrictWarning: Boolean,
    @SerialName("altered") val altered: String? = null,
    @SerialName("safesearch") val safesearch: Boolean? = null,
    @SerialName("is_navigational") val isNavigational: Boolean,
    @SerialName("is_geolocal") val isGeolocal: Boolean? = null,
    @SerialName("local_decision") val localDecision: String? = null,
    @SerialName("local_locations_idx") val localLocationsIdx: Int? = null,
    @SerialName("is_trending") val isTrending: Boolean? = null,
    @SerialName("is_news_breaking") val isNewsBreaking: Boolean,
    @SerialName("ask_for_location") val askForLocation: Boolean? = null,
    @SerialName("language") val language: Language? = null,
    @SerialName("spellcheck_off") val spellcheckOff: Boolean,
    @SerialName("country") val country: String,
    @SerialName("bad_results") val badResults: Boolean,
    @SerialName("should_fallback") val shouldFallback: Boolean,
    @SerialName("lat") val lat: String? = null,
    @SerialName("long") val long: String? = null,
    @SerialName("postal_code") val postalCode: String,
    @SerialName("city") val city: String,
    @SerialName("state") val state: String,
    @SerialName("header_country") val headerCountry: String,
    @SerialName("more_results_available") val moreResultsAvailable: Boolean,
    @SerialName("custom_location_label") val customLocationLabel: String? = null,
    @SerialName("reddit_cluster") val redditCluster: String? = null
)

typealias Language = UNKNOWN_JSON

// # Search
//A model representing a collection of web search results.
//
//FIELD	TYPE	DESCRIPTION
//type	"search"	A type identifying web search results. The value is always search.
//results	list [ SearchResult ]	A list of search results.
//family_friendly	bool	Whether the results are family friendly.
@Serializable
data class Search(
    @SerialName("type") val type: String,
    @SerialName("results") val results: List<SearchResult>,
    @SerialName("family_friendly") val familyFriendly: Boolean
)

// # Profile
//A profile of an entity.
//
//FIELD	TYPE	DESCRIPTION
//name	string	The name of the profile.
//long_name	string	The long name of the profile.
//url	string	The original url where the profile is available.
//img	string	The served image url representing the profile.
@Serializable
data class Profile(
    @SerialName("name") val name: String,
    @SerialName("long_name") val longName: String,
    @SerialName("url") val url: String,
    @SerialName("img") val img: String
)

// # Result
//A model representing a web search result.
//
//FIELD	TYPE	DESCRIPTION
//title	string	The title of the web page.
//url	string	The url where the page is served.
//is_source_local	bool
//is_source_both	bool
//description	string	A description for the web page.
//page_age	string	A date representing the age of the web page.
//page_fetched	string	A date representing when the web page was last fetched.
//profile	Profile	A profile associated with the web page.
//language	string	A language classification for the web page.
//family_friendly	bool	Whether the web page family friendly
@Serializable
@JsonClassDiscriminator("type")
sealed class Result {
    abstract val title: String
    abstract val url: String
    abstract val isSourceLocal: Boolean?
    abstract val isSourceBoth: Boolean?
    abstract val description: String
    abstract val pageAge: String?
    abstract val pageFetched: String?
    abstract val profile: Profile?
    abstract val language: String?
    abstract val familyFriendly: Boolean?
}

@Serializable
data class ClusterResult(
    @SerialName("title") val title: String,
    @SerialName("url") val url: String,
    @SerialName("is_source_local") val isSourceLocal: Boolean? = null,
    @SerialName("is_source_both") val isSourceBoth: Boolean? = null,
    @SerialName("description") val description: String,
    @SerialName("page_age") val pageAge: String? = null,
    @SerialName("page_fetched") val pageFetched: String? = null,
    @SerialName("profile") val profile: Profile? = null,
    @SerialName("language") val language: String? = null,
    @SerialName("family_friendly") val familyFriendly: Boolean? = null,
)

// # SearchResult (Result)
//Aggregated information on a web search result, relevant to the query.
//
//FIELD	TYPE	DESCRIPTION
//type	"search_result"	A type identifying a web search result. The value is always search_result.
//subtype	"generic"	A sub type identifying the web search result type.
//deep_results	DeepResult	Gathered information on a web search result.
//schemas	list [ list ]	A list of schemas extracted from the web search result.
//meta_url	MetaUrl	Aggregated information on the url associated with the web search result.
//thumbnail	Thumbnail	The thumbnail of the web search result.
//age	string	A string representing the age of the web search result.
//language	string	The main language on the web search result.
//restaurant	LocationResult	If a location result, associated restaurant information.
//locations	Locations	The locations associated with the web search result.
//video	VideoData	The video associated with the web search result.
//movie	MovieData	The movie associated with the web search result.
//faq	FAQ	Any frequently asked questions associated with the web search result.
//qa	QAPage	Any question answer information associated with the web search result page.
//book	Book	Any book information associated with the web search result page.
//rating	Rating	Rating found for the web search result page.
//article	Article	An article found for the web search result page.
//product	ProductReview	The main product and a review that is found on the web search result page.
//product_cluster	list [ ProductReview ]	A list of products and reviews that are found on the web search result page.
//cluster_type	string	A type representing a cluster. The value can be product_cluster.
//cluster	list [ Result ]	A list of web search results.
//creative_work	CreativeWork	Aggregated information on the creative work found on the web search result.
//music_recording	MusicRecording	Aggregated information on music recording found on the web search result.
//review	Review	Aggregated information on the review found on the web search result.
//software	Software	Aggregated information on a software product found on the web search result page.
//content_type	string	The content type associated with the search result page.
//extra_snippets	list [ string ]	A list of extra alternate snippets for the web page.
@Serializable
@SerialName("search_result")
data class SearchResult(
    @SerialName("type") val type: String = "search_result",
    @SerialName("subtype") val subtype: String = "generic",
    @SerialName("deep_results") val deepResults: DeepResult? = null,
    @SerialName("schemas") val schemas: List<List<String>>? = null,
    @SerialName("meta_url") val metaUrl: MetaUrl,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("age") val age: String? = null,
    @SerialName("language") override val language: String,
    @SerialName("location") val location: LocationResult? = null, // THIS IS UNDOCUMENTED
    @SerialName("restaurant") val restaurant: LocationResult? = null,
    @SerialName("locations") val locations: Locations? = null,
    @SerialName("video") val video: VideoData? = null,
    @SerialName("movie") val movie: MovieData? = null,
    @SerialName("faq") val faq: FAQ? = null,
    @SerialName("recipe") val recipe: Recipe? = null,
    @SerialName("qa") val qa: QAPage? = null,
    @SerialName("book") val book: Book? = null,
    @SerialName("rating") val rating: Rating? = null,
    @SerialName("article") val article: Article? = null,
    @SerialName("product") val product: Product? = null,
    @SerialName("organization") val organization: Organization? = null,
    @SerialName("product_cluster") val productCluster: List<ProductOrReview>? = null,
    @SerialName("cluster_type") val clusterType: String? = null,
    @SerialName("cluster") val cluster: List<ClusterResult>? = null,
    @SerialName("creative_work") val creativeWork: CreativeWork? = null,
    @SerialName("music_recording") val musicRecording: MusicRecording? = null,
    @SerialName("review") val review: Review? = null,
    @SerialName("software") val software: Software? = null,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("extra_snippets") val extraSnippets: List<String>? = null,
    @SerialName("title") override val title: String,
    @SerialName("url") override val url: String,
    @SerialName("is_source_local") override val isSourceLocal: Boolean,
    @SerialName("is_source_both") override val isSourceBoth: Boolean,
    @SerialName("description") override val description: String,
    @SerialName("page_age") override val pageAge: String? = null,
    @SerialName("page_fetched") override val pageFetched: String? = null,
    @SerialName("profile") override val profile: Profile? = null,
    @SerialName("family_friendly") override val familyFriendly: Boolean
) : Result()

// {
//          "title": "Homemade Gluten Free Pasta Recipe",
//          "description": "<strong>Making</strong> fresh <strong>gluten</strong> <strong>free</strong> <strong>pasta</strong> at home is a labor of love, but it&#x27;s a simple process with the right guidance. Find out how!",
//          "thumbnail": {
//            "src": "https://imgs.search.brave.com/0gBrCVNOlAIci0sqogMjHvUPh99OcPqP6PbrqR33t1A/rs:fit:200:200:1/g:ce/aHR0cHM6Ly9nbHV0/ZW5mcmVlb25hc2hv/ZXN0cmluZy5jb20v/d3AtY29udGVudC91/cGxvYWRzLzIwMTYv/MTEvc3F1YXJlLWxp/bmd1aW5lLWZvci1o/b21lcGFnZS5qcGc",
//            "original": "https://glutenfreeonashoestring.com/wp-content/uploads/2016/11/square-linguine-for-homepage.jpg"
//          },
//          "url": "https://glutenfreeonashoestring.com/gluten-free-pasta-recipe/",
//          "domain": "glutenfreeonashoestring.com",
//          "favicon": "https://imgs.search.brave.com/R7q60VyJ2ocggVCYaKm-ji7XWCDOBQSBAWfrTk8-zE4/rs:fit:32:32:1/g:ce/aHR0cDovL2Zhdmlj/b25zLnNlYXJjaC5i/cmF2ZS5jb20vaWNv/bnMvNGZhZTg5MzMw/ODlhY2RmYjFhZjE1/MmIxN2Q2YzdmZjQz/ODhhNTVhYzMyYTZj/MDZiYTQ0ZjI1MmJm/YjE3MGRiYS9nbHV0/ZW5mcmVlb25hc2hv/ZXN0cmluZy5jb20v",
//          "time": "50:00",
//          "prep_time": "45:00",
//          "cook_time": "05:00",
//          "ingredients": "2 cups all purpose gluten free flour blend ((I like Better Batter here; click through for details) plus more for dusting), 1 teaspoon xanthan gum (omit if your blend already contains it), 5 tablespoons Expandex modified tapioca starch ( (or replace with an equal amount of tapioca starch) (See Recipe Notes)), 1/2 teaspoon kosher salt, 2  eggs (at room temperature, beaten), 2  egg yolks (at room temperature, beaten), 1 tablespoon extra virgin olive oil, 1/3 cup warm water (plus more, as necessary)",
//          "instructions": [
//            {
//              "text": "In the bowl of a food processor fitted with the steel blade, place the flour, xanthan gum, Expandex (or tapioca starch), salt, olive oil, eggs and egg yolks, and pulse until combined.",
//              "name": "In the bowl of a food processor fitted with the steel blade, place the flour, xanthan gum, Expandex (or tapioca starch), salt, olive oil, eggs and egg yolks, and pulse until combined.",
//              "url": "https://glutenfreeonashoestring.com/gluten-free-pasta-recipe/#wprm-recipe-44935-step-0-0",
//              "image": []
//            },
//            {
//              "text": "Add 1/3 cup water to the mixture in the food processor, and process until the dough is moistened.",
//              "name": "Add 1/3 cup water to the mixture in the food processor, and process until the dough is moistened.",
//              "url": "https://glutenfreeonashoestring.com/gluten-free-pasta-recipe/#wprm-recipe-44935-step-0-1",
//              "image": []
//            },
//            {
//              "text": "Turn on the food processor on low speed, remove the hopper and add more water very slowly until the dough clumps to one side of the food processor.",
//              "name": "Turn on the food processor on low speed, remove the hopper and add more water very slowly until the dough clumps to one side of the food processor.",
//              "url": "https://glutenfreeonashoestring.com/gluten-free-pasta-recipe/#wprm-recipe-44935-step-0-2",
//              "image": []
//            }
//          ],
//          "servings": 1,
//          "publisher": "Nicole Hunn",
//          "rating": {
//            "ratingValue": 5.0,
//            "bestRating": 5.0,
//            "reviewCount": 148,
//            "is_tripadvisor": false
//          },
//          "recipeCategory": "Dinner,Dough",
//          "recipeCuisine": "Italian",
//          "video": {
//            "duration": "01:17",
//            "thumbnail": {
//              "src": "https://imgs.search.brave.com/PqP7eMmAeSAKS5xt7URuLRXMQnnaIJcszcI4h3zlyuA/rs:fit:200:200:1/g:ce/aHR0cHM6Ly9tZWRp/YXZpbmUtcmVzLmNs/b3VkaW5hcnkuY29t/L2ltYWdlL3VwbG9h/ZC9zLS10aHo2Vmg3/Si0tL2NfbGltaXQs/Zl9hdXRvLGZsX2xv/c3N5LGhfMTA4MCxx/X2F1dG8sd18xOTIw/L3YxNDc5MDU4MDE0/L2czZG11M25iem5t/cXl3aW5kcTJlLmpw/Zw",
//              "original": "https://mediavine-res.cloudinary.com/image/upload/s--thz6Vh7J--/c_limit,f_auto,fl_lossy,h_1080,q_auto,w_1920/v1479058014/g3dmu3nbznmqywindq2e.jpg"
//            }
//          }
//        }
@Serializable
data class Recipe(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("thumbnail") val thumbnail: Thumbnail,
    @SerialName("url") val url: String,
    @SerialName("domain") val domain: String,
    @SerialName("favicon") val favicon: String,
    @SerialName("time") val time: String? = null,
    @SerialName("prep_time") val prepTime: String? = null,
    @SerialName("cook_time") val cookTime: String? = null,
    @SerialName("ingredients") val ingredients: String,
    @SerialName("calories") val calories: Int? = null,
    @SerialName("instructions") val instructions: List<Instruction>,
    @SerialName("servings") val servings: Int,
    @SerialName("publisher") val publisher: String? = null,
    @SerialName("rating") val rating: Rating? = null,
    @SerialName("recipeCategory") val recipeCategory: String? = null,
    @SerialName("recipeCuisine") val recipeCuisine: String? = null,
    @SerialName("video") val video: VideoData? = null
)

@Serializable
data class Instruction(
    @SerialName("text") val text: String,
    @SerialName("name") val name: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("image") val image: List<String>
)

// # NewsResult
//A model representing news results.
//
//FIELD	TYPE	DESCRIPTION
//meta_url	MetaUrl	The aggregated information on the url representing a news result
//source	string	The source of the news.
//breaking	bool	Whether the news result is currently a breaking news.
//thumbnail	Thumbnail	The thumbnail associated with the news result.
//age	string	A string representing the age of the news article.
@Serializable
data class NewsResult(
    @SerialName("meta_url") val metaUrl: MetaUrl,
    @SerialName("source") val source: String? = null,
    @SerialName("breaking") val breaking: Boolean? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("age") val age: String? = null,
    @SerialName("title") override val title: String,
    @SerialName("language") override val language: String? = null,
    @SerialName("url") override val url: String,
    @SerialName("is_source_local") override val isSourceLocal: Boolean,
    @SerialName("is_source_both") override val isSourceBoth: Boolean,
    @SerialName("description") override val description: String,
    @SerialName("page_age") override val pageAge: String? = null,
    @SerialName("page_fetched") override val pageFetched: String? = null,
    @SerialName("profile") override val profile: Profile? = null,
    @SerialName("family_friendly") override val familyFriendly: Boolean
) : Result() // NOT DOCUMENTED THAT IT IS A RESULT

// # VideoResult (Result)
//A model representing a video result.
//
//FIELD	TYPE	DESCRIPTION
//type	"video_result"	The type identifying the video result. The value is always video_result.
//data	VideoData	Meta data for the video.
//meta_url	MetaUrl	Aggregated information on the URL
//thumbnail	Thumbnail	The thumbnail of the video.
//age	string	A string representing the age of the video.
@Serializable
@SerialName("video_result")
data class VideoResult(
    @SerialName("type") val type: String = "video_result",
    // @SerialName("data") val data: VideoData? = null,
    @SerialName("meta_url") val metaUrl: MetaUrl,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("age") val age: String? = null,
    @SerialName("video") val video: VideoData? = null, // THIS IS NOT DOCUMENTED
    @SerialName("language") override val language: String? = null,
    @SerialName("title") override val title: String,
    @SerialName("url") override val url: String,
    @SerialName("is_source_local") override val isSourceLocal: Boolean? = null,
    @SerialName("is_source_both") override val isSourceBoth: Boolean? = null,
    @SerialName("description") override val description: String,
    @SerialName("page_age") override val pageAge: String? = null,
    @SerialName("page_fetched") override val pageFetched: String? = null,
    @SerialName("profile") override val profile: Profile? = null,
    @SerialName("family_friendly") override val familyFriendly: Boolean? = null
) : Result()

// # LocationResult (Result)
//A result that is location relevant.
//
//FIELD	TYPE	DESCRIPTION
//type	"location_result"	Location result type identifier. The value is always location_result.
//provider_url	string	The complete url of the provider.
//coordinates	list [ float ]	A list of coordinates associated with the location. This is a lat long represented as a floating point.
//zoom_level	int	The zoom level on the map.
//thumbnail	Thumbnail	The thumbnail associated with the location.
//postal_address	PostalAddress	The postal address associated with the location.
//opening_hours	OpeningHours	The opening hours, if it is a business, associated with the location .
//contact	Contact	The contact of the business associated with the location.
//price_range	string	A display string used to show the price classification for the business.
//rating	Rating	The ratings of the business.
//distance	Unit
//profiles	list [ DataProvider ]	The associated profiles with the business.
//reviews	Reviews	Aggregated reviews from various sources relevant to the business.
//pictures	PictureResults	A bunch of pictures associated with the business.
@Serializable
@SerialName("location_result")
data class LocationResult(
    @SerialName("type") val type: String = "location_result",
    @SerialName("provider_url") val providerUrl: String? = null,
    @SerialName("coordinates") val coordinates: List<Float>? = null,
    @SerialName("zoom_level") val zoomLevel: Int? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("postal_address") val postalAddress: PostalAddress? = null,
    @SerialName("opening_hours") val openingHours: OpeningHours? = null,
    @SerialName("serves_cuisine") val servesCuisine: List<String>? = null, // Not documented
    @SerialName("contact") val contact: Contact? = null,
    @SerialName("price_range") val priceRange: String? = null,
    @SerialName("rating") val rating: Rating? = null,
    @SerialName("categories") val categories: List<UNKNOWN_JSON>,
    @SerialName("distance") val distance: Unit? = null,
    @SerialName("profiles") val profiles: List<DataProvider>? = null,
    @SerialName("reviews") val reviews: Reviews? = null,
    @SerialName("pictures") val pictures: PictureResults? = null,
    @SerialName("language") override val language: String? = null,
    @SerialName("title") override val title: String,
    @SerialName("url") override val url: String,
    @SerialName("is_source_local") override val isSourceLocal: Boolean? = null,
    @SerialName("is_source_both") override val isSourceBoth: Boolean? = null,
    @SerialName("description") override val description: String,
    @SerialName("page_age") override val pageAge: String? = null,
    @SerialName("page_fetched") override val pageFetched: String? = null,
    @SerialName("profile") override val profile: Profile? = null,
    @SerialName("family_friendly") override val familyFriendly: Boolean? = null
) : Result()

// # PostalAddress
//A model representing a postal address of a location
//
//FIELD	TYPE	DESCRIPTION
//type	"PostalAddress"	A type identifying a postal address. The value is always postaladdress
//country	string	The country associated with the location.
//postalCode	string	The postal code associated with the location.
//streetAddress	string	The street address associated with the location.
//addressRegion	string	The region associated with the location. This is usually a state.
//addressLocality	string	The address locality or subregion associated with the location.
//displayAddress	string	The displayed address string.
@Serializable
data class PostalAddress(
    @SerialName("type") val type: String = "PostalAddress",
    @SerialName("country") val country: String? = null,
    @SerialName("postalCode") val postalCode: String? = null,
    @SerialName("streetAddress") val streetAddress: String? = null,
    @SerialName("addressRegion") val addressRegion: String? = null,
    @SerialName("addressLocality") val addressLocality: String? = null,
    @SerialName("displayAddress") val displayAddress: String? = null
)

// # OpeningHours
//Opening hours of a bussiness at a particular location.
//
//FIELD	TYPE	DESCRIPTION
//current_day	list [ DayOpeningHours ]	The current day opening hours. Can have two sets of opening hours.
//days	list [ list [ DayOpeningHours ] ]	The opening hours for the whole week.
@Serializable
data class OpeningHours(
    @SerialName("current_day") val currentDay: List<DayOpeningHours>? = null,
    @SerialName("days") val days: List<List<DayOpeningHours>>? = null
)

// # DayOpeningHours
//A model representing a day opening hour for a business at a particular location.
//
//FIELD	TYPE	DESCRIPTION
//abbr_name	string	A short string representing the day of the week.
//full_name	string	A full string representing the day of the week.
//opens	string	A 24 hr clock time string for the opening time of the business at the particular day.
//closes	string	A 24 hr clock time string for the closing time of the business at the particular day.
@Serializable
data class DayOpeningHours(
    @SerialName("abbr_name") val abbrName: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("opens") val opens: String? = null,
    @SerialName("closes") val closes: String? = null
)

// # Contact
//A model representing contact information for an entity.
//
//FIELD	TYPE	DESCRIPTION
//email	string	The email address.
//telephone	string	The telephone number.
@Serializable
data class Contact(
    @SerialName("email") val email: String? = null,
    @SerialName("telephone") val telephone: String? = null
)

// # Rating
//The rating associated with an entity.
//
//FIELD	TYPE	DESCRIPTION
//ratingValue	float	The current value of the rating.
//bestRating	float	Best rating received.
//reviewCount	int	The number reviews for the rating.
//profile	Profile	The profile associated with the rating.
//is_tripadvisor	bool	Is the rating coming from trip advisor.
@Serializable
data class Rating(
    @SerialName("ratingValue") val ratingValue: Float? = null,
    @SerialName("bestRating") val bestRating: Float? = null,
    @SerialName("reviewCount") val reviewCount: Int? = null,
    @SerialName("profile") val profile: Profile? = null,
    @SerialName("is_tripadvisor") val isTripadvisor: Boolean? = null
)

// # Unit
//A model representing a unit of measurement.
//
//FIELD	TYPE	DESCRIPTION
//value	float	The quantity of the unit.
//units	string	The name of the unit associated with the quantity.
@Serializable
data class Unit(
    @SerialName("value") val value: Float? = null,
    @SerialName("units") val units: String? = null
)

// # DataProvider
//A model representing the data provider associated with the entity.
//
//FIELD	TYPE	DESCRIPTION
//type	"external"	A type representing the source of data. This is usually external.
//name	string	The name of the data provider. This can be a domain.
//url	string	The url where the information is coming from.
//long_name	string	The long name for the data provider.
//img	string	The served url for the image data.
@Serializable
data class DataProvider(
    @SerialName("type") val type: String = "external",
    @SerialName("name") val name: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("long_name") val longName: String? = null,
    @SerialName("img") val img: String? = null
)

// # Reviews
//The reviews associated with an entity.
//
//FIELD	TYPE	DESCRIPTION
//results	list [ TripAdvisorReview ]	A list of trip advisor reviews for the entity.
//viewMoreUrl	string	A url to a web page where more information on the result can be seen.
//reviews_in_foreign_language	bool	Any reviews available in a foreign language.
@Serializable
data class Reviews(
    @SerialName("results") val results: List<TripAdvisorReview>? = null,
    @SerialName("viewMoreUrl") val viewMoreUrl: String? = null,
    @SerialName("reviews_in_foreign_language") val reviewsInForeignLanguage: Boolean? = null
)

// # TripAdvisorReview
//A model representing the a trip advisor review.
//
//FIELD	TYPE	DESCRIPTION
//title	string	The title of the review.
//description	string	A description seen in the review.
//date	string	The date when the review was published.
//rating	Rating	A rating given by the reviewer.
//author	Person	The author name of the review.
//review_url	string	A url link to the page where the review can be found.
//language	string	The language of the review.
@Serializable
data class TripAdvisorReview(
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("rating") val rating: Rating? = null,
    @SerialName("author") val author: Person? = null,
    @SerialName("review_url") val reviewUrl: String? = null,
    @SerialName("language") val language: String? = null
)

@Serializable(with=ProductOrReview.Serializer::class)
sealed class ProductOrReview {
    class Serializer : KSerializer<ProductOrReview> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ProductOrReview") {
            element("type", String.serializer().descriptor)
        }

        override fun serialize(encoder: Encoder, value: ProductOrReview) {
            require(encoder is JsonEncoder)
            val type = when (value) {
                is Review -> "review"
                is Product -> "Product"
            }

            val jsonElement = when (value) {
                is Review -> Json.encodeToJsonElement(Review.serializer(), value)
                is Product -> Json.encodeToJsonElement(Product.serializer(), value)
            }

            val jsonObject = jsonElement.jsonObject.toMutableMap()
            jsonObject["type"] = JsonPrimitive(type)
            encoder.encodeJsonElement(JsonObject(jsonObject))
        }

        override fun deserialize(decoder: Decoder): ProductOrReview {
            require(decoder is JsonDecoder)
            val element = decoder.decodeJsonElement()
            val type = element.jsonObject["type"]?.jsonPrimitive?.contentOrNull
            // Rebuild the object without the type field
            val elementWithoutType = JsonObject(element.jsonObject.filterKeys { it != "type" })
            return when {
                element.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "review" ->
                    Json.decodeFromJsonElement<Review>(elementWithoutType)
                element.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "Product" ->
                    Json.decodeFromJsonElement<Product>(elementWithoutType)
                else -> throw SerializationException("Unknown type")
            }
        }
    }
}

// # Review
//A model representing a review for an entity.
//
//FIELD	TYPE	DESCRIPTION
//type	"review"	A string representing review type. This is always review.
//name	string	The review title for the review.
//thumbnail	Thumbnail	The thumbnail associated with the reviewer.
//description	string	A description of the review.
//rating	Rating	The ratings associated with the review.
@Serializable @SerialName("review") data class Review(
    @SerialName("type") val type: String = "review",
    @SerialName("name") val name: String? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("rating") val rating: Rating? = null
) : ProductOrReview()

// # Product
//A model representing a product.
//
//FIELD	TYPE	DESCRIPTION
//type	"Product"	A string representing a product type. The value is always product.
//name	string	The name of the product.
//price	string	The price of the product.
//thumbnail	Thumbnail	A thumbnail associated with the product.
//description	string	The description of the product.
//offers	list [ Offer ]	A list of offers available on the product.
//rating	Rating	A rating associated with the product.
/**
 * @param type A string representing a product type. The value is always product.
 * @param name The name of the product.
 * @param price The price of the product.
 * @param thumbnail A thumbnail associated with the product.
 * @param description The description of the product.
 * @param offers A list of offers available on the product.
 * @param rating A rating associated with the product.
 */
@Serializable @SerialName("Product") data class Product(
    @SerialName("type") val type: String = "Product",
    @SerialName("name") val name: String? = null,
    @SerialName("price") val price: String? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("offers") val offers: List<Offer>? = null,
    @SerialName("rating") val rating: Rating? = null
) : ProductOrReview()

// # Offer
//An offer associated with a product.
//
//FIELD	TYPE	DESCRIPTION
//url	string	The url where the offer can be found.
//priceCurrency	string	The currency in which the offer is made.
//price	string	The price of the product currently on offer.
/**
 * @param url The url where the offer can be found.
 * @param priceCurrency The currency in which the offer is made.
 * @param price The price of the product currently on offer.
 */
@Serializable
data class Offer(
    @SerialName("url") val url: String? = null,
    @SerialName("priceCurrency") val priceCurrency: String? = null,
    @SerialName("price") val price: String? = null
)

// # PictureResults
//A model representing a list of pictures.
//
//FIELD	TYPE	DESCRIPTION
//viewMoreUrl	string	A url to view more pictures.
//results	list [ Thumbnail ]	A list of thumbnail results.
@Serializable
data class PictureResults(
    @SerialName("viewMoreUrl") val viewMoreUrl: String? = null,
    @SerialName("results") val results: List<Thumbnail>? = null
)

// {"name":"LocalAI","author":"go-skynet","is_npm":false,"is_pypi":false,"stars":11500,"forks":1000,"programmingLanguage":"Go 58.9% | Python 34.5% | Makefile 4.9% | Dockerfile 1.4%"}
@Serializable
data class Software(
    @SerialName("name") val name: String? = null,
    @SerialName("version") val version: String? = null,
    @SerialName("author") val author: String? = null,
    @SerialName("is_npm") val isNpm: Boolean? = null,
    @SerialName("is_pypi") val isPypi: Boolean? = null,
    @SerialName("stars") val stars: Int? = null,
    @SerialName("forks") val forks: Int? = null,
    @SerialName("programmingLanguage") val programmingLanguage: String? = null
)

// # MovieData
//Aggregated data for a movie result.
//
//FIELD	TYPE	DESCRIPTION
//name	string	Name of the movie.
//description	string	A short plot summary for the movie.
//url	string	A url serving a movie profile page.
//thumbnail	Thumbnail	A thumbnail for a movie poster.
//release	string	The release date for the movie.
//directors	list [ Person ]	A list of people responsible for directing the movie.
//actors	list [ Person ]	A list of actors in the movie.
//rating	Rating	Rating provided to the movie from various sources.
//duration	string	The runtime of the movie. The format is HH:MM:SS.
//genre	list [ string ]	List of genres in which the movie can be classified.
//query	string	The query that resulted in the movie result.
@Serializable
data class MovieData(
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("release") val release: String? = null,
    @SerialName("directors") val directors: List<Person>? = null,
    @SerialName("actors") val actors: List<Person>? = null,
    @SerialName("rating") val rating: Rating? = null,
    @SerialName("duration") val duration: String? = null,
    @SerialName("genre") val genre: List<String>? = null,
    @SerialName("query") val query: String? = null
)

// # MusicRecording
//Result classified as a music label or a song.
//
//FIELD	TYPE	DESCRIPTION
//name	string	The name of the song or album.
//thumbnail	Thumbnail	A thumbnail associated with the music.
//rating	Rating	The rating of the music.
@Serializable
data class MusicRecording(
    @SerialName("name") val name: String? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("rating") val rating: Rating? = null
)

@Serializable(with=ViewCount.Serializer::class) @JvmInline
value class ViewCount(val value: Int) {
    class Serializer : KSerializer<ViewCount> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ViewCount", PrimitiveKind.INT)
        override fun serialize(encoder: Encoder, value: ViewCount) = encoder.encodeInt(value.value)
        override fun deserialize(decoder: Decoder): ViewCount {
            require(decoder is JsonDecoder)
            val element = decoder.decodeJsonElement()

            if (element is JsonPrimitive) {
                val value = element.content
                if (value.endsWith("K")) {
                    return ViewCount(value.removeSuffix("K").toIntOrNull() ?: value.toInt() * 1000)
                } else if (value.endsWith("M")) {
                    return ViewCount(value.removeSuffix("M").toIntOrNull() ?: value.toInt() * 1_000_000)
                }
                return ViewCount(value.toInt())
            } else {
                throw SerializationException("Expected JsonPrimitive but got $element")
            }
        }
    }
}

// # VideoData
//A model representing metadata gathered for a video.
//
//FIELD	TYPE	DESCRIPTION
//duration	string	A time string representing the duration of the video.
//views	string	The number of views of the video.
//creator	string	The creator of the video.
//publisher	string	The publisher of the video.
//thumbnail	Thumbnail	A thumbnail associated with the video.
@Serializable
data class VideoData(
    @SerialName("duration") val duration: String? = null,
    @SerialName("views") val views: ViewCount? = null,
    @SerialName("creator") val creator: String? = null,
    @SerialName("publisher") val publisher: String? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null
)

// # QAPage
//Aggreated result from a question answer page.
//
//FIELD	TYPE	DESCRIPTION
//question	string	The question that is being asked.
//answer	Answer	An answer to the question.
@Serializable
data class QAPage(
    @SerialName("question") val question: String? = null,
    @SerialName("answer") val answer: Answer? = null
)

// # Answer
//A response representing an answer to a question on a forum.
//
//FIELD	TYPE	DESCRIPTION
//text	string	The main content of the answer.
//author	string	A name string for the author of the answer.
//upvoteCount	int	Number of up votes on the answer.
//downvoteCount	int	The number of down votes on the answer.
@Serializable
data class Answer(
    @SerialName("text") val text: String? = null,
    @SerialName("author") val author: String? = null,
    @SerialName("upvoteCount") val upvoteCount: Int? = null,
    @SerialName("downvoteCount") val downvoteCount: Int? = null
)

// # CreativeWork
//A creative work relevant to the query. Can be a app etc.
//
//FIELD	TYPE	DESCRIPTION
//name	string	The name of the creative work.
//thumbnail	Thumbnail	A thumbnail associated with the creative work.
//rating	Rating	A rating that is given to the creative work.
@Serializable
data class CreativeWork(
    @SerialName("name") val name: String? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("rating") val rating: Rating? = null
)

// # Book
//A model representing a book result.
//
//FIELD	TYPE	DESCRIPTION
//title	string	The title of the book.
//author	list [ Person ]	The author of the book.
//date	string	A published date for the book.
//price	Price	The price of the book.
//pages	string	The number of pages in the book.
//publisher	Person	The publisher of the book.
//rating	Rating	A gathered rating from different sources associated with the book.
@Serializable
data class Book(
    @SerialName("title") val title: String? = null,
    @SerialName("author") val author: List<Person>? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("price") val price: Price? = null,
    @SerialName("pages") val pages: Int? = null,
    @SerialName("publisher") val publisher: Person? = null,
    @SerialName("rating") val rating: Rating? = null
)

// # Price
//A model describing a price for an entity.
//
//FIELD	TYPE	DESCRIPTION
//price	string	The price value in a given currency.
//price_currency	string	The current of the price value.
@Serializable
data class Price(
    @SerialName("price") val price: String? = null,
    @SerialName("priceCurrency") val priceCurrency: String? = null // FIXME: This is not documented
)

// # Article
//A model representing an article.
//
//FIELD	TYPE	DESCRIPTION
//author	list [ Person ]	The author of the article.
//date	string	The date when the article was published.
//publisher	Organization	The name of the publisher for the article.
//thumbnail	Thumbnail	A thumbnail associated with the article.
//isAccessibleForFree	bool	Whether the article is free to read or is behind a paywall.
@Serializable
data class Article(
    @SerialName("author") val author: List<Person>? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("publisher") val publisher: Organization? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("isAccessibleForFree") val isAccessibleForFree: Boolean? = null
)

// # Person
//A model describing a person entity.
//
//FIELD	TYPE	DESCRIPTION
//type	"person"	A type identifying a person. The value is always person.
//name	string	The name of the person.
//url	string	A profile url for the person.
//thumbnail	Thumbnail	Thumbnail associated with the person.
@Serializable
@SerialName("person")
data class Person(
    @SerialName("type") val type: String = "person",
    @SerialName("name") val name: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null
)

// # DeepResult
//Aggregated deep results from news social videos and images.
//
//FIELD	TYPE	DESCRIPTION
//news	list [ NewsResult ]	A list of news results associated with the result.
//buttons	list [ ButtonResult ]	A list of buttoned results associated with the result.
//social	list [ KnowledgeGraphProfile ]	Social profile associated with the result.
//videos	list [ VideoResult ]	Videos associated with the result.
//images	list [ Image ]	Images associated with the result.
@Serializable
data class DeepResult(
    @SerialName("news") val news: List<NewsResult>? = null,
    @SerialName("buttons") val buttons: List<ButtonResult>? = null,
    @SerialName("social") val social: List<KnowledgeGraphProfile>? = null,
    @SerialName("videos") val videos: List<VideoResult>? = null,
    @SerialName("images") val images: List<Image>? = null
)

// # ButtonResult
//A result which can be used as a button.
//
//FIELD	TYPE	DESCRIPTION
//type	"button_result"	A type identifying button result. The value is always button_result.
//title	string	The title of the result.
//url	string	The url for the button result.
@Serializable
@SerialName("button_result")
data class ButtonResult(
    @SerialName("type") val type: String = "button_result",
    @SerialName("title") val title: String? = null,
    @SerialName("url") val url: String? = null
)

// # KnowledgeGraphProfile (KnowledgeGraphEntity)
//Represents an entity profile from a knowledge graph.
//
//FIELD	TYPE	DESCRIPTION
//url	URL	The url representing the profile.
//description	string	A description of the profile.
@Serializable
data class KnowledgeGraphProfile(
    @SerialName("url") val url: URL? = null,
    @SerialName("description") val description: String? = null
)

// # Image
//A model describing an image
//
//FIELD	TYPE	DESCRIPTION
//thumbnail	Thumbnail	The thumbnail associated with the image.
//url	string	The url of the image.
//properties	ImageProperties	Metadata on the image.
@Serializable
data class Image(
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("properties") val properties: ImageProperties? = null
)

// # ImageProperties
//Metadata on an image.
//
//FIELD	TYPE	DESCRIPTION
//url	string	The image URL.
//resized	string	The resized image.
//height	int	The height of the image.
//width	int	The width of the image.
//format	string	The format specifier for the image.
//content_size	string	The image storage size.
@Serializable
data class ImageProperties(
    @SerialName("url") val url: String? = null,
    @SerialName("resized") val resized: String? = null,
    @SerialName("height") val height: Int? = null,
    @SerialName("width") val width: Int? = null,
    @SerialName("format") val format: String? = null,
    @SerialName("content_size") val contentSize: String? = null
)

// # URL
//A model representing a URL.
//
//FIELD	TYPE	DESCRIPTION
//original	string	The original source URL.
//display	string	The display URL.
//alternatives	list [ string ]	An alternative representation of a URL.
//canonical	string	The canonical form of the URL.
//mobile	MobileUrlItem	A mobile friendly version of the URL.
@Serializable
data class URL(
    @SerialName("original") val original: String? = null,
    @SerialName("display") val display: String? = null,
    @SerialName("alternatives") val alternatives: List<String>? = null,
    @SerialName("canonical") val canonical: String? = null,
    @SerialName("mobile") val mobile: MobileUrlItem? = null
)

// # MobileUrlItem
//A mobile friendly representation of the URL.
//
//FIELD	TYPE	DESCRIPTION
//original	string	The original source URL.
//amp	string	The amp version of the URL.
//android	string	An android friendly version of the URL.
//ios	string	An ios friendly version of the URL.
@Serializable
data class MobileUrlItem(
    @SerialName("original") val original: String? = null,
    @SerialName("amp") val amp: String? = null,
    @SerialName("android") val android: String? = null,
    @SerialName("ios") val ios: String? = null
)

// # Organization (Person)
//An entity responsible for another entity.
//
//FIELD	TYPE	DESCRIPTION
//type	"organization"	A type string identifying an organization. The value is always organization.
@Serializable
@SerialName("organization")
data class Organization(
    @SerialName("type") val type: String = "organization",
    @SerialName("name") val name: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerialName("contact_points") val contactPoints: List<ContactPoint>? = null
)

typealias ContactPoint = UNKNOWN_JSON

// # Thumbnail
//Aggregated details representing a picture thumbnail.
//
//FIELD	TYPE	DESCRIPTION
//src	string	The served url of the image.
//height	int	The height of the image.
//width	int	The width of the image.
//bg_color	string	The background color of the image.
//original	string	The original url of the image.
//logo	bool	Whether the image is a logo.
//duplicated	bool	Whether the image is duplicated.
//theme	string	The theme associated with the image.
@Serializable
data class Thumbnail(
    @SerialName("src") val src: String,
    @SerialName("height") val height: Int? = null,
    @SerialName("width") val width: Int? = null,
    @SerialName("bg_color") val bgColor: String? = null,
    @SerialName("original") val original: String? = null,
    @SerialName("logo") val logo: Boolean? = null,
    @SerialName("duplicated") val duplicated: Boolean? = null,
    @SerialName("theme") val theme: String? = null
)

// # MixedResponse
//The ranking order of results on a search result page.
//
//FIELD	TYPE	DESCRIPTION
//type	"mixed"	The type representing the model mixed. The value is by default mixed.
//main	list [ ResultReference ]	The ranking order for the main section of the search result page.
//top	list [ ResultReference ]	The ranking order for the top section of the search result page.
//side	list [ ResultReference ]	The ranking order for the side section of the search result page.
@Serializable
data class MixedResponse(
    @SerialName("type") val type: String = "mixed",
    @SerialName("main") val main: List<ResultReference>,
    @SerialName("top") val top: List<ResultReference>,
    @SerialName("side") val side: List<ResultReference>
)

// # ResultReference
//The ranking order of results on a search result page.
//
//FIELD	TYPE	DESCRIPTION
//type	string	The type of the result.
//index	int	The 0th based index where the result should be placed.
//all	bool	Whether to put all the results from the type at specific position.
@Serializable
data class ResultReference(
    @SerialName("type") val type: String,
    @SerialName("index") val index: Int? = null,
    @SerialName("all") val all: Boolean
)

// # Videos
//A model representing video results.
//
//FIELD	TYPE	DESCRIPTION
//type	videos	The type representing the videos. The value is always videos.
//results	list [ VideoResult ]	A list of video results.
//mutated_by_goggles	bool	Whether the videos result where changed by a goggle.
@Serializable
data class Videos(
    @SerialName("type") val type: String = "videos",
    @SerialName("results") val results: List<VideoResult>,
    @SerialName("mutated_by_goggles") val mutatedByGoggles: Boolean
)

private fun saveBraveSearchResponse(request: String) {
    val root: File
    if (File("./lib-web-brave").exists()) {
        root = File("./lib-web-brave")
    } else if (File(".").absoluteFile.canonicalFile.name == "lib-web-brave") {
        root = File(".")
    } else {
        throw IllegalStateException("Could not find root directory")
    }

    // Save file into app-marketscape/src/test/resources/brave_N.json
    // where N is the next available number
    val files = File(root,"src/test/resources/brave").listFiles()
        .map { it.name }.toSet()

    // println("Files: $files")

    var num = 1
    while (true) {
        val testName = "brave_$num.json"
        if (testName in files) {
            num += 1
            continue
        }

        File(root, "src/test/resources/brave/$testName").writeText(request)
        break
    }
}

//class BraveCache(val connection: Connection, val ttl: Duration = Duration.ofDays(1)) {
//    init {
//        connection.createStatement().execute(
//            """
//            CREATE TABLE IF NOT EXISTS brave_request (
//                digest TEXT NOT NULL, -- SHA-256 of the inputs
//                index INT NOT NULL,   -- 0-based index of the request
//                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
//
//                query_params TEXT NOT NULL,       -- JSON (input)
//                header_params TEXT NOT NULL,      -- JSON (input)
//                response TEXT NOT NULL            -- JSON
//                PRIMARY KEY (digest, index)
//            )
//            """.trimIndent()
//        )
//    }
//
//    data class Cached(
//        val digest: String,
//        val queryParams: Map<String, String>,
//        val headerParams: Map<String, String>,
//        val response: String,
//    )
//
//    @OptIn(ExperimentalStdlibApi::class)
//    fun get(queryParams: Map<String, String>, headerParams: Map<String, String>): String? {
//        val digest = MessageDigest.getInstance("SHA-256")
//        digest.update(queryParams.toString().toByteArray())
//        digest.update(0x00.toByte())
//        digest.update(headerParams.toString().toByteArray())
//
//        val digestHex = digest.digest().toHexString()
//
//        val stmt = connection.prepareStatement(
//            """
//            SELECT response FROM brave_request
//            WHERE digest = ? AND created_at > ?
//            ORDER BY created_at DESC
//            LIMIT 1
//            """.trimIndent()
//        )
//        stmt.setString(1, digestHex)
//        stmt.setTimestamp(2, Timestamp.from(Instant.now().minus(ttl)))
//        val rs = stmt.executeQuery()
//        if (!rs.next()) {
//            return null
//        }
//        return rs.getString("response")
//    }
//}

suspend fun braveSearch(httpClient: HttpClient, query: String, subscriptionToken: String): SearchResponse {
    // https://api.search.brave.com/res/v1/web/search
    // https://api.search.brave.com/res/v1/web/search?q=hello
    // curl -s --compressed "https://api.search.brave.com/res/v1/web/search?q=brave+search" -H "Accept:
    //      application/json" -H "Accept-Encoding: gzip" -H "X-Subscription-Token: <YOUR_API_KEY>"

    // Use proper URL encoding
    val query = "https://api.search.brave.com/res/v1/web/search?extra_snippets=true&q=${URLEncoder.encode(query, "UTF-8")}"

    val response = httpClient.get(query) {
        header("Accept", "application/json")
        // header("Accept-Encoding", "gzip")
        header("X-Subscription-Token", subscriptionToken)
    }

    val body = response.bodyAsText()
    //saveBraveSearchResponse(body)
    return Json.decodeFromString<SearchResponse>(body)
}
