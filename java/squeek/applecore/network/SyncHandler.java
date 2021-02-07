package squeek.applecore.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import squeek.applecore.ModInfo;

public class SyncHandler
{
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
		.named(new ResourceLocation(ModInfo.MODID, "sync"))
		.clientAcceptedVersions(s -> true)
		.serverAcceptedVersions(s -> true)
		.networkProtocolVersion(() -> PROTOCOL_VERSION)
		.simpleChannel();

	public static void init()
	{
		CHANNEL.registerMessage(1, MessageDifficultySync.class, MessageDifficultySync::encode, MessageDifficultySync::decode, MessageDifficultySync::handle);

		MinecraftForge.EVENT_BUS.register(new SyncHandler());
	}

	/*
	 * Sync difficulty (vanilla MC does not sync it on servers)
	 */
	private Difficulty lastDifficultySetting = null;

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if (!(event.getPlayer() instanceof ServerPlayerEntity))
			return;

		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

		MessageDifficultySync msg = new MessageDifficultySync(event.getPlayer().world.getDifficulty());
		CHANNEL.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
			return;

		if (event.type == TickEvent.Type.SERVER)
		{
			if (this.lastDifficultySetting != event.world.getDifficulty())
			{
				CHANNEL.send(PacketDistributor.ALL.noArg(), new MessageDifficultySync(event.world.getDifficulty()));
				this.lastDifficultySetting = event.world.getDifficulty();
			}
		}
	}
}