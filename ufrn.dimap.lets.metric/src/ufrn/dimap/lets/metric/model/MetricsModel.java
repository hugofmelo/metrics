package ufrn.dimap.lets.metric.model;

import java.util.ArrayList;
import java.util.List;

public class MetricsModel
{
	private List<SignalerEntry> signalers;
	private List<TryEntry> tries;
	private List<CatchEntry> catches;
	private List<FinallyEntry> finallies;
	private int rethrows;
	private int wrappings;
	
	public MetricsModel ()
	{
		signalers = new ArrayList<SignalerEntry> ();
		tries = new ArrayList<TryEntry> ();
		catches = new ArrayList<CatchEntry> ();
		finallies = new ArrayList<FinallyEntry> ();
		
		rethrows = 0;
		wrappings = 0;
	}
	
	public void addSignalerEntry (SignalerEntry entry)
	{
		this.signalers.add(entry);
	}

	public void addTryEntry(TryEntry entry)
	{
		this.tries.add(entry);
	}
	
	public void addCatchEntry(CatchEntry entry)
	{
		this.catches.add(entry);
	}
	
	public void addFinallyEntry(FinallyEntry entry)
	{
		this.finallies.add(entry);
	}
	
	public List<SignalerEntry> getSignalers()
	{
		return this.signalers;
	}
	
	public List<TryEntry> getTries()
	{
		return this.tries;
	}
	
	public List<CatchEntry> getCatches()
	{
		return this.catches;
	}
	
	public List<FinallyEntry> getFinallies()
	{
		return this.finallies;
	}

	public int getRethrows ()
	{
		return this.rethrows;
	}
	
	public void incrementRethrows ()
	{
		this.rethrows++;
	}

	public int getWrappings ()
	{
		return this.wrappings;
	}
	
	public void incrementWrappings ()
	{
		this.wrappings++;
	}


}
