/*
 * Copyright (c) 2004-2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: aschultz $'
 * '$Date: 2010-02-22 16:21:40 -0800 (Mon, 22 Feb 2010) $' 
 * '$Revision: 23182 $'
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */

package org.kepler.actor.job;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kepler.configuration.ConfigurationManager;
import org.kepler.configuration.ConfigurationNamespace;
import org.kepler.configuration.ConfigurationProperty;
import org.kepler.job.Job;
import org.kepler.job.JobException;
import org.kepler.job.JobFactory;
import org.kepler.job.JobManagerFactory;
import org.kepler.job.JobStatusCode;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

/**
 * A generic job launcher actor that can launch a job using PBS, NCCS, Condor,
 * Loadleveler, or SGE, and wait till a user specified status.
 * <p> 
 * JobLauncher actor is based on code from Norbert Podhorszki's JobCreator, JobManager, 
 * JobStatus, and JobSubmitter actors. It uses JobLauncher.properties to 
 * find the list of supported job schedulers and the corresponding support class.
 * @author Frankie Kwok, Chandrika Sivaramakrishnan
 * @version $Id$

 */
@SuppressWarnings("serial")
public class GenericJobReconnect extends TypedAtomicActor {
	public GenericJobReconnect(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		/** Job creator parameter and port */
		// target selects the machine where the jobmanager is running
		target = new PortParameter(this, "target", new StringToken(
				"[local | [user]@host]"));
		new Parameter(target.getPort(), "_showName", BooleanToken.TRUE);

		// real job id generated by the scheduler
		realJobId = new PortParameter(this, "real job id", new StringToken(
				""));
		new Parameter(realJobId.getPort(), "_showName", BooleanToken.TRUE);
		
		// working dir name parameter & port
		workdir = new PortParameter(this, "workdir", new StringToken(
				".kepler-hpcc"));
		new Parameter(workdir.getPort(), "_showName", BooleanToken.TRUE);
		
		/** Job Manager parameter and port */
		// jobManager denotes the name of the actual job manager
		scheduler = new PortParameter(this, "scheduler", new StringToken(
				"One of [Condor | PBS | LoadLeveler | SGE | Fork]"));
		// jobManager.setStringMode(true); // string mode (no "s, but no
		// variables as well!
		new Parameter(scheduler.getPort(), "_showName", BooleanToken.TRUE);

		// flag to set if you want the actor to stage the default fork script
		defaultForkScript = new Parameter(this, "Use default fork script",
				new BooleanToken(false));
		defaultForkScript.setTypeEquals(BaseType.BOOLEAN);
		defaultForkScript.setVisibility(Settable.EXPERT);
		// binPath is the full path to the jobmanager commands on the target
		// machine
		binPath = new Parameter(this, "binary path");
		binPath.setVisibility(Settable.EXPERT);

		/** Job Status parameter and port */
		
		waitUntil = new Parameter(this, "Wait Until Status", new StringToken(
		"ANY"));
		waitUntil.setStringMode(true);
		waitUntil.addChoice("ANY");
		for (JobStatusCode code : JobStatusCode.values()) {
			waitUntil.addChoice(code.toString());
		}
		
		sleepWhileWaiting = new Parameter(this, "Wait Until Sleep (seconds)",
				new IntToken(_sleepWhileWaitingVal));
		sleepWhileWaiting.setTypeEquals(BaseType.INT);

		// Output: jobID of the submitted job
		jobOut = new TypedIOPort(this, "jobOut", false, true);
		jobOut.setTypeEquals(BaseType.OBJECT);
		new Parameter(jobOut, "_showName", BooleanToken.TRUE);

		// Output: log
		logPort = new TypedIOPort(this, "logPort", false, true);
		logPort.setTypeEquals(BaseType.STRING);
		new Parameter(logPort, "_showName", BooleanToken.TRUE);

		//Output: success
		success = new TypedIOPort(this, "success", false, true);
		success.setTypeEquals(BaseType.BOOLEAN);
		new Parameter(success, "_showName", BooleanToken.TRUE);
		
		//Output: reconnect. True if the job was found in queue. 
		//False if job was not in queue. Not in queue could be either
		//because the job completed or if the jobid passed was wrong. 
		reconnect = new TypedIOPort(this, "reconnect", false, true);
		reconnect.setTypeEquals(BaseType.BOOLEAN);
		new Parameter(reconnect, "_showName", BooleanToken.TRUE);
	}

	/***********************************************************
	 * ports and parameters
	 */

	/**
	 * The real job id generated by the scheduler when the job was
	 * originally submitted
	 * 
	 * <p>
	 * This parameter is read each time in fire().
	 * </p>
	 */
	public PortParameter realJobId;

	/**
	 * The working directory in which the actual job submission command will be
	 * executed (on the remote machine if the job manager is a remote
	 * jobmanager).
	 * 
	 * <p>
	 * It should be an absolute path, or a relative one. In the latter case on
	 * remote machine, the directory path will be relative to the user's home
	 * directory (coming from the use of ssh).
	 * </p>
	 * By default, a new unique sub directory is created within this workdir based on 
	 * the job id created by kepler. Job is run from this sub directory. This can
	 * be overwritten by setting the parameter "use given workdir" 
	 * <p>
	 * This parameter is read each time in fire().
	 * </p>
	 */
	public PortParameter workdir;

	/**
	 * The name of the jobmanager to be used It should be a name, for which a
	 * supporter class exist as <i>org.kepler.job.JobSupport<jobManager>.class
	 * 
	 * This parameter is read each time in fire().
	 */
	public PortParameter scheduler;

	/**
	 * Boolean flag to indicate if the default fork script should be staged. If
	 * bin path is provided the default script is uploaded to bin path, else it
	 * is uploaded to the working directory
	 */
	public Parameter defaultForkScript;

	/**
	 * The machine to be used at job submission. It should be null, "" or
	 * "local" for the local machine or [user@]host to denote a remote machine
	 * accessible with ssh.
	 * 
	 * This parameter is read each time in fire().
	 */
	public PortParameter target;

	/**
	 * The path to the job manager commands on the target machines. Commands are
	 * constructed as <i>binPath/command</i> and they should be executable this
	 * way. This parameter is read each time in fire().
	 */
	public Parameter binPath;

	/**
	 * The job is passed on in this actor. This token can be used (delaying it
	 * with a Sleep actor) to ask its Status again and again until the job is
	 * finished or aborted. This port is an output port of type Object.
	 */
	public TypedIOPort jobOut;

	/**
	 * Logging information of job status query. Useful to inform user about
	 * problems at unsuccessful status query but it also prints out job status
	 * and job id on successful query. This port is an output port of type
	 * String. The name of port on canvas is 'log'
	 */
	public TypedIOPort logPort;

	/**
	 * Wait until the job has a reached specific status. The available status'
	 * that can be reached are: any, wait, running, not in queue, and error.
	 */
	public Parameter waitUntil;

	/**
	 * Amount of time (in seconds) to sleep between checking job status.
	 */
	public Parameter sleepWhileWaiting;

	/**
	 * The exit code of the command. If the exit code is 0, the command was
	 * performed successfully. If the exit code is anything other than a 0, an
	 * error occured.
	 */
	// public TypedIOPort exitcode;

	/**
	 * true if reconnect was successful, false otherwise  
	 */
	 public TypedIOPort success;
	 
	 /**
	  * true if the job was found in queue.
	  * False if job was not in queue. Not in queue could be either 
	  * because the job completed or if the jobid passed was wrong.
	  * Workflows that get jobid from user might want to check this
	  * flag in addition to the port success 
	  */
	 public TypedIOPort reconnect;
	 
	/**
	 * fire
	 * 
	 * @exception IllegalActionException
	 *                Not thrown.
	 */
	public void fire() throws IllegalActionException {
		super.fire();

		/* Job creation by processing port parameters */
		System.out.println("KEPLER HOME IS "+System.getProperty("KEPLER"));
		realJobId.update();
		workdir.update();

		String strLog = null;
		String strBinPath = null;
		
		boolean bDefaultFork = false;
		
		if (this.getAttribute("_expertMode") != null) {
			Token temp = null;

			temp = (binPath != null) ? binPath.getToken() : null;
			strBinPath = (temp != null) ? ((StringToken) temp).stringValue()
					.trim() : null;
			bDefaultFork = ((BooleanToken) defaultForkScript.getToken())
					.booleanValue();
		}

		scheduler.update();
		target.update();

		Token token = realJobId.getToken();
		String strRealJobId = (token==null) ? null : ((StringToken)token ).stringValue().trim();
		
		token = workdir.getToken();
		String strWorkdir = (token==null) ? null :((StringToken) token).stringValue().trim();
		
		token = scheduler.getToken();
		String strScheduler = (token==null) ? null :((StringToken)token).stringValue().trim();
		
		token = target.getToken();
		String strTarget = (token==null) ? null :((StringToken)token).stringValue().trim();
		
		// create job object
		String strJobID = JobFactory.create();
		Job _job = JobFactory.get(strJobID);
		try{
			// set _job's  working dir 
			if (strWorkdir != null && strWorkdir.trim().length() > 0) {
				//never create a sub directory - use given dir as working dir
				_job.setWorkdir(strWorkdir,false);  
			} else {
				throw new JobException("Please provide a valid working directory");
			}
	
			if (strRealJobId != null && strRealJobId.trim().length() > 0) {
				_job.status.jobID = strRealJobId ;
			} else {
				throw new JobException("Please provide a valid job id");
			}
			//Set binfile
			if(bDefaultFork){
				File resourcesDir = ConfigurationManager.getModule("actors").getResourcesDir();
				File binFile = new File(resourcesDir,"jmgr-fork.sh");
				if(!binFile.exists()){
					throw new JobException("Unable to locate default fork script - "
							+ binFile.getAbsolutePath() + ". Please copy fork script manually.");
				}
				_job.setBinFile(binFile.getAbsolutePath(), true);

			}
			if("Fork".equalsIgnoreCase(strScheduler)){
				//Set the bin path explicitly if it is not already set
				//This is required because running jmgr-fork.sh without path 
				// fails with command not found. 
				//It works only if there is an absolute or relative path prefix
				if(strBinPath == null || strBinPath.trim().equals("")){
					strBinPath = _job.getWorkdirPath(); //setWorkdir was already called 
													//so, this method should return the right path
				}
			}

		}catch(JobException ex){
			log.error(ex);
			JobFactory.remove(strJobID);
			strJobID = "";
			_job = null;
			throw new IllegalActionException("Error creating job: "
					+ ex.toString());
		}
		
		/* Process the input scheduler */
		org.kepler.job.JobManager myJmgr = null;
		try{
			if (strScheduler == null || strScheduler.equals("")) {
				throw new JobException(
				"Please provide a valid input for the port/parameter scheduler.");
			}
			//Find the job support class name prefix for a given job scheduler name 
			//This is done to keep the parameter case insensitive. 
			//For example the string pbs or PBS or Pbs would map to the right class name prefix say PBS
			ConfigurationProperty cp = ConfigurationManager.getInstance().
		        getProperty(ConfigurationManager.getModule("salssajob-module"), 
		          new ConfigurationNamespace("JobLauncher"));
			List<ConfigurationProperty> properties = cp.findProperties("name",
					strScheduler.toLowerCase(), true);
			String jobsSupported = null;
			if(properties.size() != 0){
				ConfigurationProperty prop = properties.get(0);
				jobsSupported = prop.getProperty("value").getValue();
			}
		    if (jobsSupported != null) {
		      strScheduler = jobsSupported;
		    } else {
		    	throw new JobException("Job Scheduler " + strScheduler
						+ " is not supported."); 
		    }
	
		    // Create a JobManager object or get it if it was already created
			if (isDebugging)
				log.debug("Create/get JobManager object. Name = "
						+ strScheduler + "; target = " + strTarget
						+ "; binPath = " + strBinPath);
			myJmgr = JobManagerFactory.get(strScheduler, strTarget, strBinPath);

		} catch (JobException ex) {
			log.error("Job manager object could not be created. " + ex);
			myJmgr = null;
			JobFactory.remove(strJobID);
			strJobID = "";
			throw new IllegalActionException("JobManager Error: "
					+ ex.toString());
		}

		/* Job Reconnect */
		boolean bSucc = false;
		boolean bReconnect = false;
		try {
			if (_job == null) {
				throw new JobException("JobSubmitter: incoming Job is null");
			}

			if (isDebugging) {
				log.debug("JobSubmit: reconnect to job " + strRealJobId + "...");
			}
			_job.reconnect(myJmgr);
			if(_job.status.statusCode == JobStatusCode.NotInQueue){
				StringBuffer sblog = new StringBuffer(100);
				sblog.append("JobStatus: Status of job ")
					.append(_job.getJobID())
					.append( ": ")
					.append(JobStatusCode.NotInQueue)
					.append("\nWarning:Initial status query couldn't find")
					.append(" job in queue. Either job id is invalid or job is")
					.append(" complete and not in queue anymore");
				success.send(0, new BooleanToken(true));
				reconnect.send(0, new BooleanToken(bReconnect));
				logPort.send(0, new StringToken(sblog.toString()));
				jobOut.send(0, new ObjectToken(_job));
				//No job to do status check on. Return
				return;
			}
			log.info("Reconnected successfully to "+ strRealJobId);
			bReconnect = true;
			log.info("Reconnected to job " + _job.status.jobID);
		} catch (JobException ex) {
			log.error(ex);
			strLog = "JobReconnect Error: " + ex.toString();
			logPort.send(0, new StringToken(ex.toString()));
			success.send(0, new BooleanToken(bSucc));
			reconnect.send(0, new BooleanToken(bReconnect));
			return;
		}
		

		/* Job Status Checking */
		JobStatusCode jobStatusCode;
		try {
			jobStatusCode = _checkStatus(_job);

			// while (_waitUntilCode != null && _waitUntilCode != jobStatusCode)
			// {
			//Loop if there is no match and job status is NOT Error or NotInQueue.
			//Second check is necessary to avoid infinite loop in case where job
			//never gets to the user requested state or if the state goes undetected
			//(say during sleep between poll). 
			while (!matchStatus(jobStatusCode) && jobStatusCode.ordinal()>1) {
				Long time = 1000L * _sleepWhileWaitingVal;
				Thread.sleep(time);
				jobStatusCode = _checkStatus(_job);
			}
			bSucc = true;
		} catch (Exception ex) {
			log.error(ex);
			jobStatusCode = JobStatusCode.Error;
			strLog = "JobStatus Error: " + ex.toString();
			bSucc = false;
			success.send(0, new BooleanToken(bSucc));
			reconnect.send(0, new BooleanToken(bReconnect));
			logPort.send(0, new StringToken(strLog));
			return;
		}

		if (_job != null) {
			strLog = new String("JobStatus: Status of job " + _job.getJobID()
					+ ": " + jobStatusCode.toString());
			jobOut.send(0, new ObjectToken(_job));
		}
		success.send(0, new BooleanToken(bSucc));
		reconnect.send(0, new BooleanToken(bReconnect));
		logPort.send(0, new StringToken(strLog));
	}

	/** React to a change in an attribute. */
	public void attributeChanged(Attribute attribute)
			throws IllegalActionException {
		if (attribute == waitUntil) {
			String waitUntilStr = waitUntil.getExpression();
			waitUntilStr = waitUntilStr.trim();
			String[] split = waitUntilStr.split("\\s*,\\s*");
			_waitUntilCodes = new ArrayList<String>(Arrays.asList(split));
			// check validity
			if (_waitUntilCodes.contains("ANY")) {
				_waitUntilCodes.clear();
			} else {
				for (int i = 0; i < _waitUntilCodes.size(); i++) {
					JobStatusCode waitUntilCode = JobStatusCode
							.getFromString(_waitUntilCodes.get(i));
					if (waitUntilCode == null) {
						throw new IllegalActionException(this,
								"Invalid job status type: "
										+ _waitUntilCodes.get(i));
					}
				}
			}
		} else if (attribute == sleepWhileWaiting) {
			if ((IntToken) sleepWhileWaiting.getToken() != null) {
				_sleepWhileWaitingVal = ((IntToken) sleepWhileWaiting
						.getToken()).intValue();
				if (_sleepWhileWaitingVal < 0) {
					throw new IllegalActionException(this,
							"Sleep While Waiting value cannot be negative.");
				}
			}
		} else {
			super.attributeChanged(attribute);
		}
	}

	private JobStatusCode _checkStatus(Job job) throws Exception {
		JobStatusCode jobStatusCode = JobStatusCode.Error;
		if (job == null) {
			throw new Exception("JobStatus: Job is null");
		}

		job.status(); // successful query or exception

		jobStatusCode = job.status.statusCode;
		log.info("Status of job " + job.getJobID() + ": "
				+ jobStatusCode.toString());
		return jobStatusCode;
	}
	
	private boolean matchStatus(JobStatusCode jobStatusCode) {
		String str = jobStatusCode.toString();

		if (_waitUntilCodes.size() == 0 || _waitUntilCodes.contains(str)) {
			return true;
		}
		return false;
	}
	
	private static final Log log = LogFactory.getLog(GenericJobLauncher.class
			.getName());
	private static final boolean isDebugging = log.isDebugEnabled();
	//private JobStatusCode _waitUntilCode = null;
	private ArrayList<String> _waitUntilCodes = new ArrayList<String>();
	private int _sleepWhileWaitingVal = 5;
}
