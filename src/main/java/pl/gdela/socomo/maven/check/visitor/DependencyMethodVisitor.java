package pl.gdela.socomo.maven.check.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


class DependencyMethodVisitor extends MethodVisitor {

	private VisitorDataCollector visitorDataCollector;

	public DependencyMethodVisitor(VisitorDataCollector visitorDataCollector) {
		super(Opcodes.ASM4);
		this.visitorDataCollector = visitorDataCollector;
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(
			final int parameter,
			final String desc,
			final boolean visible)
	{
		visitorDataCollector.addDesc(desc);
		return new DependencyAnnotationVisitor(visitorDataCollector);
	}

	@Override
	public void visitAttribute(Attribute attribute) {

	}

	@Override
	public void visitTypeInsn(final int opcode, final String type) {
		visitorDataCollector.addType(Type.getObjectType(type));
	}

	@Override
	public void visitFieldInsn(
			final int opcode,
			final String owner,
			final String name,
			final String desc)
	{
		visitorDataCollector.addInternalName(owner);
		visitorDataCollector.addDesc(desc);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		visitorDataCollector.addInternalName(owner);
		visitorDataCollector.addMethodDesc(desc);
	}

	@Override
	public void visitLdcInsn(final Object cst) {
		if (cst instanceof Type) {
			visitorDataCollector.addType((Type) cst);
		}
	}

	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		visitorDataCollector.addDesc(desc);
	}

	@Override
	public void visitLocalVariable(
			final String name,
			final String desc,
			final String signature,
			final Label start,
			final Label end,
			final int index)
	{
		visitorDataCollector.addTypeSignature(signature);
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return new DependencyAnnotationVisitor(visitorDataCollector);
	}

	@Override
	public AnnotationVisitor visitAnnotation(
			final String desc,
			final boolean visible)
	{
		visitorDataCollector.addDesc(desc);
		return new DependencyAnnotationVisitor(visitorDataCollector);
	}



	@Override
	public void visitCode() {
	}

	@Override
	public void visitFrame(
			final int type,
			final int nLocal,
			final Object[] local,
			final int nStack,
			final Object[] stack)
	{
	}

	@Override
	public void visitInsn(final int opcode) {
	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
	}

	@Override
	public void visitLabel(final Label label) {
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
	}

	@Override
	public void visitTableSwitchInsn(
			final int min,
			final int max,
			final Label dflt,
			final Label[] labels)
	{
	}

	@Override
	public void visitLookupSwitchInsn(
			final Label dflt,
			final int[] keys,
			final Label[] labels)
	{
	}

	@Override
	public void visitTryCatchBlock(
			final Label start,
			final Label end,
			final Label handler,
			final String type)
	{
		if (type != null) {
			visitorDataCollector.addInternalName(type);
		}
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
	}

	@Override
	public void visitEnd() {

	}
}
