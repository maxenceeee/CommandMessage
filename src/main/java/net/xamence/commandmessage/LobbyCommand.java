package net.xamence.commandmessage;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

public class LobbyCommand implements SimpleCommand {

    private ProxyServer proxyServer;

    public LobbyCommand(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("Only for players!"));
            return;
        }

        ConnectionRequestBuilder requestBuilder = player.createConnectionRequest(proxyServer.getServer("lobby").get());
        requestBuilder.fireAndForget();
    }
}
