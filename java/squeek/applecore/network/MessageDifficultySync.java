package squeek.applecore.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.Difficulty;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageDifficultySync
{
	Difficulty difficulty;

	public MessageDifficultySync(Difficulty difficulty)
	{
		this.difficulty = difficulty;
	}

	public static void encode(MessageDifficultySync pkt, PacketBuffer buf)
	{
		buf.writeInt(pkt.difficulty.getId());
	}

	public static MessageDifficultySync decode(PacketBuffer buf)
	{
		return new MessageDifficultySync(Difficulty.byId(buf.readInt()));
	}

	public static void handle(final MessageDifficultySync message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			NetworkHelper.getSidedPlayer(ctx.get()).world.getWorldInfo().setDifficulty(message.difficulty);
		});
		ctx.get().setPacketHandled(true);
	}
}