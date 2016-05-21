package squeek.applecore.asm.module;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.ASMConstants;
import squeek.applecore.asm.IClassTransformerModule;
import squeek.applecore.asm.TransformerModuleHandler;
import squeek.asmhelper.applecore.ASMHelper;
import squeek.asmhelper.applecore.ObfHelper;

import static org.objectweb.asm.Opcodes.*;

public class ModulePlantFertilization implements IClassTransformerModule
{
	public static enum FertilizeMethodInfo
	{
		IGROWABLE_BLOCK(ObfHelper.isObfuscated() ? "func_176474_b" : "grow", ASMHelper.toMethodDescriptor("V", ASMConstants.WORLD, ASMConstants.RANDOM, ASMConstants.BLOCK_POS, ASMConstants.IBLOCKSTATE), 1, 3, 4, 2);

		public static final int NULL_PARAM = -1;
		public final String name;
		public final String desc;
		public final int worldIndex;
		public final int blockPosIndex;
		public final int blockStateIndex;
		public final int randomIndex;

		FertilizeMethodInfo(String methodName, String methodDesc, int worldIndex, int blockPosIndex, int blockStateIndex, int randomIndex)
		{
			this.name = methodName;
			this.desc = methodDesc;
			this.worldIndex = worldIndex;
			this.blockPosIndex = blockPosIndex;
			this.blockStateIndex = blockStateIndex;
			this.randomIndex = randomIndex;
		}

		public InsnList getLoadCoordinatesInsns()
		{
			InsnList insnList = new InsnList();
			insnList.add(new VarInsnNode(ALOAD, blockPosIndex));
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

		public InsnList getLoadBlockStateInsns()
		{
			InsnList insnList = new InsnList();
			insnList.add(new VarInsnNode(ALOAD, blockStateIndex));
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
		if (ASMHelper.doesClassImplement(classReader, ObfHelper.getInternalClassName(ASMConstants.IGROWABLE)))
		{
			FertilizeMethodInfo methodInfo = FertilizeMethodInfo.IGROWABLE_BLOCK;
			ClassNode classNode = ASMHelper.readClassFromBytes(bytes, ClassReader.EXPAND_FRAMES);
			MethodNode method = ASMHelper.findMethodNodeOfClass(classNode, methodInfo.name, methodInfo.desc);
			if (method != null)
			{
				ASMHelper.copyAndRenameMethod(classNode, method, "AppleCore_fertilize");
				classNode.interfaces.add(ASMHelper.toInternalClassName(ASMConstants.IAPPLECOREFERTILIZABLE));
				replaceFertilizeMethod(method, methodInfo);
				return ASMHelper.writeClassToBytes(classNode);
			}
		}
		return bytes;
	}

	private void replaceFertilizeMethod(MethodNode method, FertilizeMethodInfo methodInfo)
	{
		if (ASMHelper.isMethodAbstract(method))
			return;

		if (method.localVariables != null)
			method.localVariables.clear();
		if (method.tryCatchBlocks != null)
			method.tryCatchBlocks.clear();

		method.instructions.clear();
		method.instructions.add(new VarInsnNode(ALOAD, 0));
		method.instructions.add(methodInfo.getLoadWorldInsns());
		method.instructions.add(methodInfo.getLoadCoordinatesInsns());
		method.instructions.add(methodInfo.getLoadBlockStateInsns());
		method.instructions.add(methodInfo.getLoadRandomInsns());
		method.instructions.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.HOOKS), "fireAppleCoreFertilizeEvent", ASMHelper.toMethodDescriptor("V", ASMConstants.BLOCK, ASMConstants.WORLD, ASMConstants.BLOCK_POS, ASMConstants.IBLOCKSTATE, ASMConstants.RANDOM), false));
		method.instructions.add(new InsnNode(RETURN));
	}
}