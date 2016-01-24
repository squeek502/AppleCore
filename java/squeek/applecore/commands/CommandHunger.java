package squeek.applecore.commands;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import squeek.applecore.api.AppleCoreAPI;
import java.util.List;

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
	public void processCommand(ICommandSender commandSender, String[] args) throws CommandException
	{
		if (args.length > 0)
		{
			EntityPlayerMP playerToActOn = args.length >= 2 ? getPlayer(commandSender, args[1]) : getCommandSenderAsPlayer(commandSender);
			int newHunger = args.length >= 2 ? parseInt(args[1], 0, 20) : parseInt(args[0], 0, 20);

			AppleCoreAPI.mutator.setHunger(playerToActOn, newHunger);
			if (playerToActOn.getFoodStats().getSaturationLevel() > newHunger)
				AppleCoreAPI.mutator.setSaturation(playerToActOn, newHunger);

			notifyOperators(commandSender, this, 1, StatCollector.translateToLocalFormatted("applecore.commands.hunger.set.hunger.to", playerToActOn.getDisplayName(), newHunger));
		}
		else
		{
			throw new WrongUsageException(getCommandUsage(commandSender));
		}
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
	{
		if (args.length == 1)
			return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		else
			return null;
	}
}