package ufrn.dimap.lets.metric.visitor.exceptionalinterface;

import org.eclipse.jdt.core.JavaModelException;

public class ShouldNotHappenException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ShouldNotHappenException (String message)
	{
		super (message);
	}

	
	public ShouldNotHappenException (String message, JavaModelException e)
	{
		super (message, e);
	}


	public ShouldNotHappenException(Exception e) {
		super(e);
	}
}
