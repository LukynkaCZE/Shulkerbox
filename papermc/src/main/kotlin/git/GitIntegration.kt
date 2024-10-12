package git

import ShulkerboxMap
import config.ConfigManager
import org.bukkit.entity.Player
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

object GitIntegration {

    lateinit var git: Git

    fun load() {
        val config = ConfigManager.currentConfig.git
        val credentials = UsernamePasswordCredentialsProvider(config.gitUser, config.gitPassword)
        val folder = File("plugins/Shulkerbox/git/")
        if(!folder.exists()) folder.mkdirs()
        val files = folder.listFiles()?.toList() ?: listOf()

        git = if(files.firstOrNull { it.name.contains(".git") } != null) {
            Git.open(folder)
        } else {
            Git.cloneRepository()
                .setBranch("main")
                .setDirectory(folder)
                .setURI(config.gitUrl)
                .setCredentialsProvider(credentials)
                .call()
        }
        pull()
    }

    fun pull() {
        val config = ConfigManager.currentConfig.git
        val credentials = UsernamePasswordCredentialsProvider(config.gitUser, config.gitPassword)
        git.pull()
            .setCredentialsProvider(credentials)
            .setRemoteBranchName("main")
            .call()
    }

    fun commit(map: ShulkerboxMap, commitMessage: String, committee: Player) {

        val config = ConfigManager.currentConfig.git
        val credentials = UsernamePasswordCredentialsProvider(config.gitUser, config.gitPassword)

        val mapFile = File("plugins/Shulkerbox/maps/${map.id}.shulker")
        mapFile.copyTo(File("plugins/Shulkerbox/git/${map.id}.shulker"), true)

        git.add().addFilepattern(".").setUpdate(false).call()
        git.commit()
            .setAuthor(committee.name, "${committee.name}@shulker.box")
            .setMessage("(${map.id}) $commitMessage")
            .call()
        git.push()
            .setCredentialsProvider(credentials)
            .call()
    }
}