/*
 * Copyright (c) 2003-2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: welker $'
 * '$Date: 2010-05-05 22:21:26 -0700 (Wed, 05 May 2010) $' 
 * '$Revision: 24234 $'
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

package org.ecoinformatics.seek.gis.gdal;

import java.io.File;

import org.kepler.objectmanager.cache.DataCacheFileObject;
import org.kepler.objectmanager.cache.DataCacheManager;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * <p>
 * This actor provides projection capabilities provided through the GDAL GIS
 * library. It attempts to find the projected file in the kepler file cache
 * before doing the projection. If the file is found, the projection is not
 * performed but the cached file is passed on. The file is cached by it's
 * filename (minus the extension after the '.') and the output format so the
 * cached result can be read from the cache with a tuple like
 * {outputFilename.substring(0, outputFilename.lastIndexOf(".")), outputFormat}.
 * </p>
 * <p>
 * The parameters inputParams and outputParams must be of the form used when
 * passing values to the -s_srs and -t_srs params on the gdalwarp command line
 * See the gdalwarp docs for more info. <br/>
 * http://www.remotesensing.org/gdal/gdalwarp.html
 * </p>
 * <p>
 * Note that the 'cache options' parameter has a choice of 'no cache'. This
 * option will not only not store files in the cache, but it will also ignore
 * previously stored cache items. It can thus be used to force a new calculation
 * even if the item was previousl cached (Dan Higgins)
 * </p>
 */
public class GDALWarpActor extends TypedAtomicActor {
	/** The -s_srs params from the command line. **/
	public StringParameter inputParams = new StringParameter(this,
			"input params");
	/** the -t_srs params from the cammand line. **/
	public StringParameter outputParams = new StringParameter(this,
			"output params");
	/**
	 * The format of the output file taken from the list provided by gdalwarp
	 * --formats on the command line
	 */
	public StringParameter outputFormat = new StringParameter(this,
			"output format");
	/** how to use the cache, if at all */
	public StringParameter cacheOutput = new StringParameter(this,
			"Cache options");
	/** the filename of the input file **/
	public TypedIOPort inputFilename = new TypedIOPort(this, "inputFilename",
			true, false);
	/** the filename (cacheid) of the outputfile **/
	public TypedIOPort outputFilename = new TypedIOPort(this, "outputFilename",
			false, true);
	public TypedIOPort outputCachename = new TypedIOPort(this,
			"outputCachename", false, true);
	public TypedIOPort outputCacheType = new TypedIOPort(this,
			"outputCacheType", false, true);
	/** trigger */
	public TypedIOPort trigger = new TypedIOPort(this, "trigger", true, false);
	/** production rate param to handle the multiport input **/
	public Parameter outputFilenameTokenProductionRate;
	// cache manager
	private DataCacheManager cacheManager;

	/**
	 * Constructor
	 */
	public GDALWarpActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		inputFilename.setTypeEquals(BaseType.STRING);
		inputFilename.setMultiport(true);
		outputFilename.setTypeEquals(BaseType.STRING);
		outputCachename.setTypeEquals(BaseType.STRING);
		outputCacheType.setTypeEquals(BaseType.STRING);
		outputFilenameTokenProductionRate = new Parameter(outputFilename,
				"tokenProductionRate");
		outputFilenameTokenProductionRate.setExpression("0");
		cacheManager = DataCacheManager.getInstance();
	}

	/**
	 * Notify this entity that the links to the specified port have been
	 * altered. This sets the production rate of the output port and notifies
	 * the director that the schedule is invalid, if there is a director.
	 * 
	 * @param port
	 */
	public void connectionsChanged(Port port) {
		super.connectionsChanged(port);
		if (port == inputFilename) {
			try {
				outputFilenameTokenProductionRate.setToken(new IntToken(
						inputFilename.getWidth()));
				// NOTE: schedule is invalidated automatically already
				// by the changed connections.
			} catch (IllegalActionException ex) {
				throw new InternalErrorException(this, ex,
						"Failed to set the token production rate of the "
								+ "outputFilename port");
			}
		}
	}

	/**
	 * Initialize
	 */
	public void initialize() throws IllegalActionException {
	}

	/**
	 * Prefire
	 */
	public boolean prefire() throws IllegalActionException {
		return super.prefire();
	}

	/**
	 * Fire
	 */
	public void fire() throws IllegalActionException {
		super.fire();
		if (trigger.getWidth() > 0 && !trigger.hasToken(0)) { // make sure we're
																// ready to fire
			return;
		}

		// get the params
		String inputParamsStr = inputParams.stringValue();
		String outputParamsStr = outputParams.stringValue();
		String outputFormatStr = outputFormat.stringValue();
		String cacheOutputStr = cacheOutput.stringValue();
		String outputCachenameStr = "";

		// what kind of cache should we use
		int cacheOption = DataCacheFileObject.COPY_FILE_TO_CACHE;
		if (cacheOutputStr.equals("Cache Files but Preserve Location")) {
			cacheOption = DataCacheFileObject.EXTERNAL;
		} else if (cacheOutputStr.equals("No Caching")) {
			cacheOption = -666;
		}

		int inputFilenameWidth = inputFilename.getWidth();

		for (int i = 0; i < inputFilenameWidth; i++) { // loop through each
														// input on the port
			StringToken inputFilenameToken = (StringToken) inputFilename.get(i);
			String inputFilenameStr = inputFilenameToken.stringValue();
			String outputFilenameStr;

			if (inputFilenameStr.indexOf(".") != -1) { // get rid of the . and
														// the extension on the
														// filename
				outputCachenameStr = inputFilenameStr.substring(0,
						inputFilenameStr.lastIndexOf("."));
				outputFilenameStr = outputCachenameStr + "." + outputFormatStr;
			} else {
				outputFilenameStr = inputFilenameStr;
				outputCachenameStr = inputFilenameStr;
			}

			try {
				if (cacheOption != -666) { // if we are using caching...
					// try to get the output file from the cache
					DataCacheFileObject fileItem = cacheManager.getFile(
							outputCachenameStr, outputFormatStr);
					outputFilename.broadcast(new StringToken(fileItem.getFile()
							.getAbsolutePath()));
					outputCachename.broadcast(new StringToken(
							outputCachenameStr));
					outputCacheType.broadcast(new StringToken(outputFormatStr));
				} else {
					throw new NullPointerException();
				}
			} catch (NullPointerException npe) { // if the file doesn't exist in
													// the cache, reproject
													// it.*/
				try {
					File f = new File(outputFilenameStr);
					if (f.exists()) { // delete the old output file if there is
										// one.
						f.delete();
					}
					GDALJniGlue g = new GDALJniGlue();
					String results = g.GDALWarp(inputParamsStr,
							outputParamsStr, outputFormatStr, inputFilenameStr,
							outputFilenameStr);
					if (results != null) {
						throw new IllegalActionException(results);
					}
					// put the output file into the cache
					if (cacheOption != -666) {
						DataCacheFileObject fileItem = DataCacheManager
								.putFile(outputFilenameStr, outputCachenameStr,
										outputFormatStr, cacheOption);
						outputFilename.broadcast(new StringToken(fileItem
								.getFile().getAbsolutePath()));
						outputCachename.broadcast(new StringToken(
								outputCachenameStr));
						outputCacheType.broadcast(new StringToken(
								outputFormatStr));
					} else {
						outputFilename.broadcast(new StringToken(
								outputFilenameStr));
					}
				} catch (Exception e) {
					throw new IllegalActionException(
							"Error running jni code for GDAL: "
									+ e.getMessage());
				}
			}
		}
	}
}