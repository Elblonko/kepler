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

package org.kepler.gis.display;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Name: JumpSHPDisplayer.java Purpose:The actor serves as a proxy to invoke a
 * frame window to display ESRI shape file. Knwon problem: the actor currently
 * only takes local file (no "file://" prefix is needed). Directly read remote
 * file and/or use URI is planned. Author: Jianting Zhang Date: August, 2005
 */
public class JumpSHPDisplayer extends TypedAtomicActor {
	// input port: file name of the shapefile to display
	/** Description of the Field */
	public TypedIOPort SHPFileNamePort = new TypedIOPort(this,
			"SHPFileNamePort", true, false);

	/**
	 * Constructor for the JumpSHPDisplayer object
	 * 
	 *@param container
	 *            Description of the Parameter
	 *@param name
	 *            Description of the Parameter
	 *@exception NameDuplicationException
	 *                Description of the Exception
	 *@exception IllegalActionException
	 *                Description of the Exception
	 */
	public JumpSHPDisplayer(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {

		super(container, name);
		SHPFileNamePort.setTypeEquals(BaseType.STRING);

		_attachText(
				"_iconDescription",
				"<svg>\n"
						+ "<rect x=\"-20\" y=\"-15\" "
						+ "width=\"40\" height=\"30\" "
						+ "style=\"fill:lightGrey\"/>\n"
						+ "<rect x=\"-15\" y=\"-10\" "
						+ "width=\"30\" height=\"20\" "
						+ "style=\"fill:white\"/>\n"
						+ "<line x1=\"-13\" y1=\"-6\" x2=\"-4\" y2=\"-6\" "
						+ "style=\"stroke:grey\"/>\n"
						+ "<line x1=\"-13\" y1=\"-2\" x2=\"0\" y2=\"-2\" "
						+ "style=\"stroke:grey\"/>\n"
						+ "<line x1=\"-13\" y1=\"2\" x2=\"-8\" y2=\"2\" "
						+ "style=\"stroke:grey\"/>\n"
						+ "<line x1=\"-13\" y1=\"6\" x2=\"4\" y2=\"6\" "
						+ "style=\"stroke:grey\"/>\n"
						+ "<text x=\"-10\" y=\"-10\" font-size=\"10\" fill=\"blue\">Shape</text>"
						+ "</svg>\n");
	}

	/**
	 *@exception IllegalActionException
	 *                Description of the Exception
	 */
	public void initialize() throws IllegalActionException {
	}

	/**
	 *@return Description of the Return Value
	 *@exception IllegalActionException
	 *                Description of the Exception
	 */
	public boolean prefire() throws IllegalActionException {
		return super.prefire();
	}

	/**
	 *@exception IllegalActionException
	 *                Description of the Exception
	 */
	public void fire() throws IllegalActionException {
		// System.out.println("firing JumpSHPDisplayer");
		super.fire();

		StringToken SHPFileNameToken = (StringToken) SHPFileNamePort.get(0);
		String SHPFileName = SHPFileNameToken.stringValue();
		// note: URI prefix like "file://" is not handling here

		System.out.println(SHPFileName);
		try {
			JumpFrame frame = new JumpFrame(this.getName());
			frame.setVisible(true);
			frame.addSHPLayer(SHPFileName, this.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The main program for the JumpSHPDisplayer class
	 * 
	 *@param args
	 *            The command line arguments
	 *@exception Exception
	 *                Description of the Exception
	 */
	public static void main(String[] args) throws Exception {
		JumpSHPDisplayer d = new JumpSHPDisplayer(null, "");
		d.fire();
	}
}