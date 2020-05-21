package squeek.applecore.asm.module;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import squeek.applecore.asm.IClassTransformerModule;
import squeek.asmhelper.ObfHelper;
import squeek.asmhelper.applecore.ASMHelper;

import static org.objectweb.asm.Opcodes.*;

public class ModuleItemFood implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[] {ASMConstants.ITEM_FOOD};
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(transformedName.equals(ASMConstants.ITEM_FOOD))
		{
			ClassNode classNode = ASMHelper.readClassFromBytes(bytes);
			
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "<init>", null);
			patchItemFoodInit(methodNode);
		}
		return basicClass;
	}
	
	private void patchItemFoodInit(MethodNode method)
	{
		InsnList insn = new InsnList();
		insn.add(new VarInsnNode);
		insn.add(new FieldInsnNode(PUTFIELD, ObfHelper.getInternalClassName(ASMConstants.ITEM_FOOD), ObfHelper.isObfuscated() ? "field_77852_b" : "alwaysEdible", "Z"));
		method.instructions.insert(insn);
	}
}
