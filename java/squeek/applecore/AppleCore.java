package squeek.applecore;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.applecore.api_impl.AppleCoreAccessorMutatorImpl;
import squeek.applecore.asm.TransformerModuleHandler;
import squeek.applecore.commands.Commands;
import squeek.applecore.network.SyncHandler;
import squeek.asmhelper.applecore.ObfHelper;

import java.io.InputStream;
import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(1100)
@IFMLLoadingPlugin.MCVersion("1.12.1")
@IFMLLoadingPlugin.TransformerExclusions({"squeek.applecore.asm", "squeek.asmhelper"})
@Mod(modid = ModInfo.MODID, name = ModInfo.MODNAME, version = ModInfo.VERSION, acceptedMinecraftVersions="[1.12.1]", acceptableRemoteVersions = "*", dependencies = "required-after:forge@[14.22,)")
public class AppleCore implements IFMLLoadingPlugin
{
	public static final Logger LOG = LogManager.getLogger(ModInfo.MODID);

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		// too lazy to figure out a real solution for this (@Mod enforces mcmod.info filename)
		// this will at least allow the metadata to populate the mod listing, though
		InputStream is = MetadataCollection.class.getResourceAsStream("/applecore.info");
		MetadataCollection metadataCollection = MetadataCollection.from(is, ModInfo.MODID);
		Loader.instance().activeModContainer().bindMetadata(metadataCollection);

		// force initialization of the singletons
		AppleCoreAccessorMutatorImpl.values();

		FMLInterModComms.sendRuntimeMessage(ModInfo.MODID, "versionchecker", "addVersionCheck", "http://www.ryanliptak.com/minecraft/versionchecker/squeek502/AppleCore");
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		SyncHandler.init();
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		Commands.init(event.getServer());
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
		ObfHelper.setObfuscated((Boolean) data.get("runtimeDeobfuscationEnabled"));
		ObfHelper.setRunsAfterDeobfRemapper(true);
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}