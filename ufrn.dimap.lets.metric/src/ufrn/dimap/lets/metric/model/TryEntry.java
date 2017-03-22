package ufrn.dimap.lets.metric.model;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TryStatement;

public class TryEntry extends AbstractViewEntry
{
	public TryEntry( TryStatement node )
	{
		super (node);
		
		CompilationUnit compilationUnit = (CompilationUnit) node.getRoot();
		Block tryBlock = node.getBody();
		/*
		this.startPosition = node.getStartPosition();
		this.length = tryBlock.getStartPosition() - this.startPosition + tryBlock.getLength();
		
		this.initLineNumber = compilationUnit.getLineNumber(this.startPosition);
		this.endLineNumber = compilationUnit.getLineNumber(this.startPosition + this.length);
		this.LoCs = endLineNumber - initLineNumber + 1;
		*/
	}

}