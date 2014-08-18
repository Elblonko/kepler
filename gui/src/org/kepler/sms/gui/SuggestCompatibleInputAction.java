/*
 * Copyright (c) 2005-2010 The Regents of the University of California.
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

package org.kepler.sms.gui;

import java.awt.event.ActionEvent;

import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.FigureAction;

public class SuggestCompatibleInputAction extends FigureAction {

	/**
	 * REQUIRED *****
	 * 
	 * Constructor
	 * 
	 * @param parent
	 *            the "frame" (derived from ptolemy.gui.Top) where the menu is
	 *            being added. NOTE this parameter is automatically passed to
	 *            your constructor by the menu system at runtime
	 */
	public SuggestCompatibleInputAction(TableauFrame parent) {
		super("");
		// this.parent = parent;
		putValue("tooltip", "Search for compatible input/source components");
	}

	/**
	 * 
	 * Invoked when an action occurs.
	 * 
	 *@param e
	 *            ActionEvent
	 */
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		NamedObj object = getTarget();

		if (object instanceof Entity) {
			Entity entity = (Entity) object;
			// Frame frame = getFrame();
			SuggestCompatibleOperation o = new SuggestCompatibleOperation(null,
					entity, SuggestCompatibleOperation.INPUT);
		}
	}

}