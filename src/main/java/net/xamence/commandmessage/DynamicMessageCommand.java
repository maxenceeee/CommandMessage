package net.xamence.commandmessage;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class DynamicMessageCommand implements SimpleCommand {

    private volatile String message;


    public DynamicMessageCommand(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void execute(Invocation invocation) {
        invocation.source().sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
