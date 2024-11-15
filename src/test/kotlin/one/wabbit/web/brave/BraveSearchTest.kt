package one.wabbit.web.brave

import io.ktor.client.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test

class BraveSearchTest {
    @Ignore @Test fun test() {
        val json = Json

        val ids = File("./src/test/resources/brave/").listFiles()!!.map {
            val name = it.name
            check(name.startsWith("brave_") && name.endsWith(".json"))
            val num = name.substring(6, name.length - 5).toInt()
            num
        }.sorted()

        for (i in ids) {
//            println("Reading $i")

            val text = javaClass.getResource("/brave/brave_$i.json").readText()

            val obj =
                try {
                    val r = json.decodeFromString<SearchResponse>(text)
                    Json.encodeToString(r)
                }
                catch (e: Exception) {
                    println("Error at $i: $e")
                    break
                }

//            val strBuf = StringWriter()
//            pp(obj, omitNulls = true, writeTo = strBuf)
        }

        if (false) {
            val client = HttpClient { }

            val allQueries = mutableListOf(
                "How to grow a garden in a small urban space?",
                "Top 10 sci-fi movies of the last decade",
                "Easy vegan recipes under 30 minutes",
                "Beginner's guide to stock market investment",
                "History of the internet and its impact on global communication",
                "DIY home decor projects on a budget",
                "Steps to learn a new language effectively",
                "Ways to improve mental health and reduce stress",
                "Best travel destinations 2023",
                "Latest advancements in renewable energy technologies",
                "What are the top 10 emerging technologies in 2023?",
                "Famous unsolved mysteries of the world",
                "Best vegan recipes for beginners",
                "How to train for a marathon in 3 months",
                "Upcoming astronomical events viewable from Earth",
                "Most impactful climate change initiatives globally",
                "Guide to starting a small business online",
                "Virtual reality in education: case studies and examples",
                "The rise and influence of cryptocurrency in global finance",
                "DIY home improvement projects for beginners",
                "How do octopuses change color?",
                "What is the theory of everything in physics?",
                "Top 10 innovations in renewable energy for 2023",
                "Impact of artificial intelligence on modern medicine",
                "Secret life of pets: Do animals dream?",
                "Historical turning points that shaped the digital era",
                "Best books on early human civilizations",
                "Recipe for making homemade gluten-free pasta",
                "Most anticipated tech gadgets of the next decade",
                "How to start learning a new language effectively",
                "How does quantum computing work?",
                "Top 10 innovations of the last decade",
                "Impact of climate change on marine life",
                "Future of space exploration and Mars colonization",
                "Artificial Intelligence vs Human Creativity",
                "Hidden health benefits of Mediterranean diet",
                "Cultural significance of traditional festivals around the world",
                "Technological advancements in renewable energy",
                "Psychological effects of social media on teens",
                "Mysteries behind the construction of the Pyramids",
                "how to grow organic vegetables in small spaces",
                "top 10 science fiction books of the last decade",
                "DIY projects for solar energy at home",
                "best workout routines for home with no equipment",
                "tips for learning a new language quickly",
                "guide to star gazing and identifying constellations",
                "history of coffee culture around the world",
                "how do self-driving cars work",
                "methods for effective stress relief techniques",
                "emerging technology trends in 2023",
                "How to grow a veggie garden in limited space?",
                "Top 10 programming languages in 2023",
                "Recipe for vegan lasagna",
                "Latest discoveries in space by NASA",
                "Tips for meditation and mindfulness for beginners",
                "Best cities to visit in Europe in winter",
                "How do electric cars work?",
                "History of the internet timeline",
                "Upcoming movies in 2023 and their release dates",
                "DIY home renovation ideas on a budget",
                "latest advancements in renewable energy 2023",
                "top 10 healthy recipes for weight loss",
                "current trends in AI and machine learning",
                "best travel destinations in Europe for families",
                "how to start an online business from home",
                "tips for improving mental health and well-being",
                "best practices for organic gardening in urban areas",
                "upcoming technology innovations in the automotive industry",
                "historical impacts of pandemics on global economies",
                "comparison of Python vs. JavaScript for web development",
                "Why do squirrels not like disco music?",
                "Can a cat be trained to speak Spanish?",
                "Is it possible to grow a chocolate-flavored apple tree?",
                "Wearing socks with sandals on Mars: fashion crimes?",
                "Do spiders dream about electric insects?",
                "Are there secret societies for introverted yogis?",
                "Recipes for a cake that tastes like lasagna",
                "What is the average flight speed of an unladen swallow in a hurricane?",
                "Can a narwhal and a unicorn be best friends?",
                "Is there a world championship for underwater basket weaving?",
                "Why do squirrels like disco music?",
                "Is it possible to teach an octopus to paint?",
                "What's the recipe for a cloud sandwich?",
                "How many books can a whale read?",
                "Most fashionable hats for cats in 2023",
                "Can a potato predict the weather?",
                "Dancing lessons for penguins near me",
                "What do unicorns eat for breakfast?",
                "Do aliens prefer tea or coffee?",
                "How to organize a party for invisible friends",
                "Best karaoke songs for parrots",
                "Secret lives of singing plants",
                "Why do ghosts avoid fast food?",
                "How to knit a sweater for a snake",
                "What's the world record for underwater juggling?",
                "Ancient philosophies about time traveling worms",
                "Top 10 picnic spots on the moon",
                "Are there any sports leagues for robots?",
                "How to become best friends with a mermaid",
                "Best-selling perfumes for zombies",
                "Could a kangaroo win a chess tournament?",
                "Fitness routines for lazy dragons",
                "Guide to starting your own unicorn farm",
                "Interview tips for wizards",
                "Weekend DIY: building a spaceship in your backyard",
                "How to learn a song that hasn't been written yet",
                "Eco-friendly homes for imaginary animals",
                "What language do mirrors speak?",
                "Can cookies have nightmares about dieting?",
                "History of pirate ninjas in the Atlantic Ocean",
                "can ducks live on Jupiter",
                "do plants scream when no one is around",
                "why don't we have B-movies about snails",
                "can you teach an AI to sneeze",
                "what does Wednesday smell like",
                "is it possible to read a book backwards in a mirror",
                "dance lessons for cats near me",
                "how many spoons of sugar to sweeten the ocean",
                "world championship in slow walking",
                "DIY rocket ship to Mars schedule",
                "recipes for cooking socks",
                "do aliens pay taxes",
                "can you train a goldfish to solve puzzles",
                "why aren't there any pirate supermarkets",
                "is the moon made of spare rib",
                "how do I unsubscribe from gravity",
                "what's the Wi-Fi password for the nearest galaxy",
                "training sneakers to run away from home",
                "secret lives of haunted toasters",
                "extreme ironing underwater",
                "yodeling lessons for beginners online",
                "how to knit a sweater for a tree",
                "invisible ink visible only to cats",
                "unicorn sightings in Antarctica",
                "tips for photographing a ghost",
                "can a time machine be powered by potatoes",
                "how old is the oldest piece of chewing gum",
                "does talking to plants make them gossip about you",
                "annual mermaid parade application form",
                "DIY guide to building your own pyramid",
                "Best vegan recipes 2023",
                "Top 10 movies of the last decade",
                "How does blockchain technology work",
                "Local weather forecast for New York",
                "History of the Roman Empire",
                "Latest stock market news",
                "Top sci-fi books 2023",
                "AI developments in healthcare",
                "Travel restrictions Europe COVID-19",
                "How to start a podcast",
                "Exercise routines for beginners",
                "Easy DIY home decor projects",
                "Live football scores",
                "Future of electric vehicles 2025",
                "Photography tips for beginners",
                "How to learn Python for free",
                "Best coffee shops near me",
                "Guide to meditation and mindfulness",
                "Current top Billboard hits",
                "SpaceX latest launch updates",
                "Upcoming video game releases 2023",
                "Best smartphones under $500",
                "How to grow an indoor herb garden",
                "Tips for improving mental health",
                "Recipes using only 5 ingredients",
                "Online learning platforms review",
                "Fashion trends for spring 2023",
                "Wildlife conservation efforts worldwide",
                "Best credit cards for travel rewards",
                "Guide to starting an online business",
                "cost implications of new technologies in high torque low speed electric motors"
            )

            // println(File(".").absolutePath)

            for (fn in File("src/test/resources/brave/").listFiles()!!) {
                val json = Json.decodeFromString<JsonElement>(fn.readText())

                if (json.jsonObject["type"]!!.jsonPrimitive.content == "search") {
                    val query = json.jsonObject["query"]!!.jsonObject["original"]!!.jsonPrimitive.content
                    allQueries.remove(query)
                    // println("Removing $query")
                }
            }

            runBlocking {
                for (q in allQueries.reversed()) {
                    delay(2000)
                    println("Searching for $q")
                    try {
                        val token = ""
                        val r = braveSearch(client, q, token)
                        Json.encodeToString(r)
                    }
                    catch (e: Exception) {
                        println("Error: $e")
                    }
                }
            }
        }
    }
}
