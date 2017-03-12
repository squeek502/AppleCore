package squeek.applecore.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import squeek.applecore.api.AppleCoreAPI;

import java.util.List;

public class CommandHunger extends CommandBase
{
	@Override
	public String getName()
	{
		return "hunger";
	}

	@Override
	public String getUsage(ICommandSender icommandsender)
	{
		return "applecore.commands.hunger.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException
	{
		if (args.length > 0)
		{
			EntityPlayerMP playerToActOn = args.length >= 2 ? getPlayer(server, commandSender, args[1]) : getCommandSenderAsPlayer(commandSender);
			int newHunger = args.length >= 2 ? parseInt(args[1], 0, 20) : parseInt(args[0], 0, 20);

			AppleCoreAPI.mutator.setHunger(playerToActOn, newHunger);
			if (playerToActOn.getFoodStats().getSaturationLevel() > newHunger)
				AppleCoreAPI.mutator.setSaturation(playerToActOn, newHunger);

			notifyCommandListener(commandSender, this, 1, "applecore.commands.hunger.set.hunger.to", playerToActOn.getDisplayName(), newHunger);
		}
		else
		{
			throw new WrongUsageException(getUsage(commandSender));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		if (args.length == 1)
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		else
			return null;
	}
}
