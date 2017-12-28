package ufrn.dimap.lets.metric.model.exceptionalinterface;

import java.util.HashSet;
import java.util.Set;

public class Method
{
	private String identifier;
	private Set<String> thrownTypes;
	private Set<String> rethrownTypes;
	
	public Method (String identifier)
	{
		this.identifier = identifier;
		this.thrownTypes = new HashSet<>();
		this.rethrownTypes = new HashSet<>();
	}

	public String getIdentifier() {
		return this.identifier;
	}
	
	public void addThrownType (String type)
	{
		this.thrownTypes.add(type);
	}
	
	public void addRethrownType (String type)
	{
		this.rethrownTypes.add(type);
	}
	
	public void addThrownTypes(Set<String> thrownTypes)
	{
		this.thrownTypes.addAll(thrownTypes);
	}
	
	public void addRethrownTypes(Set<String> rethrownTypes)
	{
		this.rethrownTypes.addAll(rethrownTypes);
	}
	
	public Set<String> getThrownTypes ()
	{
		return this.thrownTypes;
	}
	
	public Set<String> getRethrownTypes ()
	{
		return this.rethrownTypes;
	}

	

	
}
