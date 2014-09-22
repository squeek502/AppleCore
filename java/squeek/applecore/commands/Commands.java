package squeek.applecore.commands;

import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;

public class Commands
{
	public static void init(MinecraftServer server)
	{
		CommandHandler commandHandler = (CommandHandler) server.getCommandManager();

		commandHandler.registerCommand(new CommandHunger());
	}
}
