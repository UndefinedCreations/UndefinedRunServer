package com.undefinedcreations.runServer.lib.links

import com.google.gson.JsonParser
import java.io.File
import java.io.FileOutputStream
import java.net.URI


/**
 * This class is used to download the server jar selected.
 *
 * @since 1.0.0
 */

object DownloadLib {

    /**
     * This method is used to download PaperMC.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The minecraft version target
     */
    fun paper(folder: File, mcVersion: String) =
        downloadFromPaperMCRepo(folder, mcVersion, "paper")

    /**
     * This method is used to download Waterfall proxy.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The minecraft version target
     */
    fun waterfall(folder: File, mcVersion: String) =
        downloadFromPaperMCRepo(folder, mcVersion, "waterfall")

    /**
     * This method is used to download Velocity proxy.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The minecraft version target
     */
    fun velocity(folder: File, mcVersion: String) =
        downloadFromPaperMCRepo(folder, mcVersion, "velocity")

    /**
     * This method is used to download Folia.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The minecraft version target
     */
    fun folia(folder: File, mcVersion: String) =
        downloadFromPaperMCRepo(folder, mcVersion, "folia")

    /**
     * This method is used to download Spigot.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The minecraft version target
     */
    fun spigot(folder: File, mcVersion: String) =
        downloadFromUndefinedCreations(folder, mcVersion)

    /**
     * This method is used to download Bungeecord proxy.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The minecraft version target
     */
    fun bungeecord(folder: File): DownloadResult =
        downloadFile(folder, Repositories.BUNGEECORD_REPO, "Bungeecord.jar")

    /**
     * This method is used to download Purper.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The minecraft version target
     */
    fun purper(folder: File, mcVersion: String): DownloadResult =
        downloadFile(folder, "${Repositories.PURPER_REPO}/$mcVersion/latest/download", "Purper.jar")

    /**
     * This method is used to download Pufferfish.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The minecraft version target
     */
    fun pufferfish(folder: File, mcVersion: String): DownloadResult {
        val mainUrl = URI("${Repositories.PUFFERFISH_REPO}/Pufferfish-$mcVersion/lastSuccessfulBuild")
        val buildUrl = URI("$mainUrl/api/json")

        val path = JsonParser.parseString(buildUrl.toURL().readText())
            .asJsonObject.getAsJsonArray("artifacts")
            .get(0).asJsonObject.get("relativePath").asString

        return downloadFile(folder, "$mainUrl/artifact/$path", "PufferFish.jar")
    }


    /**
     * This method is used to download file from the PaperMC repository.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The jar version to download
     * @param projectName The project name to download from the repository
     */
    private fun downloadFromPaperMCRepo(folder: File, mcVersion: String, projectName: String): DownloadResult {
        val url = URI("${Repositories.PAPERMC_REPO}/$projectName/versions/$mcVersion")
        val builds = JsonParser.parseString(url.toURL().readText())
            .asJsonObject.getAsJsonArray("builds")
        val latestBuild = builds.last().asInt

        return downloadFile(folder, "$url/builds/$latestBuild/downloads/$projectName-$mcVersion-$latestBuild.jar", "$projectName.jar")
    }

    /**
     * This method is used to download file from the UndefinedCreations repository.
     *
     * @param folder The folder to download the jar to
     * @param mcVersion The jar version to download
     */
    private fun downloadFromUndefinedCreations(folder: File, mcVersion: String): DownloadResult =
        downloadFile(folder, "${Repositories.UNDEFINEDCREATION_REPO}/spigot-$mcVersion.jar", "spigot.jar")

    /**
     * This method is used to download a file from an url.
     *
     * @param folder The folder to download the jar to
     * @param downloadURL The url to download from
     * @param name The name to give the downloaded file
     */
    fun downloadFile(folder: File, downloadURL: String, name: String): DownloadResult =
        downloadFile(folder, URI(downloadURL), name)

    /**
     * This method is used to download a file from an url.
     *
     * @param folder The folder to download the jar to
     * @param downloadURL The url to download from
     * @param name The name to give the downloaded file
     *
     * @return This method will return the download result if it fails or not
     */
    private fun downloadFile(folder: File, downloadURL: URI, name: String): DownloadResult {
        val file = File(folder, name)

        return if (file.exists()) {
            DownloadResult(DownloadResultType.SUCCESS, null, file)
        } else {
            try {
                downloadURL.toURL().openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                DownloadResult(DownloadResultType.SUCCESS, null, file)
            } catch (exception: Exception) {
                DownloadResult(DownloadResultType.FAILED, exception.message, null)
            }
        }
    }
}

