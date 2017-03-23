package ufrn.dimap.lets.metric.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;

public abstract class AbstractEntry
{
	private 	ASTNode				node;
	private 	IBinding			binding;
	

	public AbstractEntry ( ASTNode node )
	{
		this.node = node;
		this.binding = null;
	}
	
	public AbstractEntry ( IBinding binding )
	{
		this.node = null;
		this.binding = binding;
	}
	
	public AbstractEntry ( ASTNode node, IBinding binding )
	{
		this.node = node;
		this.binding = binding;
	}
	
	public void setNode (ASTNode node)
	{
		this.node = node;
	}
	
	public ICompilationUnit getICompilationUnit ()
	{
		if (node != null)
		{
			CompilationUnit cu = (CompilationUnit) node.getRoot();
			return (ICompilationUnit) cu.getJavaElement();
		}
		else if ( binding != null )
		{
			IJavaElement javaElement = binding.getJavaElement();
			
			if (javaElement instanceof ICompilationUnit)
			{
				return (ICompilationUnit) javaElement;
			}
			else
			{
				throw new UnsupportedOperationException();
			}
		}
		else // não deve acontecer
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public int getInitLineNumber ()
	{
		if ( node != null )
		{
			CompilationUnit compilationUnit = (CompilationUnit) node.getRoot();
			return compilationUnit.getLineNumber(node.getStartPosition());
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private int getEndLineNumber ()
	{
		if ( node != null )
		{
			CompilationUnit compilationUnit = (CompilationUnit) node.getRoot();
			return compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public int getStartPosition ()
	{
		if ( node != null )
		{
			return node.getStartPosition();
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public int getLength()
	{
		if ( node != null )
		{
			return node.getLength();
		}
		else
		{
			throw new UnsupportedOperationException();
		}
		
	}
	
	public int getLoCs ()
	{
		return this.getEndLineNumber() - this.getInitLineNumber() + 1;
	}
	
	public IBinding getBinding()
	{
		if ( this.binding != null )
		{
			return this.binding;
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public IJavaElement getJavaElement()
	{
		if (node != null)
		{
			CompilationUnit cu = (CompilationUnit) node.getRoot();
			return cu.getJavaElement();
		}
		else if ( binding != null )
		{
			return binding.getJavaElement();
		}
		else // não deve acontecer
		{
			throw new UnsupportedOperationException();
		}
	}

	public boolean hasNode()
	{
		return this.node != null;
	}
	
	public String toString ()
	{
		String result = "";
		
		if ( this.hasNode() )
		{
			result += "Code snippet: " + this.node + "\n";
		}
		
		return result;
	}
}
