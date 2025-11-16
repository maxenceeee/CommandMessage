package net.xamence.commandmessage;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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


    private final Logger logger;

    private final ProxyServer proxy;

    private YamlDocument pluginConfig;
    private final Map<String, DynamicMessageCommand> commandMessageMap;

    @Inject
    public CommandMessage(ProxyServer proxy, Logger logger, @DataDirectory Path pluginDirectory) {
        this.proxy = proxy;
        this.logger = logger;

        this.commandMessageMap = new HashMap<>();

        try {
            File configFile = new File(pluginDirectory.toFile(), "config.yml");


            if (!configFile.exists()) {
                try (InputStream defaultConfig = getClass().getResourceAsStream("/config.yml")) {
                    this.pluginConfig = YamlDocument.create(
                            configFile,
                            defaultConfig,
                            GeneralSettings.DEFAULT,
                            LoaderSettings.builder().setAutoUpdate(false).build(),
                            DumperSettings.DEFAULT,
                            UpdaterSettings.DEFAULT
                    );
                    this.pluginConfig.save();
                    this.logger.info("Config.yml created successfully.");
                }
            } else {
                this.pluginConfig = YamlDocument.create(
                        configFile,
                        GeneralSettings.DEFAULT,
                        LoaderSettings.builder().setAutoUpdate(false).build(),
                        DumperSettings.DEFAULT
                );
                this.pluginConfig.reload();
                this.logger.info("Config.yml loaded successfully.");
            }
        } catch (IOException e) {
            this.logger.error("Can't use the config file !");
            proxy.getPluginManager().getPlugin("commandmessage").ifPresent((pluginContainer -> pluginContainer.getExecutorService().shutdown()));
        }
    }


    private void registerCommands() {
        for(Object keys : this.pluginConfig.getKeys()) {
            String commandName = String.valueOf(keys);
            String messageFromJSON = this.pluginConfig.getString(commandName);

            DynamicMessageCommand currentCommand = this.commandMessageMap.get(commandName);

            if (currentCommand == null) {
                currentCommand = new DynamicMessageCommand(messageFromJSON);

                this.commandMessageMap.put(commandName, currentCommand);

                CommandManager commandManager = proxy.getCommandManager();
                commandManager.register(commandManager.metaBuilder(commandName).plugin(this).build(), currentCommand);
            } else {
                currentCommand.setMessage(messageFromJSON);
            }

        }
    }

    private void reloadConfig() {
        try {
            this.pluginConfig.reload();
            this.commandMessageMap.clear();
            this.registerCommands();
            this.logger.info("Configuration reloaded successfully.");
        } catch (IOException e) {
            this.logger.error("Error while reloading configuration!", e);
        }
    }


    private void registerMainCommand() {
        CommandManager commandManager = this.proxy.getCommandManager();

        commandManager.register(
                commandManager.metaBuilder("commandmessage").aliases("cm").plugin(this).build(),
                (SimpleCommand) invocation -> {
                    String[] args = invocation.arguments();

                    if(args.length == 1 && args[0].equals("reload")) {
                        this.reloadConfig();
                        invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<green>Rechargement des commandes : <b>exécuté</b>.</green>"));
                        return;
                    }

                    invocation.source().sendMessage(MiniMessage.miniMessage().deserialize("<red>Utilise plutôt : <click:suggest_command:'/commandmessage reload'><u>/commandmessage reload</u></click></red>"));
                }
        );
        commandManager.register(commandManager.metaBuilder("lobby").plugin(this).build(), new LobbyCommand(proxy));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.logger.info("CommandMessage Initializing....");

        this.registerCommands();
        this.registerMainCommand();
    }
}
