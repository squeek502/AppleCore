package squeek.applecore.asm.module;

import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import squeek.applecore.AppleCore;
import squeek.applecore.asm.ASMHelper;
import squeek.applecore.asm.Hooks;
import squeek.applecore.asm.IClassTransformerModule;

public class ModuleDrawTooltip implements IClassTransformerModule
{
	@Override
	public String[] getClassesToTransform()
	{
		return new String[]{
		"net.minecraft.client.gui.GuiScreen",
		"codechicken.lib.gui.GuiDraw",
		"tconstruct.client.gui.NewContainerGui"
		};
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		ClassNode classNode = ASMHelper.readClassFromBytes(bytes);

		if (transformedName.equals("net.minecraft.client.gui.GuiScreen"))
		{
			boolean isObfuscated = !name.equals(transformedName);

			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "drawHoveringText", isObfuscated ? "(Ljava/util/List;IILbbu;)V" : "(Ljava/util/List;IILnet/minecraft/client/gui/FontRenderer;)V");

			if (methodNode != null)
			{
				addDrawHoveringTextHook(methodNode, Hooks.class, "onDrawHoveringText", "(IIII)V", isObfuscated);
			}
			else
				throw new RuntimeException("GuiScreen.drawHoveringText not found");
		}
		else if (name.equals("codechicken.lib.gui.GuiDraw"))
		{
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "drawTooltipBox", "(IIII)V");

			if (methodNode != null)
			{
				addCodeChickenDrawHoveringTextHook(methodNode, Hooks.class, "onDrawHoveringText", "(IIII)V");
			}
			else
				AppleCore.Log.error("drawTooltipBox method in codechicken.lib.gui.GuiDraw not found");
		}
		else if (name.equals("tconstruct.client.gui.NewContainerGui"))
		{
			MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_102021_a", "(Ljava/util/List;II)V");

			if (methodNode != null)
			{
				addTinkersDrawHoveringTextHook(methodNode, Hooks.class, "onDrawHoveringText", "(IIII)V", false);
			}
			else
				AppleCore.Log.error("func_102021_a method in tconstruct.client.gui.NewContainerGui not found");
		}

		return ASMHelper.writeClassToBytes(classNode);
	}

	public void addDrawHoveringTextHook(MethodNode method, Class<?> hookClass, String hookMethod, String hookDesc, boolean isObfuscated)
	{
		AbstractInsnNode targetNode = null;

		// get last drawGradientRect call
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction.getOpcode() == INVOKEVIRTUAL)
			{
				MethodInsnNode methodInsn = (MethodInsnNode) instruction;

				if (methodInsn.desc.equals("(IIIIII)V"))
					targetNode = instruction;
			}
		}
		if (targetNode == null)
			throw new RuntimeException("Unexpected instruction pattern encountered in " + method.name);

		LocalVariableNode x = ASMHelper.findLocalVariableOfMethod(method, "j2", "I");
		LocalVariableNode y = ASMHelper.findLocalVariableOfMethod(method, "k2", "I");
		LocalVariableNode w = ASMHelper.findLocalVariableOfMethod(method, "k", "I");
		LocalVariableNode h = ASMHelper.findLocalVariableOfMethod(method, "i1", "I");

		if (x == null || y == null || w == null || h == null)
		{
			AppleCore.Log.warn("Could not patch " + method.name + "; local variables not found");
			return;
		}

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		Hooks.onDrawHoveringText(0, 0, 0, 0);
		*/

		toInject.add(new VarInsnNode(ILOAD, x.index));
		toInject.add(new VarInsnNode(ILOAD, y.index));
		toInject.add(new VarInsnNode(ILOAD, w.index));
		toInject.add(new VarInsnNode(ILOAD, h.index));
		toInject.add(new MethodInsnNode(INVOKESTATIC, hookClass.getName().replace('.', '/'), hookMethod, hookDesc));

		method.instructions.insert(targetNode, toInject);
	}

	public void addTinkersDrawHoveringTextHook(MethodNode method, Class<?> hookClass, String hookMethod, String hookDesc, boolean isObfuscated)
	{
		AbstractInsnNode targetNode = null;

		// get last drawGradientRect call
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction.getOpcode() == INVOKEVIRTUAL)
			{
				MethodInsnNode methodInsn = (MethodInsnNode) instruction;

				if (methodInsn.desc.equals("(IIIIII)V"))
					targetNode = instruction;
			}
		}
		if (targetNode == null)
		{
			AppleCore.Log.warn("Could not patch " + method.name + "; target node not found");
			return;
		}

		LocalVariableNode x = ASMHelper.findLocalVariableOfMethod(method, "i1", "I");
		LocalVariableNode y = ASMHelper.findLocalVariableOfMethod(method, "j1", "I");
		LocalVariableNode w = ASMHelper.findLocalVariableOfMethod(method, "k", "I");
		LocalVariableNode h = ASMHelper.findLocalVariableOfMethod(method, "k1", "I");

		if (x == null || y == null || w == null || h == null)
		{
			AppleCore.Log.warn("Could not patch " + method.name + "; local variables not found");
			return;
		}

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		Hooks.onDrawHoveringText(0, 0, 0, 0);
		*/

		toInject.add(new VarInsnNode(ILOAD, x.index));
		toInject.add(new VarInsnNode(ILOAD, y.index));
		toInject.add(new VarInsnNode(ILOAD, w.index));
		toInject.add(new VarInsnNode(ILOAD, h.index));
		toInject.add(new MethodInsnNode(INVOKESTATIC, hookClass.getName().replace('.', '/'), hookMethod, hookDesc));

		method.instructions.insert(targetNode, toInject);
	}

	public void addCodeChickenDrawHoveringTextHook(MethodNode method, Class<?> hookClass, String hookMethod, String hookDesc)
	{
		AbstractInsnNode targetNode = ASMHelper.findFirstInstruction(method);

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		Hooks.onDrawHoveringText(0, 0, 0, 0);
		*/

		toInject.add(new VarInsnNode(ILOAD, 0));	// x
		toInject.add(new VarInsnNode(ILOAD, 1));	// y
		toInject.add(new VarInsnNode(ILOAD, 2));	// w
		toInject.add(new VarInsnNode(ILOAD, 3));	// h
		toInject.add(new MethodInsnNode(INVOKESTATIC, hookClass.getName().replace('.', '/'), hookMethod, hookDesc));

		method.instructions.insertBefore(targetNode, toInject);
	}
}
