package ufrn.dimap.lets.exceptionalinterface.limitations;

public class Example extends BaseExample {

	public Example() throws Exception
	{
		super();
		signaler();
		new ThrowingConstructor();
		super.signaler();
		
		Exception e = new RuntimeException();
		throw e;
		//throw new RuntimeException();
		
	}

	protected void signaler()
	{
		throw new RuntimeException();
	}
}

class BaseExample
{
	public BaseExample()
	{
		throw new IllegalArgumentException();
	}
	
	protected void signaler()
	{
		throw new IllegalArgumentException();
	}
}

class ThrowingConstructor
{
	public ThrowingConstructor()
	{
		throw new RuntimeException();
	}
}