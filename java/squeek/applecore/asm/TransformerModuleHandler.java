package squeek.applecore.asm;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.launchwrapper.IClassTransformer;
import squeek.applecore.asm.module.*;

public class TransformerModuleHandler implements IClassTransformer
{
	public static final String WILDCARD = "*";
	public static final String[] ALL_CLASSES = new String[] { WILDCARD };

	private static final List<IClassTransformerModule> transformerModules = new ArrayList<IClassTransformerModule>();
	static
	{
		registerTransformerModule(new ModuleFoodStats());
		registerTransformerModule(new ModulePlantGrowth());
		registerTransformerModule(new ModuleFoodEatingSpeed());
		registerTransformerModule(new ModuleBlockFood());
		registerTransformerModule(new ModuleDrawTooltip());
		registerTransformerModule(new ModulePeacefulRegen());
	}

	public static void registerTransformerModule(IClassTransformerModule transformerModule)
	{
		transformerModules.add(transformerModule);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (basicClass == null)
			return null;

		for (IClassTransformerModule transformerModule : transformerModules)
		{
			for (String classToTransform : transformerModule.getClassesToTransform())
			{
				if (classToTransform.equals(WILDCARD) || classToTransform.equals(transformedName))
				{
					basicClass = transformerModule.transform(name, transformedName, basicClass);
				}
			}
		}
		return basicClass;
	}

}
