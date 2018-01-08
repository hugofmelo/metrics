package ufrn.dimap.lets.exceptionalinterface.limitations;

public class InterfaceResolution {

	public InterfaceResolution()
	{
		IA ia = new A();
		ia.m1();
		
		A a = new A();
		a.m1();
	}
}

class A implements IA 
{
	public void m1() 
	{
		System.out.println("asdfsad");
	}
}

interface IA
{
	public void m1();
}