package ufrn.dimap.lets.exceptionalinterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class CallGraphPrinter
{	
	private CallGraphPrinter()
	{
	}

	public static void printComplete (MethodNode method, Path filePath) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		
	    printCompleteR(method, new Stack<>(), builder, filePath);
	    
	    Files.write(filePath, builder.toString().getBytes());
	}
	
	// TODO refatorar esse write
	private static void printCompleteR (MethodNode method, Stack<MethodNode> processing, StringBuilder builder, Path filePath ) throws IOException
	{
		// Previnir estouro de memória
		// TODO remover esse hardcoded
		if ( builder.length() > 1024*1024 )
		{
			Files.write(filePath, builder.toString().getBytes());
			builder = new StringBuilder();
		}
		
		for ( int i = 0 ; i < processing.size() ; i++ )
			builder.append("  "); 
		
		builder.append (method);
		builder.append("\n");
		
		if ( !processing.contains(method) )
		{
			processing.push(method);
				
			for ( MethodNode n : method.getChildren() )
			{
				printCompleteR(n, processing, builder, filePath);
			}
			
			processing.pop();
		}
	}
	
	public static void printPruned (MethodNode method, Path filePath) throws IOException
	{	
		StringBuilder builder = new StringBuilder();
		
		printPrunedR(method, new HashSet<>(), 0, builder, filePath);
		
		Files.write(filePath, builder.toString().getBytes());
	}
	
	private static void printPrunedR (MethodNode method, Set<MethodNode> discovered, int tabs, StringBuilder builder, Path filePath ) throws IOException
	{	
		// Previnir estouro de memória
		// TODO remover esse hardcoded
		if ( builder.length() > 1024*1024 )
		{
			Files.write(filePath, builder.toString().getBytes());
			builder = new StringBuilder();
		}
		
		for ( int i = 0 ; i < tabs ; i++ )
			builder.append("  "); 
		
		builder.append (method);
		builder.append("\n");
		
		if ( !discovered.contains(method) )
		{
			discovered.add(method);
				
			for ( MethodNode n : method.getChildren() )
			{
				printPrunedR(n, discovered, tabs+1, builder, filePath);
			}
		}
	}	
}
