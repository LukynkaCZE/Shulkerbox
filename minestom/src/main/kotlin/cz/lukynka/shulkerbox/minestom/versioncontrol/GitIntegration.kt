package cz.lukynka.shulkerbox.minestom.versioncontrol

import cz.lukynka.shulkerbox.minestom.ShulkerboxConfigManager
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

object GitIntegration {

    lateinit var git: Git

    fun load() {
        val config = ShulkerboxConfigManager.currentConfig.git
        val credentials = UsernamePasswordCredentialsProvider(config.gitUser, config.gitPassword)
        val folder = File("./shulkerbox/maps/")
        if (!folder.exists()) folder.mkdirs()
        val files = folder.listFiles()?.toList() ?: listOf()

        git = if (files.firstOrNull { it.name.contains(".git") } != null) {
            Git.open(folder)
        } else {
            Git.cloneRepository()
                .setBranch("master")
                .setDirectory(folder)
                .setURI(config.gitUrl)
                .setCredentialsProvider(credentials)
                .call()
        }
        println("Loaded git integration")
        pull()
    }

    fun pull() {
        val config = ShulkerboxConfigManager.currentConfig.git
        val credentials = UsernamePasswordCredentialsProvider(config.gitUser, config.gitPassword)
        git.pull()
            .setCredentialsProvider(credentials)
            .setRemoteBranchName("master")
            .call()
    }
}