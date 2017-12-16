package ufrn.dimap.lets.metric.model.exceptionalinterface;

import java.util.HashSet;
import java.util.Set;

public class Method
{
	public String identifier;
	public Set<String> thrownTypes;
	public Set<String> rethrownTypes;
	
	public Method (String identifier)
	{
		this.identifier = identifier;
		this.thrownTypes = new HashSet<>();
		this.rethrownTypes = new HashSet<>();
	}
}
