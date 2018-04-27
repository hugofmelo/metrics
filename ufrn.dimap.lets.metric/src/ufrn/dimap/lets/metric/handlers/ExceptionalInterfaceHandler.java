package ufrn.dimap.lets.metric.handlers;

import java.util.List;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import ufrn.dimap.lets.exceptionexpert.callgraph.CallGraphPrinter;
import ufrn.dimap.lets.exceptionexpert.callgraph.CallgraphGenerator;
import ufrn.dimap.lets.exceptionexpert.model.MethodNode;
import ufrn.dimap.lets.exceptionexpert.model.Signaler;

public class ExceptionalInterfaceHandler extends AbstractHandler
{	
	private static FileOutputStream stream;
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		try 
		{
			stream = new FileOutputStream("../log/resumo.txt");
			
			List<IMethod> mainMethods = HandlerUtil.getAllMainMethods();
			write ("METHOD\tCALLGRAPH\tINTERFACES\tCALLEES\tINTERFACE\n");
			System.out.println("\nINICIO DA EXECUÇÃO\n");
			long time = System.nanoTime();
			int count = 1;
			for (IMethod method : mainMethods)
			{
				System.out.println("Executando " + count++ + " de " + mainMethods.size() + " métodos main...");
				doIt(method);
			}
			time = System.nanoTime() - time; 

			System.out.println("Tempo total de processamento: " + TimeUnit.SECONDS.convert(time, TimeUnit.NANOSECONDS) + "\n");
			stream.close();
		}
		catch (JavaModelException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return null;
	}

	// TODO mudar esse nome!
	private void doIt (IMethod method) throws IOException
	{
		long time;
		StringBuilder builder = new StringBuilder();
		
		builder.append(method.getDeclaringType().getFullyQualifiedName() + "\t");
		time = System.nanoTime();

		MethodNode methodRoot = CallgraphGenerator.generateGraphFrom(method);

		//		CallgraphVisitor visitor = new CallgraphVisitor();
		//		MethodWrapper wrapper = CallgraphVisitor.convertToWrapper(method);
		//		wrapper.accept(visitor, new NullProgressMonitor());
		//		MethodNode methodRoot = visitor.getRoot();
		time = System.nanoTime() - time; 


		builder.append(TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + "\t");
		

		try
		{
			CallGraphPrinter.printPruned(methodRoot, Paths.get("../log/" + method.getDeclaringType().getFullyQualifiedName() + ".txt"));
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}



		//		System.out.println( "Grafo de chamadas podado:\n" + methodRoot.printPrunedGraph());
		//		System.out.println();

		try
		{
			time = System.nanoTime();
			// TODO processar callgraph e interface junto
			methodRoot.computeExceptionalInterface();
			time = System.nanoTime() - time;

			builder.append(TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + "\t");

			
//			builder.append(methodRoot + "\t");
//			
//			for ( MethodNode callee : methodRoot.getChildren())
//			{
//				builder.append(callee + "-");
//			}
//			builder.append("\t");
//
//			for ( Signaler signaler : methodRoot.getExceptionalInterface().getSignalers() )
//			{
//				builder.append(signaler+"-");
//			}		
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		finally
		{
			builder.append("\n");
			write ( builder.toString() );
		}
	}

	private void write (String s) throws IOException
	{
		stream.write(s.getBytes());
	}
}
