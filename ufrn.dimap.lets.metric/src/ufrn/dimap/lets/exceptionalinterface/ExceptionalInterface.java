package ufrn.dimap.lets.exceptionalinterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExceptionalInterface
{
	private Map<EIType, Set<EIType>> caught;
	private Set<EIType> propagated;
	private Set<EIType> thrown;
	private Set<EIType> rethrown;
	private Map<EIType, Set<EIType>> wrapped;
	
	public ExceptionalInterface ()
	{
		this.caught = new HashMap<>();
		this.propagated = new HashSet<>();
		this.thrown = new HashSet<>();
		this.rethrown = new HashSet<>();
		this.wrapped = new HashMap<>();
	}
	
	public Set<EIType> getExternalExceptions()
	{
		Set<EIType> exceptions = new HashSet<>();
		
		exceptions.addAll(this.thrown);
		exceptions.addAll(this.rethrown);
		exceptions.addAll(this.propagated);
		exceptions.addAll(this.wrapped.keySet());
		
		return exceptions;
	}

	public void addCaught(EIType realCaughtType, EIType catchType)
	{
		Set<EIType> caughtAs = this.caught.get(realCaughtType);
		if ( caughtAs == null )
		{
			caughtAs = new HashSet<>();
		}
		
		caughtAs.add(catchType);
		
		this.caught.put(realCaughtType, caughtAs);
	}

	public void addPropagated(EIType type)
	{
		this.propagated.add(type);
	}

	public void addThrown(EIType type)
	{
		this.thrown.add(type);
	}

	public void addRethrown(EIType type)
	{
		this.rethrown.add(type);
	}

	public void addWrapped(EIType wrapperType, EIType wrappedType)
	{
		Set<EIType> wrappedAs = this.caught.get(wrapperType);
		if ( wrappedAs == null )
		{
			wrappedAs = new HashSet<>();
		}
		
		wrappedAs.add(wrappedType);
		
		this.caught.put(wrapperType, wrappedAs);
	}
	
	public Map<EIType, Set<EIType>> getCaught() {
		return caught;
	}

	public Set<EIType> getPropagated() {
		return propagated;
	}

	public Set<EIType> getThrown() {
		return thrown;
	}

	public Set<EIType> getRethrown() {
		return rethrown;
	}

	public Map<EIType, Set<EIType>> getWrapped() {
		return wrapped;
	}
}
