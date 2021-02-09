package squeek.applecore;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.applecore.api_impl.AppleCoreAccessorMutatorImpl;
import squeek.applecore.api_impl.AppleCoreRegistryImpl;
import squeek.applecore.commands.CommandHunger;
import squeek.applecore.network.SyncHandler;

@Mod(ModInfo.MODID)
public class AppleCore
{
	public static final Logger LOG = LogManager.getLogger(ModInfo.MODID);

	public AppleCore()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void init(final FMLCommonSetupEvent event)
	{
		// force initialization of the singletons
		AppleCoreAccessorMutatorImpl.values();
		AppleCoreRegistryImpl.values();
		AppleCoreRegistryImpl.INSTANCE.init();
		SyncHandler.init();
	}

	@SubscribeEvent
	public void onCommandRegistering(RegisterCommandsEvent event)
	{
		CommandHunger.register(event.getDispatcher());
	}
}