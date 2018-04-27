package ufrn.dimap.lets.exceptionexpert.model;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Representa um sinalizador em um método. Cada sinalizador possui um tipo excepcional,
 * uma localização (em relação ao método) e uma origem (localização do throw inicial). Uma única
 * chamada de método pode estar associada a vários signalers.
 * */
public class Signaler
{
	private Object location; // Linha na qual a exceção é sinalizada
	private EIType type; // Tipo da exceção sinalizada
	private String handleIdentifier; // Arquivo onde ocorre a sinalização
	private int startPosition; // Linha onde inicia a sinalização
	private int lenght; // Tamanho da sinalização
	
	public Signaler (EIType type, String handleIdentifier, int startPosition, int lenght)	
	{
		this.type = type;
		this.handleIdentifier = handleIdentifier;
		this.startPosition = startPosition;
		this.lenght = lenght;
	}
	
	public Object getLocation ()
	{
		return this.location;
	}
	
	public EIType getType ()
	{
		return this.type;
	}
	
	public String getHandleIdentifier()
	{
		return this.handleIdentifier;
	}
	
	public int getStartPosition()
	{
		return this.startPosition;
	}
	
	public int getLenght()
	{
		return this.lenght;
	}
	
	
	@Override
	public boolean equals (Object other)
	{
		if ( !(other instanceof Signaler) )
			return false;
		else
		{
			Signaler s = (Signaler)other;
			
			return 	this.type.equals(s.type) &&
					this.location.equals(s.location) &&
					this.handleIdentifier.equals(s.handleIdentifier) &&
					this.startPosition == s.startPosition &&
					this.lenght == s.lenght;
		}
	}

	public String toString ()
	{
		return "[" + this.type + ", " + this.handleIdentifier + ", " + this.startPosition + ", " + this.lenght + "]";
	}


	
	
}
