package ufrn.dimap.lets.exceptionexpert.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Scripts
{
	// Lê o resumo e calcula o tempo total de análise e o número de interfaces excepcionais não calculadas
	public static void main (String args[]) throws IOException
	{
		long totalGraphTime = 0;
		long totalInterfaceTime = 0;
		int failedInterface = 0;
		
		List<String> lines = Files.readAllLines(Paths.get("C:/Users/mafeu_000/Ambiente PLEA WALA 1.4.3/log/resumo.txt"));
		lines.remove(0);
		for ( String line : lines )
		{
			String data[] = line.split("\t");
			
			totalGraphTime += Long.parseLong(data[1]);
			
			if ( data.length > 2 )
			{
				totalInterfaceTime += Long.parseLong(data[2]);
			}
			else
			{
				failedInterface++;
			}
		}
		
		System.out.println("Graph: " + totalGraphTime);
		System.out.println("Interface: " + totalInterfaceTime);
		System.out.println("Failed interfaces: " + failedInterface);
	}
}
