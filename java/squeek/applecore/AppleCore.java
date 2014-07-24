package squeek.applecore;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import squeek.applecore.asm.TransformerModuleHandler;
import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.SortingIndex(-100)
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class AppleCore extends DummyModContainer implements IFMLLoadingPlugin
{
	public static Logger Log = LogManager.getLogger(ModInfo.MODID);
	
    public AppleCore() {
        super(MetadataCollection.from(MetadataCollection.class.getResourceAsStream("/applecore.info"), ModInfo.MODID).getMetadataForId(ModInfo.MODID, null));
    }
    
	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{TransformerModuleHandler.class.getName()};
	}

	@Override
	public String getModContainerClass()
	{
		return AppleCore.class.getName();
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
