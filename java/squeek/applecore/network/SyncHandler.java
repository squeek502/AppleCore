package squeek.applecore.network;

import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import squeek.applecore.ModConfig;
import squeek.applecore.ModInfo;
import squeek.applecore.api.AppleCoreAPI;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SyncHandler
{
	public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.MODID);

	public static void init()
	{
		channel.registerMessage(MessageDifficultySync.class, MessageDifficultySync.class, 0, Side.CLIENT);
		channel.registerMessage(MessageExhaustionSync.class, MessageExhaustionSync.class, 1, Side.CLIENT);
		channel.registerMessage(MessageSaturationSync.class, MessageSaturationSync.class, 2, Side.CLIENT);

		SyncHandler syncHandler = new SyncHandler();
		FMLCommonHandler.instance().bus().register(syncHandler);
		MinecraftForge.EVENT_BUS.register(syncHandler);
	}

	/*
	 * Sync saturation (vanilla MC only syncs when it hits 0)
	 * Sync exhaustion (vanilla MC does not sync it at all)
	 * Sync difficulty (vanilla MC does not sync it on servers)
	 */
	private static final Map<UUID, Float> lastSaturationLevels = new HashMap<UUID, Float>();
	private static final Map<UUID, Float> lastExhaustionLevels = new HashMap<UUID, Float>();
	private EnumDifficulty lastDifficultySetting = null;

	@SubscribeEvent
	public void onLivingUpdateEvent(LivingUpdateEvent event)
	{
		if (!(event.entity instanceof EntityPlayerMP))
			return;

		EntityPlayerMP player = (EntityPlayerMP) event.entity;
		Float lastSaturationLevel = lastSaturationLevels.get(player.getUniqueID());
		Float lastExhaustionLevel = lastExhaustionLevels.get(player.getUniqueID());

		if (lastSaturationLevel == null || lastSaturationLevel != player.getFoodStats().getSaturationLevel())
		{
			channel.sendTo(new MessageSaturationSync(player.getFoodStats().getSaturationLevel()), player);
			lastSaturationLevels.put(player.getUniqueID(), player.getFoodStats().getSaturationLevel());
		}

		float exhaustionLevel = AppleCoreAPI.accessor.getExhaustion(player);
		if (lastExhaustionLevel == null || Math.abs(lastExhaustionLevel - exhaustionLevel) >= ModConfig.EXHAUSTION_SYNC_THRESHOLD)
		{
			channel.sendTo(new MessageExhaustionSync(exhaustionLevel), player);
			lastExhaustionLevels.put(player.getUniqueID(), exhaustionLevel);
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if (!(event.player instanceof EntityPlayerMP))
			return;

		lastSaturationLevels.remove(event.player.getUniqueID());
		lastExhaustionLevels.remove(event.player.getUniqueID());
		channel.sendTo(new MessageDifficultySync(event.player.worldObj.difficultySetting), (EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
			return;

		if (event.world instanceof WorldServer)
		{
			if (this.lastDifficultySetting != event.world.difficultySetting)
			{
				channel.sendToAll(new MessageDifficultySync(event.world.difficultySetting));
				this.lastDifficultySetting = event.world.difficultySetting;
			}
		}
	}
}
