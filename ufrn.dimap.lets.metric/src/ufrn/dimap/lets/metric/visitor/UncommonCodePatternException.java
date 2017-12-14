package ufrn.dimap.lets.metric.visitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public abstract class UncommonCodePatternException extends RuntimeException
{
	private ASTNode astNode;
	
	public UncommonCodePatternException (String msg, ASTNode node)
	{
		super (msg);
		this.astNode = node;
	}
	
	public String getCodeSnippet ()
	{
		return this.astNode.toString();
	}
	
	public String getFileName ()
	{
		String handleIdentifier;
		String project, sourceDir, pkg, clazz;
		String result = "";
		
		handleIdentifier = ((CompilationUnit)this.astNode.getRoot()).getJavaElement().getHandleIdentifier();
		
		project = handleIdentifier.substring(handleIdentifier.indexOf('=') + 1, handleIdentifier.indexOf('/'));
		sourceDir = handleIdentifier.substring(handleIdentifier.indexOf('/') + 1, handleIdentifier.indexOf('<'));
		pkg = handleIdentifier.substring(handleIdentifier.indexOf('<') + 1, handleIdentifier.indexOf('{'));
		clazz = handleIdentifier.substring(handleIdentifier.indexOf('{') + 1);
		
		result += project+"\t"+sourceDir+"\t"+pkg+"\t"+clazz;
		
		return result;
	}
	
	public int getLineNumber ()
	{
		return ((CompilationUnit)this.astNode.getRoot()).getLineNumber(this.astNode.getStartPosition());
	}
	
}
