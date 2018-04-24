package ufrn.dimap.lets.exceptionalinterface;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Representa a interface excepcional de um método ou de um bloco try. Um ExcepcionalInterface é
 * formado por uma lista de Signalers únicos. Os Signalers estão em ordem crescente.
 * */
public class ExceptionalInterface implements Comparator <Signaler>
{
	private TreeSet<Signaler> signalers;
	
	//private Map<EIType, Set<EIType>> caught;
	//private Set<EIType> propagated;
	//private Set<EIType> thrown;
	//private Set<EIType> rethrown;
	//private Map<EIType, Set<EIType>> wrapped;
	
	public ExceptionalInterface ()
	{
		this.signalers = new TreeSet<Signaler>(this);
		
//		this.caught = new HashMap<>();
//		this.propagated = new HashSet<>();
//		this.thrown = new HashSet<>();
//		this.rethrown = new HashSet<>();
//		this.wrapped = new HashMap<>();
	}
	
	public Set<EIType> getExternalExceptionTypes()
	{
		Set<EIType> exceptionTypes = new HashSet<>();
		
		for ( Signaler signaler : this.signalers )
		{
			exceptionTypes.add(signaler.getType());
		}
		
//		exceptions.addAll(this.thrown);
//		exceptions.addAll(this.rethrown);
//		exceptions.addAll(this.propagated);
//		exceptions.addAll(this.wrapped.keySet());
		
		return exceptionTypes;
	}

	/** 
	 * Adiciona um Signaler a esta interface excepcional na posição correta (ordem crescente).
	 * Ignora duplicatas.
	 * */
	public void addSignaler (Signaler signaler)
	{
		this.signalers.add(signaler);
		
		/*
		int index = 0;
		
		// Procura a posição correta para a inserção. Como no InsertionSort
		while ( index < this.signalers.size() && signaler.compareTo( this.signalers.get(index)) > 0 )
		{
			index++;
		}
		
		// Evita duplicatas
		if ( signaler.compareTo( this.signalers.get(index)) != 0 )
		{
			this.signalers.add(index, signaler);
		}
		*/
	}

	/** 
	 * Adiciona uma lista de signalers a esta interface excepcional. Ignora duplicatas.
	 * */
	public void addSignalers(TreeSet<Signaler> signalers)
	{
		for ( Signaler signaler : signalers )
		{
			this.addSignaler(signaler);
		}
	}
	
	public TreeSet<Signaler> getSignalers()
	{
		return this.signalers;
	}

	
	
//	public void addCaught(EIType realCaughtType, EIType catchType)
//	{
//		Set<EIType> caughtAs = this.caught.get(realCaughtType);
//		if ( caughtAs == null )
//		{
//			caughtAs = new HashSet<>();
//		}
//		
//		caughtAs.add(catchType);
//		
//		this.caught.put(realCaughtType, caughtAs);
//	}
//
//	public void addPropagated(EIType type)
//	{
//		this.propagated.add(type);
//	}
//
//	public void addThrown(EIType type)
//	{
//		this.thrown.add(type);
//	}
//
//	public void addRethrown(EIType type)
//	{
//		this.rethrown.add(type);
//	}
//
//	public void addWrapped(EIType wrapperType, EIType wrappedType)
//	{
//		Set<EIType> wrappedAs = this.caught.get(wrapperType);
//		if ( wrappedAs == null )
//		{
//			wrappedAs = new HashSet<>();
//		}
//		
//		wrappedAs.add(wrappedType);
//		
//		this.caught.put(wrapperType, wrappedAs);
//	}
//	
//	public Map<EIType, Set<EIType>> getCaught() {
//		return caught;
//	}
//
//	public Set<EIType> getPropagated() {
//		return propagated;
//	}
//
//	public Set<EIType> getThrown() {
//		return thrown;
//	}
//
//	public Set<EIType> getRethrown() {
//		return rethrown;
//	}
//
//	public Map<EIType, Set<EIType>> getWrapped() {
//		return wrapped;
//	}
	
	
	@Override
	public int compare(Signaler a, Signaler b)
	{
		int typeNameComp = a.getType().getQualifiedName().compareTo(b.getType().getQualifiedName());

		if ( typeNameComp != 0 )
		{
			return typeNameComp ;
		}
		else
		{
			int handleIdentifierComp = a.getHandleIdentifier().compareTo(b.getHandleIdentifier());
			
			if ( handleIdentifierComp != 0 )
			{
				return handleIdentifierComp;
			}
			else
			{
				int startPositionComp = a.getStartPosition() - b.getStartPosition();
				
				if ( startPositionComp != 0 )
				{
					return startPositionComp;
				}
				else
				{
					int lenghtComp = a.getLenght() - b.getLenght();
					
					if ( lenghtComp != 0 )
					{
						return lenghtComp;
					}
					else
					{
						return 0;
					}
				}
			}
		}
	}
	
	public String toString ()
	{
		StringBuilder builder = new StringBuilder();
		String delimiter = "";
		
		for ( Signaler s : this.signalers )
		{
			builder.append(delimiter);
			builder.append(s);
			delimiter = ", ";
		}
		
		return builder.toString();
	}
}
