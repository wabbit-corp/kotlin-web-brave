package one.wabbit.web.brave

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class LiveBraveConfig(
    val apiKey: String,
    val queryCount: Int,
    val seed: Long,
)

fun loadLiveBraveConfigOrNull(): LiveBraveConfig? {
    if (System.getenv("WABBIT_RUN_LIVE_BRAVE_TEST") != "true") return null
    val apiKey = loadBraveSecret("BRAVE_API_KEY", "BRAVE_KEY") ?: return null
    val queryCount = System.getenv("WABBIT_BRAVE_LIVE_QUERY_COUNT")?.toIntOrNull() ?: 20
    val seed = System.getenv("WABBIT_BRAVE_LIVE_SEED")?.toLongOrNull() ?: 20260316L
    require(queryCount > 0) { "WABBIT_BRAVE_LIVE_QUERY_COUNT must be positive" }
    return LiveBraveConfig(apiKey = apiKey, queryCount = queryCount, seed = seed)
}

private fun loadBraveSecret(vararg envKeys: String): String? {
    envKeys.forEach { key ->
        val direct = System.getenv(key)?.trim().orEmpty()
        if (direct.isNotEmpty()) return direct
    }

    val keysEnv = loadKeysEnv()
    envKeys.forEach { key ->
        val value = keysEnv[key]?.trim().orEmpty()
        if (value.isNotEmpty()) return value
    }

    return loadBraveKeyFromRootPrivate()
}

private fun loadKeysEnv(): Map<String, String> {
    val keysPath = findWorkspaceFile("keys.env") ?: return emptyMap()
    val text = runCatching { Files.readString(keysPath) }.getOrNull() ?: return emptyMap()
    return text
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && "=" in it }
        .associate { line ->
            val (key, value) = line.split("=", limit = 2)
            key.trim() to value.trim().removeSurrounding("\"")
        }
}

private fun loadBraveKeyFromRootPrivate(): String? {
    val rootPrivate = findWorkspaceFile("root.private.clj") ?: return null
    val text = runCatching { Files.readString(rootPrivate) }.getOrNull() ?: return null
    return text
        .lineSequence()
        .map { it.trim() }
        .firstNotNullOfOrNull { line ->
            if (line.startsWith(";")) return@firstNotNullOfOrNull null
            Regex("""^\(brave-key\s+"([^"]+)"\)""").matchEntire(line)?.groupValues?.getOrNull(1)
        }
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

private fun findWorkspaceFile(name: String): Path? {
    val cwd = Paths.get("").toAbsolutePath().normalize()
    val direct = cwd.parent?.resolve(name)
    if (direct != null && Files.exists(direct)) return direct
    return generateSequence(cwd) { current -> current.parent }
        .map { it.resolve(name) }
        .firstOrNull { Files.exists(it) }
}
