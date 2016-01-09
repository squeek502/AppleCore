package squeek.applecore;

import java.util.Locale;

public final class ModInfo
{
	public static final String MODID = "AppleCore";
	public static final String MODAPI = "AppleCoreAPI";
	public static final String VERSION = "${version}";
	public static final String APIVERSION = "${apiversion}";
	public static final String MODID_LOWER = ModInfo.MODID.toLowerCase(Locale.ROOT);
	public static final String GUI_FACTORY_CLASS = "squeek.applecore.client.gui.GuiFactory";
}