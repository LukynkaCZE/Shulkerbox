package cz.lukynka.shulkerbox.dockyard

import cz.lukynka.shulkerbox.dockyard.git.GitIntegration
import io.github.dockyardmc.commands.Commands

object ShulkerboxIntegration {

    fun load() {
        ShulkerboxConfigManager.load()
        Commands.add("/shulkerbox") {
            withPermission("shulkerbox.commands")

            if(ShulkerboxConfigManager.currentConfig.git.gitIntegrationEnabled) {
                addSubcommand("/pull") {
                    execute {
                        it.sendMessage("<yellow>Pulling maps from git..")
                        GitIntegration.pull()
                        it.sendMessage("<lime>Maps pulled from git!")
                    }
                }
            }
        }
    }
}