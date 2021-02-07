function tryAddFieldGetter(classNode, methodName, fieldName, fieldDescriptor) {
	fieldName = ASMAPI.mapField(fieldName);
	var methodDescriptor = ASMHelper.toMethodDescriptor(fieldDescriptor);
	if (ASMHelper.findMethodNodeOfClass(classNode, methodName, methodDescriptor) !== undefined)
		return false;

	var mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDescriptor, null, null);
	mv.visitVarInsn(Opcodes.ALOAD, 0);
	mv.visitFieldInsn(Opcodes.GETFIELD, ASMHelper.toInternalClassName(classNode.name), fieldName, fieldDescriptor);
	mv.visitInsn(Type.getType(fieldDescriptor).getOpcode(Opcodes.IRETURN));
	mv.visitMaxs(0, 0);
	return true;
}

function tryAddFieldSetter(classNode, methodName, fieldName, fieldDescriptor) {
	fieldName = ASMAPI.mapField(fieldName);
	var methodDescriptor = ASMHelper.toMethodDescriptor("V", fieldDescriptor);
	if (ASMHelper.findMethodNodeOfClass(classNode, methodName, methodDescriptor) !== undefined)
		return false;

	var mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDescriptor, null, null);
	mv.visitVarInsn(Opcodes.ALOAD, 0);
	mv.visitVarInsn(Type.getType(fieldDescriptor).getOpcode(Opcodes.ILOAD), 1);
	mv.visitFieldInsn(Opcodes.PUTFIELD, ASMHelper.toInternalClassName(classNode.name), fieldName, fieldDescriptor);
	mv.visitInsn(Opcodes.RETURN);
	mv.visitMaxs(0, 0);
	return true;
}