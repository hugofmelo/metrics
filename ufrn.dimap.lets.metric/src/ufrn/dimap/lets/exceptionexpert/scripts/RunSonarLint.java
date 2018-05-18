package ufrn.dimap.lets.exceptionexpert.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RunSonarLint {

	private static final String DEFAULT_PROJECTS_ROOT = "C:/Users/mafeu_000/Projetos GitHub/running";
	private static final String DEFAULT_REPORT_FILE = "C:/Users/mafeu_000/Projetos GitHub/sonarlint issues.txt";

	private static Path projectsRoot;
	private static Path report;
	private static List<String> sonarLintExceptionHandlingRules;
	public static void main(String[] args) throws IOException
	{
		configure(args);

		writeHeader();

		Files.newDirectoryStream(projectsRoot, path -> path.toFile().isDirectory())
		.forEach( RunSonarLint::runForProject);
	}

	private static void configure( String[] args ) {
		if ( args.length == 3 )
		{
			System.out.println("Lendo argumentos de linha de comando...");

			projectsRoot = Paths.get(args[1]);
			report = Paths.get(args[2]);
		}
		else
		{
			System.out.println("Usando argumentos default...");

			projectsRoot = Paths.get (DEFAULT_PROJECTS_ROOT);
			report = Paths.get(DEFAULT_REPORT_FILE);
		}

		sonarLintExceptionHandlingRules = new ArrayList<> ();
		sonarLintExceptionHandlingRules.add("\"throws\" declarations should not be superfluous");
		sonarLintExceptionHandlingRules.add("Generic exceptions should never be thrown");
		sonarLintExceptionHandlingRules.add("Try-catch blocks should not be nested");
		sonarLintExceptionHandlingRules.add("Jump statements should not occur in \"finally\" blocks");
		sonarLintExceptionHandlingRules.add("Throwable.printStackTrace(...) should not be called");
		sonarLintExceptionHandlingRules.add("Exceptions should not be thrown in finally blocks");
		sonarLintExceptionHandlingRules.add("Exception classes should be immutable");
		sonarLintExceptionHandlingRules.add("Throwable and Error should not be caught");
		sonarLintExceptionHandlingRules.add("Exception types should not be tested using \"instanceof\" in catch blocks");
		sonarLintExceptionHandlingRules.add("Exceptions should not be thrown from servlet methods");
		sonarLintExceptionHandlingRules.add("Try-with-resources should be used");
		sonarLintExceptionHandlingRules.add("Catches should be combined");
		sonarLintExceptionHandlingRules.add("Classes named like \"Exception\" should extend \"Exception\" or a subclass");
		sonarLintExceptionHandlingRules.add("IllegalMonitorStateException should not be caught");
		sonarLintExceptionHandlingRules.add("\"Iterator.next()\" methods should throw \"NoSuchElementException\"");
		sonarLintExceptionHandlingRules.add("\"catch\" clauses should do more than rethrow");
		sonarLintExceptionHandlingRules.add("Exception should not be created without being thrown");
	}

	private static void runForProject (Path projectPath)
	{

		// Executar sonar lint
		try
		{
			System.out.print("Executando Sonar Lint para projeto: " + projectPath.toFile().getName() + " ... ");

			ProcessBuilder builder = new ProcessBuilder(
					"cmd.exe", "/c", "cd " + projectPath.toFile().getAbsolutePath() + " && sonarlint.bat --charset ISO-8859-1");

			builder.redirectErrorStream(true);
			Process p = builder.start();
		
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while (true)
			{
				line = r.readLine();

				if (line == null) { break; }

				System.err.println(line);
			}	
			System.out.println("Finalizado.");
		}
		catch (IOException e)
		{
			System.out.println("Error.");
			return;
		}

		// Extrair dados do relatório
		try
		{
			System.out.print("Extraindo dados do relatório para projeto: " + projectPath.toFile().getName() + " ... ");
			parseReport(projectPath);
			System.out.println("Finalizado.");
		}
		catch (IOException e)
		{
			System.out.println("Error.");
			return;
		}
	}

	
	private static void parseReport (Path projectPath) throws IOException
	{
		StringBuilder stringBuilder = new StringBuilder();

		File htmlReportFile = new File(projectPath.toFile().getAbsolutePath() + File.separator + ".sonarlint" + File.separator + "sonarlint-report.html");
		SonarLintReport report = crawlSonarLintReport (htmlReportFile);

		stringBuilder.append(projectPath.toFile().getName() + "\t");
		for ( Integer i : applyFilterToReport (report, sonarLintExceptionHandlingRules ) )
		{
			stringBuilder.append(i+"\t");
		}
		stringBuilder.append("\n");

		writeToReport(stringBuilder.toString());	
	}

	
	private static SonarLintReport crawlSonarLintReport(File reportFile) throws IOException
	{
		SonarLintReport sonarLintReport = new SonarLintReport();

		Document htmlDocument = Jsoup.parse(reportFile, "UTF-8", "");
		Element summary = htmlDocument.getElementById("summary");
		Elements rules = summary.getElementsByClass("hoverable");
		for ( Element rule : rules )
		{
			String ruleDescription = rule.getElementsByAttributeValue("align","left").get(0).text(); 
			int occurrences = Integer.parseInt( rule.getElementsByAttributeValue("align","right").get(0).text() );

			sonarLintReport.addEntry(ruleDescription, occurrences);
		}

		return sonarLintReport;
	}

	private static List<Integer> applyFilterToReport ( SonarLintReport report, List<String> filterRules )
	{
		List <Integer> occurrences = new ArrayList<>();

		for ( String rule : filterRules )
		{
			occurrences.add( report.getOccurrencesOfRule(rule) );
		}

		return occurrences;
	}

	private static void writeHeader() throws IOException
	{
		Files.write(report, "".getBytes());
		
		writeToReport("PROJECT\t");

		for ( String s : sonarLintExceptionHandlingRules )
		{
			writeToReport(s+"\t");
		}
		writeToReport("\n");
	}

	private static void writeToReport (String string) throws IOException
	{
		Files.write(report, string.getBytes(), StandardOpenOption.APPEND);
	}
}

class SonarLintReport
{
	private List<SonarLintEntry> entries;	

	public SonarLintReport ()
	{
		entries = new ArrayList<>();
	}

	public void addEntry ( String rule, int occurrences )
	{
		this.entries.add( new SonarLintEntry(rule, occurrences) );
	}

	public Integer getOccurrencesOfRule(String rule)
	{
		for ( SonarLintEntry entry : this.entries )
		{
			if ( entry.getRule().equals(rule) )
			{
				return entry.getOccurrences();
			}
		}

		return 0;
	}
}

class SonarLintEntry
{
	private String rule;
	private int occurrences;

	public SonarLintEntry (String rule, int occurrences)
	{
		this.rule = rule;
		this.occurrences = occurrences;
	}

	public String getRule() { return rule; }	

	public int getOccurrences() { return occurrences; }
}

