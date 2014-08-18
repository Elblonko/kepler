/*
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: crawl $'
 * '$Date: 2012-11-26 14:22:25 -0800 (Mon, 26 Nov 2012) $' 
 * '$Revision: 31122 $'
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

package org.kepler.gui.popups;

import java.awt.Component;

import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

import org.kepler.gui.kar.DownloadKARFileAction;
import org.kepler.moml.DownloadableKAREntityLibrary;

/**
 * Created by IntelliJ IDEA.
 * User: sean
 * Date: Mar 23, 2010
 * Time: 4:55:12 PM
 */

public class DownloadableKARPopup extends NoLiidLibraryPopup {
	
	public DownloadableKARPopup(TreePath path, Component comp) {
		
		super(path, comp);

		Object obj = getSelectionPath().getLastPathComponent();
		DownloadableKAREntityLibrary entityLibrary;
		try {
			entityLibrary = (DownloadableKAREntityLibrary) obj;
		}
		catch(ClassCastException ex) {
			return;
		}
		
		DownloadKARFileAction dkfa = new DownloadKARFileAction(getParentFrame());
		dkfa.setEntityLibrary(entityLibrary);
		this.add(new JMenuItem(dkfa));		
	}
}
