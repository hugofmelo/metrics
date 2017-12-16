package ufrn.dimap.lets.metric.model.exceptionalinterface;

import java.util.ArrayList;
import java.util.List;

public class Type
{
	public String name;
	public List<Method> methods;
	
	public Type (String name)
	{
		this.name = name;
		this.methods = new ArrayList<>();
	}
}
