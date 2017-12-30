package ufrn.dimap.lets.exceptionalinterface.limitations;

public class IndirectRecursion {

	/* Este exemplo evidencia um problema no c�lculo da interface excecional dos m�todos.
	 * No meu algoritmo, os filhos s�o processados primeiro, para ent�o visitar os pais. Com isso eu
	 * conseguia calcular a interface excepcional completa.
	 * No entanto, como mostrado no exemplo abaixo, se houver recurs�o indireta, essa exce��o ser�
	 * ignorada, fazendo com a interface excepcional deste m�todo e dos seus callers fique incompleta.
	 * No exemplo, todos os m�todos podem sinalizar as 3 exce��es, mas meu algoritmo n�o vai identificar isso.
	 * Ele vai ignorar a chamada recursiva de m3 para m1. Este problema n�o ocorre na recurs�o direta porque
	 * n�o havendo m�todos intermedi�rios, a chamada recursiva pode ser ignorada sem afetar a interface
	 * excepcional. Para resolver esse problema, pensei em calcular os thrown no preVisit, mas n�o funciona.
	 * No momento esse problema representa uma limita��o da ferramenta, mas vai continuar assim porque a 
	 * solu��o � n�o-trivial e a ocorr�ncia � muito pequena. Novamente: s� ocorre em recurs�o indireta.
	 */

	
	private void exampleIndirect ()
	{
		m1(1);
	}
	
	private void m1(int i) {
	
		if ( i != 10 )
			throw new IllegalArgumentException();
		
		m2(i);
	}
	
	private void m2(int i) 
	{
		if ( i != 10 )
			throw new IllegalStateException();
		
		m3(i);
	}
	
	private void m3(int i)
	{
		if ( i != 10 )
			throw new RuntimeException();
		
		m1(i);
	}
	
	
	private void exampleDirect ()
	{
		m4(10);
	}
	
	private void m4(int i)
	{
		if ( i == 0 )
			m4(i-1);
		else
			throw new RuntimeException();
	}
	
}
