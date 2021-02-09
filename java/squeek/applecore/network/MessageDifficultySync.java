package squeek.applecore.network;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
		PlayerEntity player = NetworkHelper.getSidedPlayer(ctx.get());
		if (player.world instanceof ClientWorld) {
			ClientWorld clientWorld = (ClientWorld) player.world;
			ctx.get().enqueueWork(() -> clientWorld.getWorldInfo().setDifficulty(message.difficulty));
		}
		ctx.get().setPacketHandled(true);
	}
}