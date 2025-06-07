package com.undefinedcreations.nova

import com.google.gson.JsonParser
import com.undefinedcreations.nova.exception.UnsupportedJavaVersionException
import com.undefinedcreations.nova.lib.TaskLib
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import java.io.File
import java.net.URI
import java.net.URISyntaxException

/**
 * This class is extending `JavaExec`. Which is a task to run jar files
 *
 * @since 1.0.0
 */
abstract class AbstractServer : JavaExec() {

    @get:Internal
    protected var runDir: File? = null
    @get:Internal
    protected var pluginDir: File? = null

    @get:Internal
    protected var serverType: ServerType = ServerType.SPIGOT
    @get:Internal
    protected var minecraftVersion: String? = null

    @get:Internal
    protected var versionFolder: Boolean = false

    /**
     * This is an option to change if there should be a folder for every version
     *
     * @param boolean If a folder should be created per version
     */
    fun perVersionFolder(boolean: Boolean) { versionFolder = boolean }

    /**
     * The minecraft version the server should run
     *
     * @param minecraftVersion The minecraft version
     */
    fun minecraftVersion(minecraftVersion: String) {
        this.minecraftVersion = minecraftVersion
        dependOnTasks()
        if (!serverType.proxy && serverType != ServerType.CUSTOM) checkJavaVersion(minecraftVersion, javaVersion)
    }

    /**
     * This option allowed you to set the folder where the server is running
     *
     * @param folder This gives you the folder data and returns the file to place the server folder
     */
    fun serverFolder(folder: (FolderData).() -> File) { runDir = folder(FolderData(minecraftVersion, serverType, project.layout.projectDirectory.asFile)) }

    /**
     * This option allowed you to set the folder where the server is running
     *
     * @param folder This gives you the folder data and returns the file to place the server folder
     */
    fun serverFolderName(folder: (FolderData).() -> String) { runDir = File(project.layout.projectDirectory.asFile, folder(
        FolderData(minecraftVersion, serverType, project.layout.projectDirectory.asFile)
    )) }

    /**
     * This option allowed you to set the folder where the server is running
     *
     * @param name The server folder name
     */
    fun serverFolderName(name: String) { runDir = File(project.layout.projectDirectory.asFile, name) }

    /**
     * This option allows you to set what type of server you will be running.
     *
     * @param serverType The server type
     */
    fun serverType(serverType: ServerType) { if (serverType != ServerType.CUSTOM) this.serverType = serverType }

    protected fun setRunningDir(file: File) = file.also { runDir = it }
    protected fun setClass(file: File): JavaExec = classpath(file.path)
    protected fun setJvmArgs(args: List<String>): JavaExec = jvmArgs(args)

    /**
     * This sets up the `runDir` of JavaExec
     */
    protected fun setup() {
        standardInput = System.`in`
        if (runDir == null) {
            runDir = File(project.layout.projectDirectory.asFile, "run${if (versionFolder) "/$minecraftVersion" else ""}/${serverType.name.lowercase()}")
        }
        pluginDir = File(runDir, "plugins")
        workingDir(runDir!!.path)
    }

    /**
     * Checks and depends on the correct tasks
     */
    protected fun dependOnTasks() {
        if (TaskLib.TaskNames.REMAP in project.tasks.names) {
            setDependsOn(mutableListOf(project.tasks.named(TaskLib.TaskNames.REMAP)))
        } else if (TaskLib.TaskNames.SHADOW in project.tasks.names) {
            setDependsOn(mutableListOf(project.tasks.named(TaskLib.TaskNames.SHADOW)))
        } else {
            setDependsOn(mutableListOf(project.tasks.named(TaskLib.TaskNames.JAR)))
        }
    }

    /**
     * Throws a [UnsupportedJavaVersionException] if the correct Java version is not used.
     */
    private fun checkJavaVersion(minecraftVersion: String, javaVersion: JavaVersion) {
        try {
            val uri = URI.create("thttps://hub.spigotmc.org/versions/$minecraftVersion.json").toURL()
            val response = JsonParser.parseString(uri.readText()).asJsonObject

            val versionsArray = response["javaVersions"].asJsonArray
            val minJava = versionsArray[0].asInt
            val maxJava = versionsArray[1].asInt

            val classFileMajorVersion: Int = javaVersion.majorVersion.toInt() + 44
            if (classFileMajorVersion in minJava..maxJava) return

            throw UnsupportedJavaVersionException(minJava, maxJava)
        } catch (e: URISyntaxException) {
            logger.error("Could not find $minecraftVersion.", e)
        }
    }

}

/**
 * The folder data that is giving when selecting the folder to make the server go to.
 *
 * @param minecraftVersion The minecraft version the server is running
 * @param serverType The server type that its running
 * @param buildFolder The build folder of your project
 * @since 1.0.0
 */
class FolderData(val minecraftVersion: String?, val serverType: ServerType, val buildFolder: File)