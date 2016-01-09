package squeek.applecore.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import squeek.applecore.ModConfig;
import squeek.applecore.ModInfo;

public class GuiAppleCoreConfig extends GuiConfig {
    public GuiAppleCoreConfig(GuiScreen parentScreen) {
        super(parentScreen, new ConfigElement(ModConfig.config.getCategory(ModConfig.CATEGORY_CLIENT)).getChildElements(), ModInfo.MODID, false, false, GuiConfig.getAbridgedConfigPath(ModConfig.config.toString()));
    }
}