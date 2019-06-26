package squeek.applecore.commands;

public class CommandHunger
{
	/* TODO
	@Override
	@Nonnull
	public String getName()
	{
		return "hunger";
	}

	@Override
	@Nonnull
	public String getUsage(@Nonnull ICommandSender sender)
	{
		return "applecore.commands.hunger.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender commandSender, @Nonnull String[] args) throws CommandException
	{
		if (args.length > 0)
		{
			ServerPlayerEntity playerToActOn = args.length >= 2 ? getPlayer(server, commandSender, args[1]) : getCommandSenderAsPlayer(commandSender);
			int maxHunger = AppleCoreAPI.accessor.getMaxHunger(playerToActOn);
			int newHunger = args.length >= 2 ? parseInt(args[1], 0, maxHunger) : parseInt(args[0], 0, maxHunger);

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
	@Nonnull
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		if (args.length == 1)
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		else
			return Collections.emptyList();
	}
	*/
}