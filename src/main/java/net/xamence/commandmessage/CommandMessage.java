package net.xamence.commandmessage;


import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(
        id = "commandmessage",
        name = "Command Message",
        version = BuildConstants.VERSION,
        description = "The simple command creator to send a generic message to the player executor (for Cuboria)",
        url = "https://www.xamence.net",
        authors = {"Xamence"}
)
public class CommandMessage {


    private Logger logger;

    private ProxyServer proxy;

    private YamlDocument pluginConfig;
    private Map<String, String> commandMessageMap;

    public CommandMessage(ProxyServer proxy, Logger logger, @DataDirectory Path pluginDirectory) {
        this.proxy = proxy;
        this.logger = logger;

        this.commandMessageMap = new HashMap<>();

        try {
            this.pluginConfig = YamlDocument.create(new File(pluginDirectory.toFile(), "config.yml"), getClass().getResourceAsStream("/config.yml"));

            this.pluginConfig.update();
            this.pluginConfig.save();
        } catch (IOException e) {
            this.logger.error("Can't use the config file !");
            proxy.getPluginManager().getPlugin("commandmessage").ifPresent((pluginContainer -> pluginContainer.getExecutorService().shutdown()));
        }

    }


    private void registerCommands() {
        for(Object keys : this.pluginConfig.getKeys()) {
            String commandName = String.valueOf(keys);
            String messageFromJSON = this.pluginConfig.getString(commandName);

            this.commandMessageMap.put(commandName, messageFromJSON);

            CommandManager commandManager = proxy.getCommandManager();;


            commandManager.register(commandManager.metaBuilder(commandName).plugin(this).build(), new SimpleCommand() {
                @Override
                public void execute(Invocation invocation) {
                    invocation.source().sendMessage(MiniMessage.miniMessage().deserialize(messageFromJSON));
                }
            });
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.logger.info("CommandMessage Initializing....");

        this.registerCommands();
    }
}
