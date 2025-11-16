package net.xamence.commandmessage;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class DynamicMessageCommand implements SimpleCommand {

    private volatile String message;
    private static MiniMessage miniMessage = MiniMessage.miniMessage();
    private boolean active;

    public DynamicMessageCommand(String message) {
        this.message = message;
        this.active = true;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!active) return;
        invocation.source().sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
