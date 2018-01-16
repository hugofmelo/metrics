package ufrn.dimap.lets.exceptionalinterface.limitations;

public class InterfaceResolution {

	/* A forma que o eclipse calcula o grafo de chamadas é abstraída. Considero que se o desenvolvedor
	 * pode confiar na ferramenta Call Hierarchy, ele poderá confiar na minha interface excepcional.
	 * Uma das limitações do grafo de chamadas do eclipse é sobre a implementação de métodos abstratos.
	 * No exemplo abaixo, o mesmo método m1 é chamado, mas o eclipse só identifica o segundo,
	 * devido ao tipo da variável da primeira ocorrência ser uma interface, e a segunda ser a classe
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