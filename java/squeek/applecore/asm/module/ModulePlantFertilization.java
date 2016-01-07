package squeek.applecore.asm.module;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingMethodAdapter;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.applecore.asm.TransformerModuleHandler;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.ObfHelper;

import java.util.HashMap;

import static org.objectweb.asm.Opcodes.*;

public class ModulePlantFertilization implements IClassTransformerModule
{
	private static final HashMap<String, FertilizeMethodInfo> customFertilizeMethods = new HashMap<String, FertilizeMethodInfo>();
	static
	{
		customFertilizeMethods.put(ASMConstants.HarvestCraft.BlockPamFruit, FertilizeMethodInfo.BLOCK_PAM_FRUIT);
		customFertilizeMethods.put(ASMConstants.HarvestCraft.BlockPamSapling, FertilizeMethodInfo.BLOCK_PAM_SAPLING);
	}

	public static enum FertilizeMethodInfo
	{
		IGROWABLE_BLOCK("grow", ASMHelper.toMethodDescriptor("V", ASMConstants.World, ASMConstants.Random, "I", "I", "I"), 1, 3, 4, 5, 2),
		BLOCK_PAM_FRUIT("fertilize", ASMHelper.toMethodDescriptor("V", ASMConstants.World, "I", "I", "I"), 1, 2, 3, 4, FertilizeMethodInfo.NULL_PARAM),
		BLOCK_PAM_SAPLING("markOrGrowMarked", ASMHelper.toMethodDescriptor("V", ASMConstants.World, "I", "I", "I", ASMConstants.Random), 1, 2, 3, 4, 5);

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
		if (ASMHelper.doesClassImplement(classReader, ObfHelper.getInternalClassName(ASMConstants.IGrowable)))
		{
			FertilizeMethodInfo methodInfo = FertilizeMethodInfo.IGROWABLE_BLOCK;
			ClassNode classNode = ASMHelper.readClassFromBytes(bytes, ClassReader.EXPAND_FRAMES);
			MethodNode method = ASMHelper.findMethodNodeOfClass(classNode, methodInfo.name, methodInfo.desc);
			if (method != null)
			{
				copyAndRenameMethod(classNode, method, methodInfo);
				wrapFertilizeMethod(method, methodInfo);
				return ASMHelper.writeClassToBytes(classNode);
			}
		}
		else if (customFertilizeMethods.containsKey(transformedName))
		{
			FertilizeMethodInfo methodInfo = customFertilizeMethods.get(transformedName);
			ClassNode classNode = ASMHelper.readClassFromBytes(bytes, ClassReader.EXPAND_FRAMES);
			MethodNode method = ASMHelper.findMethodNodeOfClass(classNode, methodInfo.name, methodInfo.desc);
			if (method != null)
			{
				copyAndRenameMethod(classNode, method, methodInfo);
				wrapFertilizeMethod(method, methodInfo);
				return ASMHelper.writeClassToBytes(classNode);
			}
		}
		return bytes;
	}

	private void copyAndRenameMethod(ClassNode classNode, MethodNode method, FertilizeMethodInfo methodInfo)
	{
		MethodVisitor methodCopyVisitor = classNode.visitMethod(method.access, "AppleCore_fertilize", method.desc, method.signature, method.exceptions.toArray(new String[method.exceptions.size()]));
		method.accept(new RemappingMethodAdapter(method.access, method.desc, methodCopyVisitor, new Remapper() {}));
	}

	private void wrapFertilizeMethod(MethodNode method, FertilizeMethodInfo methodInfo)
	{
		if (ASMHelper.isMethodAbstract(method))
			return;

		InsnList toInjectAtStart = new InsnList();
		// fire event
		toInjectAtStart.add(new VarInsnNode(ALOAD, 0));
		toInjectAtStart.add(methodInfo.getLoadWorldInsns());
		toInjectAtStart.add(methodInfo.getLoadCoordinatesInsns());
		toInjectAtStart.add(methodInfo.getLoadRandomInsns());
		toInjectAtStart.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.Hooks), "fireAppleCoreFertilizeEvent", ASMHelper.toMethodDescriptor("V", ASMConstants.Block, ASMConstants.World, "I", "I", "I", ASMConstants.Random), false));
		// just return, we're done here
		toInjectAtStart.add(new InsnNode(RETURN));
		method.instructions.insertBefore(ASMHelper.findFirstInstruction(method), toInjectAtStart);
	}
}