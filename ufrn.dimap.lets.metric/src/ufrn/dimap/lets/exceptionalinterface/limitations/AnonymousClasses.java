package ufrn.dimap.lets.exceptionalinterface.limitations;

import java.io.IOException;

public class AnonymousClasses {

	/* Cada classe anonima possui uma implementa��o �nica. Por isso, os m�todos chamados
	 * no escopo de uma classe anonima deveriam ser sempre processados. Atualmente estes m�todos s�o
	 * ignorados caso eles j� tenham sido visitados.
	 * No exemplo, o algoritmo vai processar a primeira chamada a m1 e ignorar a segunda, porque m1 j�
	 * foi visitado. Como s�o interfaces excepcionais distintas, o erro ser� propagado.
	 * Note que nesse exemplo as duas chamadas est�o no mesmo m�todo, mas na pr�tica elas podem estar em
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


