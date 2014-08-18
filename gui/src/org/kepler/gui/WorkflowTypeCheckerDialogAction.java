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

package org.kepler.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.kepler.sms.gui.WorkflowTypeCheckerDialog;
import org.kepler.util.StaticResources;

import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.Entity;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.toolbox.FigureAction;
import diva.gui.GUIUtilities;

/**
 * An action that will create a WorkflowValidationDialog
 * @author Shawn Bowers, Christopher Brooks
 * @version $Id: WorkflowTypeCheckerDialogAction.java 24234 2010-05-06 05:21:26Z welker $
 */
public class WorkflowTypeCheckerDialogAction extends FigureAction {
    public WorkflowTypeCheckerDialogAction() {
        super("Type Check");
    }


    // ////////////////////////////////////////////////////////////////////////////
    // LOCALIZABLE RESOURCES - NOTE that these default values are later
    // overridden by values from the uiDisplayText resourcebundle file
    // ////////////////////////////////////////////////////////////////////////////

    private static String DISPLAY_NAME = StaticResources.getDisplayString(
            "actions.workflow.typeCheck", "Type Check");
    private static String TOOLTIP =  StaticResources.getDisplayString(
            "actions.workflow.typeCheck", "Type Check");
    private static ImageIcon LARGE_ICON = null;
    private static KeyStroke ACCELERATOR_KEY = null;

    // = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().
    // getMenuShortcutKeyMask());
    // //////////////////////////////////////////////////////////////////////////////

    private TableauFrame parent;

    /**
     * Constructor
     * 
     * @param parent
     *            the "frame" (derived from ptolemy.gui.Top) where the menu is
     *            being added.
     */
    public WorkflowTypeCheckerDialogAction(TableauFrame parent) {
        super("");
        if (parent == null) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "ImportArchiveAction constructor received NULL argument for TableauFrame");
            iae.fillInStackTrace();
            throw iae;
        }
        this.parent = parent;

        this.putValue(Action.NAME, DISPLAY_NAME);
        this.putValue(GUIUtilities.LARGE_ICON, LARGE_ICON);
        this.putValue("tooltip", TOOLTIP);
        this.putValue(GUIUtilities.ACCELERATOR_KEY, ACCELERATOR_KEY);
    }

    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        // Only makes sense if this is an ActorGraphFrame.
        Frame frame = getFrame();
        if ( frame instanceof ActorGraphFrame) {
            WorkflowTypeCheckerDialog dialog = new WorkflowTypeCheckerDialog(
                    frame, (Entity) ((ActorGraphFrame) frame).getModel());
            if (dialog != null) {
                dialog.setVisible(true);
            }
        }
    }
}