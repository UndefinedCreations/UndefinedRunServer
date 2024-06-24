package com.undefined.runServer.lib

import com.google.gson.JsonParser
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.util.concurrent.CompletableFuture

object DownloadLib {

    private val PAPERMC_REPO = "https://api.papermc.io/v2/projects"
    private val GETBUKKIT_REPO = "https://download.getbukkit.org/"
    private val BUNGEECORD_REPO = "https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar"
    private val PURPER_REPO = "https://api.purpurmc.org/v2/purpur"
    private val PUFFERFISH_REPO = "https://ci.pufferfish.host/job"


    private fun downloadFromPaperMC(folder: File, mcVersion: String, stringProjectName: String): CompletableFuture<DownloadResult> {
        val url = URI("$PAPERMC_REPO/$stringProjectName/versions/$mcVersion")
        val arrayBuildJson = JsonParser.parseString(url.toURL().readText()).asJsonObject.getAsJsonArray("builds")
        val latestBuild = arrayBuildJson.get(arrayBuildJson.size() - 1).asInt

        return downloadFile(folder, "$stringProjectName-$mcVersion-$latestBuild.jar", "$url/builds/$latestBuild/downloads/$stringProjectName-$mcVersion-$latestBuild.jar")
    }
    private fun downloadFromGetBukkit(folder: File, mcVersion: String, stringProjectName: String): CompletableFuture<DownloadResult> = downloadFile(folder, "$stringProjectName-$mcVersion.jar", "$GETBUKKIT_REPO/$stringProjectName/$stringProjectName-$mcVersion.jar")

    fun downloadPaper(folder: File, mcVersion: String) = downloadFromPaperMC(folder, mcVersion, "paper")
    fun downloadWaterfall(folder: File, mcVersion: String) = downloadFromPaperMC(folder, mcVersion, "waterfall")
    fun downloadVelocity(folder: File, mcVersion: String) = downloadFromPaperMC(folder, mcVersion, "velocity")
    fun downloadFolia(folder: File, mcVersion: String) = downloadFromPaperMC(folder, mcVersion, "folia")
    fun downloadSpigot(folder: File, mcVersion: String) = downloadFromGetBukkit(folder, mcVersion, "spigot")
    fun downloadBukkit(folder: File, mcVersion: String) = downloadFromGetBukkit(folder, mcVersion, "craftbukkit")
    fun downloadBungeecord(folder: File, mcVersion: String): CompletableFuture<DownloadResult> = downloadFile(folder, "Bungeecord.jar", BUNGEECORD_REPO)
    fun downloadPurper(folder: File, mcVersion: String): CompletableFuture<DownloadResult> = downloadFile(folder, "Purper-$mcVersion.jar", "$PURPER_REPO/$mcVersion/latest/download")
    fun downloadPufferFish(folder: File, mcVersion: String): CompletableFuture<DownloadResult> {

        val mainURL = URI("$PUFFERFISH_REPO/Pufferfish-$mcVersion/lastSuccessfulBuild")

        val buildURL = URI("$mainURL/api/json")
        val path = JsonParser.parseString(buildURL.toURL().readText()).asJsonObject.getAsJsonArray("artifacts").get(0).asJsonObject.get("relativePath").asString

        return downloadFile(folder, "Pufferfish-$mcVersion.jar", "$mainURL/artifact/$path")
    }



    private fun downloadFile(
        folder: File,
        fileName: String,
        downloadURL: String
    ): CompletableFuture<DownloadResult> = CompletableFuture.supplyAsync {

        val file = File(folder, fileName)

        if (!file.exists()) {
            return@supplyAsync try {
                URI(downloadURL).toURL().openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                DownloadResult(DownloadResultType.SUCCESS, null, file)
            } catch (exception: Exception) {
                DownloadResult(DownloadResultType.FAILED, exception.message, null)
            }
        } else {
            return@supplyAsync DownloadResult(DownloadResultType.SUCCESS, null, file)
        }
    }
}

data class DownloadResult(val downloadResultType: DownloadResultType, val e: String?, val jarFile: File?)

enum class DownloadResultType {
    SUCCESS,
    FAILED
}