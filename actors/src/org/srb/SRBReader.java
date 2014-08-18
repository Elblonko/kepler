/*
 * Copyright (c) 2002-2010 The Regents of the University of California.
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

package org.srb;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBFileSystem;
import edu.sdsc.grid.io.srb.SRBRandomAccessFile;

//////////////////////////////////////////////////////////////////////////
//// SRBReader
/**
 * <p>
 * SRBReader/StreamGet is a Kepler Actor which has a functionality similar to
 * the SRB command namely "Sget".However SRBReader actor downloads data from the
 * SRB remote file with a streaming process as a byte of arrays instead of a
 * parallel get. The following actor expects as input a reference to the SRB
 * file system. This reference connection is obtained via the SRBConnect Actor
 * in Kepler. <i>See SRBConnect and its documentation.</i>
 * </p>
 * <p>
 * The file reference system is created with a unique SRB user account and with
 * this connection reference as input the SRBReader actor is able to gain access
 * to the SRB file space. Once an alive SRB file connection system has been
 * established the actor gets the remote SRB file path and creates a
 * SRBRandomAccessFile stream. The contents of file are broadcasted as a
 * sequence of bytes of arrays, after reading from the stream in a loop.
 * </p>
 * <p>
 * <B>Actor Input:</B> Accepts a reference to the SRB files system, an SRB
 * remote file name as input.
 * </p>
 * <p>
 * <B>Actor Output:</B> The SRBStreamGet actor outputs the content of the SRB
 * remote file a sequence of byte arrays. Also outputs a endofFile value once
 * end of file has been reached and no more bytes are to be read.
 * 
 * 
 * </p>
 * <p>
 * The following actor accesses SRB file reference system and SRB file space
 * with the SRB Jargon API provided. The JARGON is a pure API for developing
 * programs with a data grid interface and I/O for SRB file systems.
 * </p>
 * <A href="http://www.sdsc.edu/srb"><I>Further information on SRB</I> </A>
 * 
 * @author Bing Zhu and Efrat Jaeger
 * @version $Id: SRBReader.java 24234 2010-05-06 05:21:26Z welker $
 * @since Ptolemy II 3.0.2
 */
public class SRBReader extends TypedAtomicActor {

	/**
	 * Construct an actor with the given container and name.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this actor.
	 * @exception IllegalActionException
	 *                If the actor cannot be contained by the proposed
	 *                container.
	 * @exception NameDuplicationException
	 *                If the container already has an actor with this name.
	 */
	public SRBReader(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {

		super(container, name);

		SRBFileSystem = new TypedIOPort(this, "SRBFileSystem", true, false);
		output = new TypedIOPort(this, "output", false, true);
		remoteFileName = new TypedIOPort(this, "remoteFileName", true, false);
		endOfFile = new TypedIOPort(this, "endOfFile", false, true);

		// Set the type constraint.
		SRBFileSystem.setTypeEquals(BaseType.GENERAL);
		output.setTypeEquals(new ArrayType(BaseType.UNSIGNED_BYTE));
		remoteFileName.setTypeEquals(BaseType.STRING);
		endOfFile.setTypeEquals(BaseType.BOOLEAN);

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"-25\" y=\"-20\" " + "width=\"68\" height=\"40\" "
				+ "style=\"fill:white\"/>\n"
				+ "<polygon points=\"-15,-10 -7,-10 -3,-14 4,-14 8,-10"
				+ " 33,-10 33,10, -15,10\" " + "style=\"fill:red\"/>\n"
				+ "<text x=\"-5\" y=\"7\" " + "style=\"font-size:14\">\n"
				+ "SRB \n" + "</text>\n" + "<text x=\"-20\" y=\"19\""
				+ "style=\"font-size:11; fill:black; font-family:SansSerif\">"
				+ "stream read</text>\n" + "</svg>\n");

	}

	// /////////////////////////////////////////////////////////////////
	// // ports and parameters ////

	/**
	 * Connection reference
	 */
	public TypedIOPort SRBFileSystem;

	/**
	 * Output. Array of bytes.
	 */
	public TypedIOPort output;

	/**
	 * The SRB file to be read.
	 */
	public TypedIOPort remoteFileName;

	/**
	 * End of file when reached, outputs true
	 */
	public TypedIOPort endOfFile;

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Accepts an SRBFileSystem reference and a file name and outputs the file
	 * content (Array of Bytes).
	 */
	public void fire() throws IllegalActionException {
		SRBFile srbFile;
		SRBRandomAccessFile srbRandomAccessFile = null;
		byte[] bytesRead = new byte[20000];
		;
		int nBytesRead;

		try {
			srbFileSystem = (SRBFileSystem) ((ObjectToken) SRBFileSystem.get(0))
					.getValue();
			String _srbFileName = ((StringToken) remoteFileName.get(0))
					.stringValue();
			srbFile = new SRBFile(srbFileSystem, _srbFileName);
			srbRandomAccessFile = new SRBRandomAccessFile(srbFile, "r");

			nBytesRead = srbRandomAccessFile.read(bytesRead);
			while (nBytesRead > 0) {
				Token _bytes[] = new Token[nBytesRead];

				for (int i = 0; i < nBytesRead; i++) {
					_bytes[i] = new UnsignedByteToken(bytesRead[i]);
				}
				output.send(0, new ArrayToken(_bytes));

				nBytesRead = srbRandomAccessFile.read(bytesRead);
				if (nBytesRead == 0) {
					endOfFile.broadcast(BooleanToken.TRUE);
				} else
					endOfFile.broadcast(BooleanToken.FALSE);
			}

		} catch (Exception ex) {
			srbFile = null;
			srbRandomAccessFile = null;
			srbFileSystem = SRBUtil.closeConnection(srbFileSystem);
			throw new IllegalActionException(this, ex, "Reading exception.");
		}
		srbFile = null;
		srbRandomAccessFile = null;
	}

	/**
	 * Post fire the actor. Return false to indicate that the process has
	 * finished.
	 */
	public boolean postfire() {
		return false; // FIX ME
	}

	/**
	 * Disconnect from SRB.
	 */
	public void wrapup() {
		srbFileSystem = SRBUtil.closeConnection(srbFileSystem);
	}

	private SRBFileSystem srbFileSystem = null;

}