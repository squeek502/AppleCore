package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.*;
import java.util.HashMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.Hooks;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.applecore.asm.TransformerModuleHandler;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.ObfHelper;
import cpw.mods.fml.common.eventhandler.Event;

public class ModulePlantFertilization implements IClassTransformerModule
{
	private static final boolean isObfuscated = ObfHelper.isObfuscated();
	private static final HashMap<String, FertilizeMethodInfo> customFertilizeMethods = new HashMap<String, FertilizeMethodInfo>();
	static
	{
		customFertilizeMethods.put("com.pam.harvestcraft.BlockPamFruit", FertilizeMethodInfo.BLOCK_PAM_FRUIT);
		customFertilizeMethods.put("com.pam.harvestcraft.BlockPamSapling", FertilizeMethodInfo.BLOCK_PAM_SAPLING);
	}

	public static enum FertilizeMethodInfo
	{
		IGROWABLE_BLOCK(isObfuscated ? "b" : "func_149853_b", "(Lnet/minecraft/world/World;Ljava/util/Random;III)V", 1, 3, 4, 5, 2),
		BLOCK_PAM_FRUIT("fertilize", "(Lnet/minecraft/world/World;III)V", 1, 2, 3, 4, FertilizeMethodInfo.NULL_PARAM),
		BLOCK_PAM_SAPLING("markOrGrowMarked", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V", 1, 2, 3, 4, 5);

		public static final int NULL_PARAM = -1;
		public final String name;
		public final String desc;
		public final int worldIndex;
		public final int xIndex;
		public final int yIndex;
		public final int zIndex;
		public final int randomIndex;

		FertilizeMethodInfo(String methodName, String methodDesc, int worldIndex, int xIndex, int yIndex, int zIndex, int randomIndex)
		{
			this.name = methodName;
			this.desc = methodDesc;
			this.worldIndex = worldIndex;
			this.xIndex = xIndex;
			this.yIndex = yIndex;
			this.zIndex = zIndex;
			this.randomIndex = randomIndex;
		}

		public InsnList getLoadCoordinatesInsns()
		{
			InsnList insnList = new InsnList();
			insnList.add(new VarInsnNode(ILOAD, xIndex));
			insnList.add(new VarInsnNode(ILOAD, yIndex));
			insnList.add(new VarInsnNode(ILOAD, zIndex));
			return insnList;
		}

		public InsnList getLoadWorldInsns()
		{
			InsnList insnList = new InsnList();
			insnList.add(new VarInsnNode(ALOAD, worldIndex));
			return insnList;
		}

		public InsnList getLoadRandomInsns()
		{
			InsnList insnList = new InsnList();
			insnList.add(randomIndex != NULL_PARAM ? new VarInsnNode(ALOAD, randomIndex) : new InsnNode(ACONST_NULL));
			return insnList;
		}

		@Override
		public String toString()
		{
			return name + desc;
		}
	}

	@Override
	public String[] getClassesToTransform()
	{
		return TransformerModuleHandler.ALL_CLASSES;
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		ClassReader classReader = new ClassReader(bytes);
		if (ASMHelper.doesClassExtend(classReader, ObfHelper.getInternalClassName("net.minecraft.block.Block")) && ASMHelper.doesClassImplement(classReader, ObfHelper.getInternalClassName("net.minecraft.block.IGrowable")))
		{
			FertilizeMethodInfo methodInfo = FertilizeMethodInfo.IGROWABLE_BLOCK;
			ClassNode classNode = ASMHelper.readClassFromBytes(bytes);
			// obfuscate the method descriptor here because FMLDeobfuscatingRemapper doesn't want to unmap
			// when the FertilizeMethodInfo enum is initialized
			MethodNode method = ASMHelper.findMethodNodeOfClass(classNode, methodInfo.name, ObfHelper.desc(methodInfo.desc));
			if (method != null)
			{
				wrapFertilizeMethod(method, methodInfo);
				return ASMHelper.writeClassToBytes(classNode);
			}
		}
		else if (customFertilizeMethods.containsKey(transformedName))
		{
			FertilizeMethodInfo methodInfo = customFertilizeMethods.get(transformedName);
			ClassNode classNode = ASMHelper.readClassFromBytes(bytes);
			MethodNode method = ASMHelper.findMethodNodeOfClass(classNode, methodInfo.name, methodInfo.desc);
			if (method != null)
			{
				wrapFertilizeMethod(method, methodInfo);
				return ASMHelper.writeClassToBytes(classNode);
			}
		}
		return bytes;
	}

	// TODO: Deal with super method calls
	private void wrapFertilizeMethod(MethodNode method, FertilizeMethodInfo methodInfo)
	{
		LabelNode endLabel = ASMHelper.findEndLabel(method);

		LabelNode previousMetadataStart = new LabelNode();
		LocalVariableNode previousMetadata = new LocalVariableNode("previousMetadata", "I", null, previousMetadataStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(previousMetadata);

		LabelNode fertilizeResultStart = new LabelNode();
		LocalVariableNode fertilizeResult = new LocalVariableNode("fertilizeResult", Type.getDescriptor(Event.Result.class), null, fertilizeResultStart, endLabel, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(fertilizeResult);

		InsnList toInjectAtStart = new InsnList();
		// get previous meta
		toInjectAtStart.add(methodInfo.getLoadWorldInsns());
		toInjectAtStart.add(methodInfo.getLoadCoordinatesInsns());
		toInjectAtStart.add(new MethodInsnNode(INVOKEVIRTUAL, ObfHelper.getInternalClassName("net.minecraft.world.World"), isObfuscated ? "e" : "getBlockMetadata", "(III)I", false));
		toInjectAtStart.add(new VarInsnNode(ISTORE, previousMetadata.index));
		toInjectAtStart.add(previousMetadataStart);
		// fire event and get result
		toInjectAtStart.add(new VarInsnNode(ALOAD, 0));
		toInjectAtStart.add(methodInfo.getLoadWorldInsns());
		toInjectAtStart.add(methodInfo.getLoadCoordinatesInsns());
		toInjectAtStart.add(methodInfo.getLoadRandomInsns());
		toInjectAtStart.add(new VarInsnNode(ILOAD, previousMetadata.index));
		toInjectAtStart.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireFertilizeEvent", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIILjava/util/Random;I)Lcpw/mods/fml/common/eventhandler/Event$Result;", false));
		toInjectAtStart.add(new VarInsnNode(ASTORE, fertilizeResult.index));
		toInjectAtStart.add(fertilizeResultStart);
		// check if its denied
		toInjectAtStart.add(new VarInsnNode(ALOAD, fertilizeResult.index));
		toInjectAtStart.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DENY", Type.getDescriptor(Event.Result.class)));
		LabelNode ifNotDenied = new LabelNode();
		toInjectAtStart.add(new JumpInsnNode(IF_ACMPNE, ifNotDenied));
		toInjectAtStart.add(new LabelNode());
		toInjectAtStart.add(new InsnNode(RETURN));
		toInjectAtStart.add(ifNotDenied);
		// check if its default
		toInjectAtStart.add(new VarInsnNode(ALOAD, fertilizeResult.index));
		toInjectAtStart.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Event.Result.class), "DEFAULT", Type.getDescriptor(Event.Result.class)));
		LabelNode ifNotDefault = new LabelNode();
		toInjectAtStart.add(new JumpInsnNode(IF_ACMPNE, ifNotDefault));
		toInjectAtStart.add(new LabelNode());
		method.instructions.insertBefore(ASMHelper.findFirstInstruction(method), toInjectAtStart);

		InsnList toInjectAtEnd = new InsnList();
		toInjectAtEnd.add(ifNotDefault);
		toInjectAtEnd.add(new VarInsnNode(ALOAD, 0));
		toInjectAtEnd.add(methodInfo.getLoadWorldInsns());
		toInjectAtEnd.add(methodInfo.getLoadCoordinatesInsns());
		toInjectAtEnd.add(new VarInsnNode(ILOAD, previousMetadata.index));
		toInjectAtEnd.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "fireFertilizedEvent", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIII)V", false));
		method.instructions.insertBefore(ASMHelper.findPreviousInstruction(endLabel), toInjectAtEnd);
	}
}
