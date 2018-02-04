package ufrn.dimap.lets.exceptionalinterface.limitations;

import java.io.IOException;
import java.rmi.activation.ActivationException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import javax.transaction.xa.XAException;

import org.omg.CORBA.portable.ApplicationException;

public class Example extends BaseExample {

	public Example() throws Exception
	{
		// 1
		signaler();
		
//		// 2
//		try
//		{
//			signaler();
//		}
//		catch (IOException e)
//		{
//			throw e;
//		}
//		
//		// 3
//		try
//		{
//			signaler();
//		}
//		catch (Exception e)
//		{
//			throw e;
//		}
//		
//		// 4
//		try
//		{
//			signaler();
//		}
//		catch (IOException e)
//		{
//			throw new SQLException(e);
//		}
//		
//		// 5
//		try
//		{
//			signaler();
//		}
//		catch (Exception e)
//		{
//			try
//			{
//				throw new SQLException(e);
//			}
//			catch (Exception e2)
//			{
//				throw (Exception)e.getCause();
//			}
//			
//			//throw e;
//		}
		
//		super();
//		signaler();
//		new ThrowingConstructor();
//		super.signaler();
//		
//		Exception e = new RuntimeException();
//		throw e;
		//throw new RuntimeException();
		
	}

	protected void signaler() throws IOException
	{
		throw new IOException();
	}
}

class BaseExample
{
	public BaseExample()
	{
		throw new IllegalArgumentException();
	}
	
	protected void signaler() throws IOException
	{
		throw new IllegalArgumentException();
	}
}

class ThrowingConstructor
{
	public ThrowingConstructor()
	{
		throw new RuntimeException();
	}
}