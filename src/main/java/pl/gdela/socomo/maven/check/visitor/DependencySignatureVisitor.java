package pl.gdela.socomo.maven.check.visitor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;


class DependencySignatureVisitor extends SignatureVisitor {

	private String signatureClassName;
	private VisitorDataCollector visitorDataCollector;

	public DependencySignatureVisitor(VisitorDataCollector visitorDataCollector) {
		super(Opcodes.ASM4);
		this.visitorDataCollector = visitorDataCollector;
	}

	@Override
	public void visitFormalTypeParameter(final String name) {
	}

	@Override
	public SignatureVisitor visitClassBound() {
		return this;
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		return this;
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		return this;
	}

	@Override
	public SignatureVisitor visitInterface() {
		return this;
	}

	@Override
	public SignatureVisitor visitParameterType() {
		return this;
	}

	@Override
	public SignatureVisitor visitReturnType() {
		return this;
	}

	@Override
	public SignatureVisitor visitExceptionType() {
		return this;
	}

	@Override
	public void visitBaseType(final char descriptor) {
	}

	@Override
	public void visitTypeVariable(final String name) {
	}

	@Override
	public SignatureVisitor visitArrayType() {
		return this;
	}

	@Override
	public void visitClassType(final String name) {
		signatureClassName = name;
		visitorDataCollector.addInternalName(name);
	}

	@Override
	public void visitInnerClassType(final String name) {
		signatureClassName = signatureClassName + "$" + name;
		visitorDataCollector.addInternalName(signatureClassName);
	}

	@Override
	public void visitTypeArgument() {
	}

	@Override
	public SignatureVisitor visitTypeArgument(final char wildcard) {
		return this;
	}

	// common

	@Override
	public void visitEnd() {
	}
}
