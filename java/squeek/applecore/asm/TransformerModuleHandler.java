package squeek.applecore.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import squeek.applecore.asm.module.*;

import java.util.ArrayList;
import java.util.List;

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
		registerTransformerModule(new ModulePeacefulRegen());
		registerTransformerModule(new ModulePlantFertilization());
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
