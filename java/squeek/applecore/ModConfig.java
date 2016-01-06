package squeek.applecore;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ModConfig
{
	public static final ModConfig instance = new ModConfig();

	protected ModConfig()
	{
	}

	private static Configuration config;

	/*
	 * CLIENT
	 */
	private static final String CATEGORY_CLIENT = "client";
	private static final String CATEGORY_CLIENT_COMMENT =
			"These config settings are client-side only";

	public static boolean SHOW_FOOD_VALUES_IN_TOOLTIP = true;
	private static final String SHOW_FOOD_VALUES_IN_TOOLTIP_NAME = "show.food.values.in.tooltip";
	private static final String SHOW_FOOD_VALUES_IN_TOOLTIP_COMMENT =
			"If true, shows the hunger and saturation values of food in its tooltip while holding SHIFT";

	public static boolean ALWAYS_SHOW_FOOD_VALUES_TOOLTIP = false;
	private static final String ALWAYS_SHOW_FOOD_VALUES_TOOLTIP_NAME = "show.food.values.in.tooltip.always";
	private static final String ALWAYS_SHOW_FOOD_VALUES_TOOLTIP_COMMENT =
			"If true, shows the hunger and saturation values of food in its tooltip automatically (without needing to hold SHIFT)";

	public static boolean SHOW_SATURATION_OVERLAY = true;
	private static final String SHOW_SATURATION_OVERLAY_NAME = "show.saturation.hud.overlay";
	private static final String SHOW_SATURATION_OVERLAY_COMMENT =
			"If true, shows your current saturation level overlayed on the hunger bar";

	public static boolean SHOW_FOOD_VALUES_OVERLAY = true;
	private static final String SHOW_FOOD_VALUES_OVERLAY_NAME = "show.food.values.hud.overlay";
	private static final String SHOW_FOOD_VALUES_OVERLAY_COMMENT =
			"If true, shows the hunger (and saturation if " + SHOW_SATURATION_OVERLAY_NAME + " is true) that would be restored by food you are currently holding";

	@Deprecated
	private static final String SHOW_FOOD_EXHAUSTION_OVERLAY_NAME = "show.food.exhaustion.hud.overlay";

	public static boolean SHOW_FOOD_EXHAUSTION_UNDERLAY = true;
	private static final String SHOW_FOOD_EXHAUSTION_UNDERLAY_NAME = "show.food.exhaustion.hud.underlay";
	private static final String SHOW_FOOD_EXHAUSTION_UNDERLAY_COMMENT =
			"If true, shows your food exhaustion as a progress bar behind the hunger bars";

	public static boolean SHOW_FOOD_DEBUG_INFO = true;
	private static final String SHOW_FOOD_DEBUG_INFO_NAME = "show.food.stats.in.debug.overlay";
	private static final String SHOW_FOOD_DEBUG_INFO_COMMENT =
			"If true, adds a line that shows your hunger, saturation, and exhaustion level in the F3 debug overlay";

	public static void init(File file)
	{
		config = new Configuration(file);

		load();

		/*
		 * CLIENT
		 */
		config.getCategory(CATEGORY_CLIENT).setComment(CATEGORY_CLIENT_COMMENT);

		// rename overlay to underlay
		boolean foodExhaustionOverlayValue = config.get(CATEGORY_CLIENT, SHOW_FOOD_EXHAUSTION_OVERLAY_NAME, true).getBoolean(true);
		config.getCategory(CATEGORY_CLIENT).remove(SHOW_FOOD_EXHAUSTION_OVERLAY_NAME);

		SHOW_FOOD_VALUES_IN_TOOLTIP = config.get(CATEGORY_CLIENT, SHOW_FOOD_VALUES_IN_TOOLTIP_NAME, true, SHOW_FOOD_VALUES_IN_TOOLTIP_COMMENT).getBoolean(true);
		ALWAYS_SHOW_FOOD_VALUES_TOOLTIP = config.get(CATEGORY_CLIENT, ALWAYS_SHOW_FOOD_VALUES_TOOLTIP_NAME, false, ALWAYS_SHOW_FOOD_VALUES_TOOLTIP_COMMENT).getBoolean(false);
		SHOW_SATURATION_OVERLAY = config.get(CATEGORY_CLIENT, SHOW_SATURATION_OVERLAY_NAME, true, SHOW_SATURATION_OVERLAY_COMMENT).getBoolean(true);
		SHOW_FOOD_VALUES_OVERLAY = config.get(CATEGORY_CLIENT, SHOW_FOOD_VALUES_OVERLAY_NAME, true, SHOW_FOOD_VALUES_OVERLAY_COMMENT).getBoolean(true);
		SHOW_FOOD_EXHAUSTION_UNDERLAY = config.get(CATEGORY_CLIENT, SHOW_FOOD_EXHAUSTION_UNDERLAY_NAME, foodExhaustionOverlayValue, SHOW_FOOD_EXHAUSTION_UNDERLAY_COMMENT).getBoolean(foodExhaustionOverlayValue);
		SHOW_FOOD_DEBUG_INFO = config.get(CATEGORY_CLIENT, SHOW_FOOD_DEBUG_INFO_NAME, true, SHOW_FOOD_DEBUG_INFO_COMMENT).getBoolean(true);

		save();
	}

	public static void save()
	{
		config.save();
	}

	public static void load()
	{
		config.load();
	}
}
