package squeek.applecore.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import squeek.applecore.asm.module.*;

import java.util.ArrayList;
import java.util.List;

public class TransformerModuleHandler implements IClassTransformer
{
	public static final String WILDCARD = "*";
	public static final String[] ALL_CLASSES = new String[] { WILDCARD };

	private static final List<IClassTransformerModule> TRANSFORMER_MODULES = new ArrayList<>();
	static
	{
		registerTransformerModule(new ModuleFoodStats());
		registerTransformerModule(new ModuleFoodEatingSpeed());
		registerTransformerModule(new ModuleBlockFood());
		registerTransformerModule(new ModulePeacefulRegen());
		registerTransformerModule(new ModuleHungerHUD());
		registerTransformerModule(new ModuleExhaustingActions());
	}

	public static void registerTransformerModule(IClassTransformerModule transformerModule)
	{
		TRANSFORMER_MODULES.add(transformerModule);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (basicClass == null)
			return null;

		for (IClassTransformerModule transformerModule : TRANSFORMER_MODULES)
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