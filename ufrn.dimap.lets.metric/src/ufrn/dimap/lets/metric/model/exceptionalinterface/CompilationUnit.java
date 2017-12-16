package ufrn.dimap.lets.metric.model.exceptionalinterface;

import java.util.ArrayList;
import java.util.List;

public class CompilationUnit implements Comparable<CompilationUnit>
{
	public String path;
	public List<Type> types;
	
	public CompilationUnit (String path)
	{
		this.types = new ArrayList<> (); 
	}

	public int compareTo(CompilationUnit compUnit)
	{
		return this.path.compareTo(compUnit.path);
	}
	
}
