package org.jacorb.trading.util;

import org.apache.avalon.framework.logger.*;
import org.apache.avalon.framework.configuration.*;

import java.util.*;
import org.omg.CORBA.*;
import java.lang.*;

/**
 * This class is a sort of ThreadPool. It holds a number of QueryThreads
 * which execute the remote query()-calls. It is closely connected to the 
 * QueryContainer-Class.<p>
 * Concurrency issues are adressed with semaphores and the producer-consumer pattern.
 * New Threads are started if the need arises.
 *
 * @author Nicolas Noffke
 */

public class QueryPropagator 
    implements Configurable
{
    /** the configuration object  */
    private org.jacorb.config.Configuration configuration = null;
    private Logger logger = null;

    /**
     * This class represents a thread wich executes the remote query()-calls.
     * For efficiency reasons it is an inner class so we can access attributes
     * of QueryPropagator in an easy fashion.
     *
     */	

    private class QueryThread 
        extends Thread
    {
	private QueryContainer m_query = null;
	private int no = 0;
	private TimeoutThread m_timer;
	
	/**
	 * Default constructor, nothing done here.
	 */
	public QueryThread(TimeoutThread timer)
        {
	    no = threadc++;
	    m_total_threads++;

	    m_timer = timer;

	    start();
	}

	/**
	 * The threads main loop.
	 */
	public void run(){
	    do {
		m_idle_threads++;
		getWork();
		executeQuery();
 	    } while (true);
	}
	
	/**
	 * This method executes the remote query()-call and copies the
	 * caught exceptions to the QueryContainer, if necessary.
	 */
	private synchronized void executeQuery()
	{
	    try
	    {
	      // setting timout, so we don't wait infinitely
		m_timer.setTimeout(this);

		m_query.m_target.query(m_query.m_type,
				       m_query.m_constr,
				       m_query.m_pref,
				       m_query.m_policies,
				       m_query.m_desired_props,
				       m_query.m_how_many,
				       m_query.m_offers,
				       m_query.m_offer_itr,
				       m_query.m_limits_applied);
		m_timer.stopTimer(this);
	    }
	    catch (UserException e)
	    {
		m_query.m_exception = e;

		// initializing anyway, for safety reasons
		m_query.m_offers.value = new org.omg.CosTrading.Offer[0];
		m_query.m_limits_applied.value = new String[0];
	    }
	    catch (Exception e)
	    {
                //		org.jacorb.util.Debug.output(2, e);
		
		// initializing anyway, for safety reasons
		m_query.m_offers.value = new org.omg.CosTrading.Offer[0];
		m_query.m_limits_applied.value = new String[0];
	    }

	    m_query.m_mutex.V(); // releasing lock on QueryContainer, i.e. resultReady() unblocks
	    m_query = null;
	}
	

	/**
	 * Wait for a new QueryContainer to arrive, i.e. be passed in by putWork().
	 * It uses two semaphores and the consumer-producer pattern for concurrency 
	 * control.
	 */
	private void getWork()
        {
	    m_query_cons.P();
	    m_query = m_new_query; // get new QueryContainer

	    m_idle_threads--;
	    m_query_prod.V();

	}
    } // QueryThread


    ////////////////////// Body of QueryPropagator /////////////////////////////////////

    private int  m_idle_threads = 0;  //stores currently idle threads
    private int m_total_threads = 0;  //no. of threads
    private Semaphore m_idle_threads_sema; //mutex for m_idle_threads

    private QueryContainer m_new_query; // new query to be sheduled
    private Semaphore m_query_cons; // consumer mutex aka 'full'
    private Semaphore m_query_prod; // producer mutex aka 'empty'

    private int m_max_threads = 10; //if more threads than this, then we don't 
                                    //start a new thread, even if none are idle
    private int m_min_threads = 5; // if less than this, then we create a new thread
    private int m_query_timeout = 60000; // max time for a query to return;
    private TimeoutThread m_timer = null;

    private static int threadc = 0;
    private int m_debug_verbosity = 2;

    /**
     * Constructor of QueryPropagator
     *
     */
    public QueryPropagator() 
    {
	m_idle_threads_sema = new Semaphore();
	m_query_cons = new Semaphore(0); // setting semaphore to 0, since consumers 
	                                 // must block until first producer issues a V()
	m_query_prod = new Semaphore();

	m_timer = new TimeoutThread(m_query_timeout);
    }
	
    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration = 
            (org.jacorb.config.Configuration)myConfiguration;
        logger = 
            configuration.getNamedLogger("jacorb.trading");

        m_max_threads = 
            configuration.getAttributeAsInteger("jtrader.util.max_threads",10);
        m_min_threads = 
            configuration.getAttributeAsInteger("jtrader.util.min_threads",1);
        m_query_timeout =  
            configuration.getAttributeAsInteger("jtrader.util.query_timeout");
    }



    /**
     * This method takes a new QueryContainer and schedules it to a QueryThread
     * to execute the remote query. Counterpart to getWork. <br>
     * If no threads are idle, a new one is started.
     *
     * @param query New QueryContainer to be executed
     */
    public void  putWork(QueryContainer query){

	boolean _none_idle;
	if (logger.isDebugEnabled()) 
	    logger.debug("Put work (waiting): query(" + query.no + ")");
	m_query_prod.P();
	_none_idle = m_idle_threads < m_min_threads && m_total_threads < m_max_threads;
	m_new_query = query;
	if (logger.isDebugEnabled()) 
	    logger.debug("Put work (got P) query(" + m_new_query.no + ")");
	m_query_cons.V();
	if (logger.isDebugEnabled()) 
	    logger.debug("left put work: query(" + m_new_query.no + ")");

 	if (_none_idle){ 
	    // no threads idle and maximum thread count not reached, so start new one
	    if (logger.isDebugEnabled()) 
		logger.debug("Not enough Threads: " + m_idle_threads);
	    QueryThread _thread = new QueryThread(m_timer);
	    // new thread will call getWork() as first action
 	}
    }
} // QueryPropagator






