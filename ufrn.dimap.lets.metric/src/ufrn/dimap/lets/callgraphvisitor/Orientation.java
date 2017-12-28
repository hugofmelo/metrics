package ufrn.dimap.lets.callgraphvisitor;

public enum Orientation
{
	CALLERS ("Callers"),
	CALLEES ("callees"),
	;

	private final String text;
	private Orientation (final String text)
	{
		this.text = text;
	}

	public String toString ()
	{
		return this.text;
	}
}
