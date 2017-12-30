package ufrn.dimap.lets.exceptionalinterface.limitations;

import java.io.IOException;

public class AnonymousClasses {

	/* Cada classe anonima possui uma implementação única. Por isso, os métodos chamados
	 * no escopo de uma classe anonima deveriam ser sempre processados. Atualmente estes métodos são
	 * ignorados caso eles já tenham sido visitados.
	 * No exemplo, o algoritmo vai processar a primeira chamada a m1 e ignorar a segunda, porque m1 já
	 * foi visitado. Como são interfaces excepcionais distintas, o erro será propagado.
	 * Note que nesse exemplo as duas chamadas estão no mesmo método, mas na prática elas podem estar em
	 * quaisquer lugares.
	 * */

	class A
	{
		public void m1() {}
	}
	
	private void method () throws Exception
	{
		new A () {
			public void m1()
			{
				throw new IllegalArgumentException();
			}
		}.m1();
		
		new A () {
			public void m1()
			{
				throw new IllegalStateException();
			}
		}.m1();
	}
}


