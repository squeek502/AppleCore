package squeek.applecore.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import squeek.applecore.ModInfo;
import squeek.applecore.api.AppleCoreAPI;

public class SyncHandler
{
	public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.MODID);

	public static void init()
	{
		CHANNEL.registerMessage(MessageDifficultySync.class, MessageDifficultySync.class, 0, Side.CLIENT);
		CHANNEL.registerMessage(MessageExhaustionSync.class, MessageExhaustionSync.class, 1, Side.CLIENT);
		CHANNEL.registerMessage(MessageSaturationSync.class, MessageSaturationSync.class, 2, Side.CLIENT);

		MinecraftForge.EVENT_BUS.register(new SyncHandler());
	}

	/*
	 * Sync saturation (vanilla MC only syncs when it hits 0)
	 * Sync exhaustion (vanilla MC does not sync it at all)
	 * Sync difficulty (vanilla MC does not sync it on servers)
	 */
	private float lastSaturationLevel = 0;
	private float lastExhaustionLevel = 0;
	private EnumDifficulty lastDifficultySetting = null;

	@SubscribeEvent
	public void onLivingUpdateEvent(LivingUpdateEvent event)
	{
		if (!(event.getEntity() instanceof EntityPlayerMP))
			return;

		EntityPlayerMP player = (EntityPlayerMP) event.getEntity();

		if (this.lastSaturationLevel != player.getFoodStats().getSaturationLevel())
		{
			CHANNEL.sendTo(new MessageSaturationSync(player.getFoodStats().getSaturationLevel()), player);
			this.lastSaturationLevel = player.getFoodStats().getSaturationLevel();
		}

		float exhaustionLevel = AppleCoreAPI.accessor.getExhaustion(player);
		if (Math.abs(this.lastExhaustionLevel - exhaustionLevel) >= 0.01f)
		{
			CHANNEL.sendTo(new MessageExhaustionSync(exhaustionLevel), player);
			this.lastExhaustionLevel = exhaustionLevel;
		}
	}

	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
			return;

		if (event.world instanceof WorldServer)
		{
			if (this.lastDifficultySetting != event.world.getDifficulty())
			{
				CHANNEL.sendToAll(new MessageDifficultySync(event.world.getDifficulty()));
				this.lastDifficultySetting = event.world.getDifficulty();
			}
		}
	}
}
