package ufrn.dimap.lets.metric.model;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TryStatement;

public class FinallyEntry extends AbstractEntry
{
	public FinallyEntry( Block node )
	{
		super (node);
		
		/*
		CompilationUnit compilationUnit = (CompilationUnit) node.getRoot();
		Block finallyBlock = node.getFinally();
		
		this.offset = finallyBlock.getStartPosition();
		this.length = finallyBlock.getLength();
		
		this.initLineNumber = compilationUnit.getLineNumber(this.offset);
		this.endLineNumber = compilationUnit.getLineNumber(this.length);
		this.LoCs = endLineNumber - initLineNumber + 1;
		*/
	}

}
