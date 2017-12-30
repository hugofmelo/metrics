package ufrn.dimap.lets.exceptionalinterface.limitations;

public class IndirectRecursion {

	/* Este exemplo evidencia um problema no cálculo da interface excecional dos métodos.
	 * No meu algoritmo, os filhos são processados primeiro, para então visitar os pais. Com isso eu
	 * conseguia calcular a interface excepcional completa.
	 * No entanto, como mostrado no exemplo abaixo, se houver recursão indireta, essa exceção será
	 * ignorada, fazendo com a interface excepcional deste método e dos seus callers fique incompleta.
	 * No exemplo, todos os métodos podem sinalizar as 3 exceções, mas meu algoritmo não vai identificar isso.
	 * Ele vai ignorar a chamada recursiva de m3 para m1. Este problema não ocorre na recursão direta porque
	 * não havendo métodos intermediários, a chamada recursiva pode ser ignorada sem afetar a interface
	 * excepcional. Para resolver esse problema, pensei em calcular os thrown no preVisit, mas não funciona.
	 * No momento esse problema representa uma limitação da ferramenta, mas vai continuar assim porque a 
	 * solução é não-trivial e a ocorrência é muito pequena. Novamente: só ocorre em recursão indireta.
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
