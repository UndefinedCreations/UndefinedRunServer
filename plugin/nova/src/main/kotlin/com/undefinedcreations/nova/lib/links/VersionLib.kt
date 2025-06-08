package com.undefinedcreations.nova.lib.links

import com.google.gson.JsonParser
import java.net.URI

/**
 * Used to get a list of all supported Minecraft server versions for each `ServerType`.
 *
 * @since 1.0
 */
object VersionLib {

    /**
     * Used to get a list of all supported for `PaperMC`.
     *
     * @return A list of all supported versions for `PaperMC`.
     */
    fun paper(): List<String> = paperRepoVersions("paper")

    /**
     * Used to get a list of all supported for `Velocity`.
     *
     * @return A list of all supported versions for `Velocity`.
     */
    fun velocity(): List<String> = paperRepoVersions("velocity")

    /**
     * Used to get a list of all supported for `Folia`.
     *
     * @return A list of all supported versions for `Folia`.
     */
    fun folia(): List<String> = paperRepoVersions("folia")

    /**
     * Used to get a list of all supported for `Waterfall`.
     *
     * @return A list of all supported versions for `Waterfall`.
     */
    fun waterfall(): List<String> = paperRepoVersions("waterfall")

    /**
     * Used to get a list of all supported for `Spigot`.
     *
     * @return A list of all supported versions for `Spigot`.
     */
    fun spigot(): List<String> {
        val url = URI(Repositories.UNDEFINEDCREATIONS_REPO)
        val text = url.toURL().readText()
        val regex = """spigot-\d+\.\d+(\.\d+)?\.jar""".toRegex()
        val files = regex.findAll(text).map { it.value.replace("spigot-", "").replace(".jar", "") }.toSet()
        return files.toList()
    }

    /**
     * Used to get a list of all supported for `Bungeecord`.
     *
     * @return Always returns `ALL_VERSIONS` because the latest Bungeecord version supports all Minecraft versions.
     */
    fun bungeecord(): List<String> =
        listOf("ALL_VERSIONS") // DownloadLib get the last version of Bungeecord

    /**
     * Used to get a list of all supported for `Purpur`.
     *
     * @return A list of all supported versions for `Purpur`.
     */
    fun purpur(): List<String> {
        val url = URI(Repositories.PURPUR_REPO)
        val versions = JsonParser.parseString(url.toURL().readText())
            .asJsonObject.getAsJsonArray("versions")
        return versions.map { it.asString }
    }

    /**
     * Used to get a list of all supported for `AdvancedSlimePaper`.
     *
     * @return A list of all supported versions for `AdvancedSlimePaper`.
     */
    fun asp(): List<String> {
        val url = URI.create(Repositories.ASP_REPO)
        val versions: MutableSet<String> = mutableSetOf()
        for (element in JsonParser.parseString(url.toURL().readText()).asJsonArray) versions.addAll(element.asJsonObject["mcVersion"].asJsonArray.map { it.asString })
        return versions.toList()
    }

    /**
     * Used to get a list of all supported for `Pufferfish`.
     *
     * @return A list of all supported versions for `Pufferfish`.
     */
    fun pufferfish(): List<String> {
        val url = URI(Repositories.PUFFERFISH_REPO.replace("/job", "/api/json"))
        val versions = JsonParser.parseString(url.toURL().readText())
            .asJsonObject.getAsJsonArray("jobs").filter {
                it.asJsonObject["name"].asString.contains("Pufferfish-1")
            }.map { it.asJsonObject["name"].asString.split("-")[1] }
        return versions
    }

    private fun paperRepoVersions(projectName: String): List<String> {
        val url = URI("${Repositories.PAPERMC_REPO}/$projectName")
        val versions = JsonParser.parseString(url.toURL().readText())
            .asJsonObject.getAsJsonArray("versions")
        return versions.map { it.asString }
    }

}