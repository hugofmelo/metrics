package ufrn.dimap.lets.exceptionalinterface.limitations;

public class InterfaceResolution {

	/* A forma que o eclipse calcula o grafo de chamadas � abstra�da. Considero que se o desenvolvedor
	 * pode confiar na ferramenta Call Hierarchy, ele poder� confiar na minha interface excepcional.
	 * Uma das limita��es do grafo de chamadas do eclipse � sobre a implementa��o de m�todos abstratos.
	 * No exemplo abaixo, o mesmo m�todo m1 � chamado, mas o eclipse s� identifica o segundo,
	 * devido ao tipo da vari�vel da primeira ocorr�ncia ser uma interface, e a segunda ser a classe
	 * que implementa a interface. Ou seja, o eclipse usa um algoritmo bem limitado nesse aspecto.
	 * */
	
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