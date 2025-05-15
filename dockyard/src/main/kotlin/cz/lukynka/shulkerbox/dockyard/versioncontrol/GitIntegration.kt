package cz.lukynka.shulkerbox.dockyard.versioncontrol

import cz.lukynka.prettylog.LogType
import cz.lukynka.prettylog.log
import cz.lukynka.shulkerbox.dockyard.ShulkerboxConfigManager
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
                .setBranch("main")
                .setDirectory(folder)
                .setURI(config.gitUrl)
                .setCredentialsProvider(credentials)
                .call()
        }
        log("Loaded git integration", LogType.SUCCESS)
        pull()
    }

    fun pull() {
        val config = ShulkerboxConfigManager.currentConfig.git
        val credentials = UsernamePasswordCredentialsProvider(config.gitUser, config.gitPassword)
        git.pull()
            .setCredentialsProvider(credentials)
            .setRemoteBranchName("main")
            .call()
    }
}