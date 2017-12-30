package ufrn.dimap.lets.exceptionalinterface.limitations;

public class Recursion {

	public Recursion()
	{
		m1();
	}
	
	private void m1()
	{
		m2();
	}
	
	private void m2()
	{
		m3();
	}
	
	private void m3()
	{
		m4();
	}
	
	private void m4()
	{
		m1();
	}

}
