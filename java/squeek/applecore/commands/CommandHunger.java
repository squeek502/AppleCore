package squeek.applecore.commands;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;
import squeek.applecore.api.AppleCoreAPI;

public class CommandHunger extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "hunger";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return StatCollector.translateToLocal("applecore.commands.hunger.usage");
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] args)
	{
		if (args.length > 0)
		{
			EntityPlayerMP playerToActOn = args.length >= 2 ? getPlayer(commandSender, args[1]) : getCommandSenderAsPlayer(commandSender);
			int newHunger = args.length >= 2 ? parseIntBounded(commandSender, args[1], 0, 20) : parseIntBounded(commandSender, args[0], 0, 20);

			AppleCoreAPI.mutator.setHunger(playerToActOn, newHunger);
			if (playerToActOn.getFoodStats().getSaturationLevel() > newHunger)
				AppleCoreAPI.mutator.setSaturation(playerToActOn, newHunger);

			func_152374_a(commandSender, this, 1, StatCollector.translateToLocalFormatted("applecore.commands.hunger.set.hunger.to", playerToActOn.getDisplayName(), newHunger));
		}
		else
		{
			throw new WrongUsageException(getCommandUsage(commandSender));
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender commandSender, String[] curArgs)
	{
		if (curArgs.length == 1)
			return getListOfStringsMatchingLastWord(curArgs, MinecraftServer.getServer().getAllUsernames());
		else
			return null;
	}

	@Override
	public int compareTo(Object obj)
	{
		if (obj instanceof ICommand)
			return super.compareTo((ICommand) obj);
		else
			return 0;
	}

	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj) || (obj instanceof ICommand && compareTo(obj) == 0);
	}

	@Override
	public int hashCode()
	{
		return getCommandName().hashCode();
	}
}
