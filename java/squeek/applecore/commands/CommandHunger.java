package squeek.applecore.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import squeek.applecore.api.AppleCoreAPI;

public class CommandHunger {

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal("hunger").requires((p) -> p.hasPermissionLevel(4)).executes(c -> sendUsage(c.getSource()))
                .then(Commands.argument("newHunger", IntegerArgumentType.integer()).executes(c -> execute(c.getSource(), IntegerArgumentType.getInteger(c, "newHunger")))));
    }

    private static int sendUsage(CommandSource source)
    {
        source.sendFeedback(new TranslationTextComponent("applecore.commands.hunger.usage"), true);
        return 0;
    }


    private static int execute(CommandSource source, int newHunger) throws CommandSyntaxException
    {
        ServerPlayerEntity playerToActOn = source.asPlayer();

        AppleCoreAPI.mutator.setHunger(playerToActOn, newHunger);
        if (playerToActOn.getFoodStats().getSaturationLevel() > newHunger)
            AppleCoreAPI.mutator.setSaturation(playerToActOn, newHunger);

        source.sendFeedback(new TranslationTextComponent("applecore.commands.hunger.set.hunger.to", playerToActOn.getDisplayName(), newHunger), true);
        return 0;
    }
}