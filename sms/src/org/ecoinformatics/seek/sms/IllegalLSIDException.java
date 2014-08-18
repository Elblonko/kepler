/*
 * Copyright (c) 2004-2010 The Regents of the University of California.
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

package org.ecoinformatics.seek.sms;

/**
 *@author Shawn Bowers
 * 
 *         A quick hack at a start to a local Kepler LSID suite of services for
 *         kepler. This ultimately is an implementation of each LSIDxyzService,
 *         but is local to Kepler. Not sure how all of this will eventually work
 *         ...
 * 
 *         This class locally maintains the default actor library. The actor
 *         library is passed at start up; and a hash table is created of all
 *         named objects in the library. New objects can be added, etc. Each new
 *         addition or change should update default actor library, persisting
 *         the change locally.
 *@created February 17, 2005
 */

public class IllegalLSIDException extends Exception {

	/**
	 *Constructor for the IllegalLSIDException object
	 * 
	 *@param lsid
	 *            Description of the Parameter
	 */
	public IllegalLSIDException(String lsid) {
		super("BADLY FORMED LSID: " + lsid);
	}

}