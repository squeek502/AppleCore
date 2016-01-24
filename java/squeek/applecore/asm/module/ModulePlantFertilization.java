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
import java.util.Random;
import static org.objectweb.asm.Opcodes.*;

public class ModulePlantFertilization implements IClassTransformerModule
{
	public static enum FertilizeMethodInfo
	{
		IGROWABLE_BLOCK(ObfHelper.isObfuscated() ? "func_176474_b" : "grow", ASMHelper.toMethodDescriptor("V", ASMConstants.World, ASMConstants.Random, ASMConstants.BlockPos, ASMConstants.IBlockState), 1, 3, 4, 2);

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
		return bytes;
	}

	private void copyAndRenameMethod(ClassNode classNode, MethodNode method, FertilizeMethodInfo methodInfo)
	{
		MethodVisitor methodCopyVisitor = classNode.visitMethod(method.access, "AppleCore_fertilize", method.desc, method.signature, method.exceptions.toArray(new String[method.exceptions.size()]));
		method.accept(new RemappingMethodAdapter(method.access, method.desc, methodCopyVisitor, new Remapper(){}));
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
		toInjectAtStart.add(methodInfo.getLoadBlockStateInsns());
		toInjectAtStart.add(methodInfo.getLoadRandomInsns());
		toInjectAtStart.add(new MethodInsnNode(INVOKESTATIC, ASMHelper.toInternalClassName(ASMConstants.Hooks), "fireAppleCoreFertilizeEvent", ASMHelper.toMethodDescriptor("V", ASMConstants.Block, ASMConstants.World, ASMConstants.BlockPos, ASMConstants.IBlockState, ASMConstants.Random), false));
		// just return, we're done here
		toInjectAtStart.add(new InsnNode(RETURN));
		method.instructions.insertBefore(ASMHelper.findFirstInstruction(method), toInjectAtStart);
	}
}