package squeek.applecore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.objectweb.asm.Opcodes.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import squeek.applecore.asm.helpers.ASMHelper;
import squeek.applecore.asm.helpers.InsnComparator;

public class TestASMHelper
{
	public InsnList haystack;

	@Before
	public void setUp() throws Exception
	{
		haystack = new InsnList();
		populateHaystack(haystack);
	}

	@After
	public void tearDown()
	{
		haystack = null;
	}

	@Test
	public void testInsnMatching()
	{
		assertTrue(ASMHelper.instructionsMatch(new InsnNode(RETURN), new InsnNode(RETURN)));

		assertTrue(ASMHelper.instructionsMatch(new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, 0)));
		assertTrue(ASMHelper.instructionsMatch(new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, InsnComparator.INT_WILDCARD)));
		assertFalse(ASMHelper.instructionsMatch(new VarInsnNode(ILOAD, 0), new VarInsnNode(ALOAD, 0)));
		assertFalse(ASMHelper.instructionsMatch(new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, 1)));
	}

	@Test
	public void testPatternMatching()
	{
		InsnList haystack = new InsnList();
		haystack.add(new LabelNode());
		haystack.add(new VarInsnNode(ALOAD, 0));
		haystack.add(new LineNumberNode(10, new LabelNode()));

		InsnList needle = new InsnList();
		needle.add(new LineNumberNode(1, new LabelNode()));
		needle.add(new VarInsnNode(ALOAD, 0));
		needle.add(new LineNumberNode(2, new LabelNode()));

		// line number/label inequality
		assertTrue(ASMHelper.patternMatches(needle, haystack.getFirst()));

		// needle longer than haystack
		needle.add(new VarInsnNode(ALOAD, 1));
		assertFalse(ASMHelper.patternMatches(needle, haystack.getFirst()));

		// haystack longer than needle
		haystack.add(new VarInsnNode(ALOAD, 1));
		haystack.add(new VarInsnNode(ALOAD, 1));
		assertTrue(ASMHelper.patternMatches(needle, haystack.getFirst()));
	}

	@Test
	public void testFind()
	{
		InsnList needle = new InsnList();
		assertNull(ASMHelper.find(haystack, needle));

		needle.add(new VarInsnNode(ALOAD, 0));
		assertEquals(haystack.get(2), ASMHelper.find(haystack, needle));

		needle.add(new FieldInsnNode(GETFIELD, InsnComparator.WILDCARD, "foodLevel", "I"));
		assertEquals(haystack.get(2), ASMHelper.find(haystack, needle));

		needle.add(new VarInsnNode(ISTORE, 3));
		assertEquals(haystack.get(2), ASMHelper.find(haystack, needle));

		needle.add(new VarInsnNode(ALOAD, 0));
		assertEquals(haystack.get(2), ASMHelper.find(haystack, needle));

		assertNull(ASMHelper.find(haystack.get(3), needle));
	}

	@Test
	public void testFindAndReplace()
	{
		InsnList needle = new InsnList();
		needle.add(new VarInsnNode(ALOAD, 0));
		needle.add(new FieldInsnNode(GETFIELD, InsnComparator.WILDCARD, "foodLevel", "I"));
		needle.add(new VarInsnNode(ISTORE, 3));
		needle.add(new VarInsnNode(ALOAD, 0));

		InsnList replacement = new InsnList();

		AbstractInsnNode afterReplacement = ASMHelper.findAndReplace(haystack, needle, replacement);
		assertEquals(haystack.get(2), afterReplacement);

		populateHaystack(haystack);
		replacement.add(new VarInsnNode(ALOAD, 0));

		afterReplacement = ASMHelper.findAndReplace(haystack, needle, replacement);
		assertEquals(haystack.get(3), afterReplacement);

		afterReplacement = ASMHelper.findAndReplace(haystack, needle, replacement);
		assertEquals(null, afterReplacement);
	}

	@Test
	public void testFindAndReplaceAll()
	{
		InsnList needle = new InsnList();
		needle.add(new VarInsnNode(ALOAD, InsnComparator.INT_WILDCARD));
		needle.add(new FieldInsnNode(GETFIELD, InsnComparator.WILDCARD, InsnComparator.WILDCARD, InsnComparator.WILDCARD));

		InsnList replacement = new InsnList();

		int numReplaced = ASMHelper.findAndReplaceAll(haystack, needle, replacement);
		assertEquals(6, numReplaced);

		numReplaced = ASMHelper.findAndReplaceAll(haystack, needle, replacement);
		assertEquals(0, numReplaced);
	}

	public void populateHaystack(InsnList haystack)
	{
		haystack.clear();
		LabelNode l0 = new LabelNode();
		haystack.add(l0);
		haystack.add(new LineNumberNode(44, l0));
		haystack.add(new VarInsnNode(ALOAD, 0));
		haystack.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/FoodStats", "foodLevel", "I"));
		haystack.add(new VarInsnNode(ISTORE, 3));
		LabelNode l1 = new LabelNode();
		haystack.add(l1);
		haystack.add(new LineNumberNode(45, l1));
		haystack.add(new VarInsnNode(ALOAD, 0));
		haystack.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/FoodStats", "entityplayer", "Lnet/minecraft/entity/player/EntityPlayer;"));
		haystack.add(new VarInsnNode(ALOAD, 1));
		haystack.add(new VarInsnNode(ALOAD, 2));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/item/ItemFood", "func_150905_g", "(Lnet/minecraft/item/ItemStack;)I"));
		haystack.add(new VarInsnNode(ILOAD, 3));
		haystack.add(new InsnNode(IADD));
		haystack.add(new MethodInsnNode(INVOKESTATIC, "org/bukkit/craftbukkit/event/CraftEventFactory", "callFoodLevelChangeEvent", "(Lnet/minecraft/entity/player/EntityPlayer;I)Lorg/bukkit/event/entity/FoodLevelChangeEvent;"));
		haystack.add(new VarInsnNode(ASTORE, 4));
		LabelNode l2 = new LabelNode();
		haystack.add(l2);
		haystack.add(new LineNumberNode(47, l2));
		haystack.add(new VarInsnNode(ALOAD, 4));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/event/entity/FoodLevelChangeEvent", "isCancelled", "()Z"));
		LabelNode l3 = new LabelNode();
		haystack.add(new JumpInsnNode(IFNE, l3));
		LabelNode l4 = new LabelNode();
		haystack.add(l4);
		haystack.add(new LineNumberNode(49, l4));
		haystack.add(new VarInsnNode(ALOAD, 0));
		haystack.add(new VarInsnNode(ALOAD, 4));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/event/entity/FoodLevelChangeEvent", "getFoodLevel", "()I"));
		haystack.add(new VarInsnNode(ILOAD, 3));
		haystack.add(new InsnNode(ISUB));
		haystack.add(new VarInsnNode(ALOAD, 1));
		haystack.add(new VarInsnNode(ALOAD, 2));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/item/ItemFood", "func_150906_h", "(Lnet/minecraft/item/ItemStack;)F"));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/FoodStats", "addStats", "(IF)V"));
		haystack.add(l3);
		haystack.add(new LineNumberNode(52, l3));
		haystack.add(new FrameNode(Opcodes.F_APPEND, 2, new Object[]{Opcodes.INTEGER, "org/bukkit/event/entity/FoodLevelChangeEvent"}, 0, null));
		haystack.add(new VarInsnNode(ALOAD, 0));
		haystack.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/FoodStats", "entityplayer", "Lnet/minecraft/entity/player/EntityPlayer;"));
		haystack.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/player/EntityPlayerMP"));
		haystack.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/player/EntityPlayerMP", "playerNetServerHandler", "Lnet/minecraft/network/NetHandlerPlayServer;"));
		haystack.add(new TypeInsnNode(NEW, "net/minecraft/network/play/server/S06PacketUpdateHealth"));
		haystack.add(new InsnNode(DUP));
		haystack.add(new VarInsnNode(ALOAD, 0));
		haystack.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/FoodStats", "entityplayer", "Lnet/minecraft/entity/player/EntityPlayer;"));
		haystack.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/player/EntityPlayerMP"));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayerMP", "getBukkitEntity", "()Lorg/bukkit/craftbukkit/entity/CraftPlayer;"));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/craftbukkit/entity/CraftPlayer", "getScaledHealth", "()F"));
		haystack.add(new VarInsnNode(ALOAD, 0));
		haystack.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/FoodStats", "entityplayer", "Lnet/minecraft/entity/player/EntityPlayer;"));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", "getFoodStats", "()Lnet/minecraft/util/FoodStats;"));
		haystack.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/FoodStats", "foodLevel", "I"));
		haystack.add(new VarInsnNode(ALOAD, 0));
		haystack.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/FoodStats", "entityplayer", "Lnet/minecraft/entity/player/EntityPlayer;"));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/player/EntityPlayer", "getFoodStats", "()Lnet/minecraft/util/FoodStats;"));
		haystack.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/FoodStats", "foodSaturationLevel", "F"));
		haystack.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/network/play/server/S06PacketUpdateHealth", "<init>", "(FIF)V"));
		haystack.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/network/NetHandlerPlayServer", "sendPacket", "(Lnet/minecraft/network/Packet;)V"));
		LabelNode l5 = new LabelNode();
		haystack.add(l5);
		haystack.add(new LineNumberNode(54, l5));
		haystack.add(new InsnNode(RETURN));
		LabelNode l6 = new LabelNode();
		haystack.add(l6);
	}

}
