package squeek.applecore;

import java.io.InputStream;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.applecore.api_impl.AppleCoreAccessorMutatorImpl;
import squeek.applecore.asm.TransformerModuleHandler;
import squeek.applecore.asm.helpers.ObfHelper;
import squeek.applecore.client.DebugInfoHandler;
import squeek.applecore.client.HUDOverlayHandler;
import squeek.applecore.client.TooltipOverlayHandler;
import squeek.applecore.network.SyncHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.Side;

@IFMLLoadingPlugin.SortingIndex(-100)
@IFMLLoadingPlugin.MCVersion("1.7.10")
@Mod(modid = ModInfo.MODID, version = ModInfo.VERSION)
public class AppleCore implements IFMLLoadingPlugin
{
	public static Logger Log = LogManager.getLogger(ModInfo.MODID);

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		// too lazy to figure out a real solution for this (@Mod enforces mcmod.info filename)
		// this will at least allow the metadata to populate the mod listing, though
		InputStream is = MetadataCollection.class.getResourceAsStream("/applecore.info");
		MetadataCollection metadataCollection = MetadataCollection.from(is, ModInfo.MODID);
		Loader.instance().activeModContainer().bindMetadata(metadataCollection);

		// force initialization of the singleton
		AppleCoreAccessorMutatorImpl.values();

		ModConfig.init(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		SyncHandler.init();

		if (event.getSide() == Side.CLIENT)
		{
			DebugInfoHandler.init();
			HUDOverlayHandler.init();
			TooltipOverlayHandler.init();
		}
	}

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{TransformerModuleHandler.class.getName()};
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		ObfHelper.obfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
