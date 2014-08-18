/*
 * Copyright (c) 2010 The Regents of the University of California.
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

package org.sdm.spa.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLUIP extends JPanel {
	// Global value so it can be ref'd by the tree-adapter
	static Document document;

	public XMLUIP() {
		// Set up the tree
		JTree tree = new JTree(new DomToTreeModelAdapter());
		System.out.println("checkpoint15\n");
		// Build left-side view
		JScrollPane treeView = new JScrollPane(tree);
		System.out.println("checkpoint16\n");
		// Build right-side view
		JEditorPane htmlPane = new JEditorPane("text/html", "");
		htmlPane.setEditable(false);
		JScrollPane htmlView = new JScrollPane(htmlPane);
		// Build split-pane view
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				treeView, htmlView);
		// splitPane.setContinuousLayout( true );
		// Add GUI components
		// setLayout(new BorderLayout());
		add("Center", splitPane);
	} // constructor

	public static void makeTree(String filename) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setValidating(true);
		// factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse("src/org/sdm/spa/gui/slideSample01.xml");
			// makeFrame();
			final XMLUIP echoPanel = new XMLUIP();
		} catch (SAXParseException spe) {
			// Error generated by the parser
			System.out.println("\n** Parsing error" + ", line "
					+ spe.getLineNumber() + ", uri " + spe.getSystemId());
			System.out.println("   " + spe.getMessage());

			// Use the contained exception, if any
			Exception x = spe;
			if (spe.getException() != null)
				x = spe.getException();
			x.printStackTrace();

		} catch (SAXException sxe) {
			// Error generated during parsing)
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			x.printStackTrace();

		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();

		} catch (IOException ioe) {
			// I/O error
			ioe.printStackTrace();
		}
	}// makeTree

	public static void main(String argv[]) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setValidating(true);
		// factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse("src/org/sdm/spa/gui/slideSample01.xml");
			makeFrame();

		} catch (SAXParseException spe) {
			// Error generated by the parser
			System.out.println("\n** Parsing error" + ", line "
					+ spe.getLineNumber() + ", uri " + spe.getSystemId());
			System.out.println("   " + spe.getMessage());

			// Use the contained exception, if any
			Exception x = spe;
			if (spe.getException() != null)
				x = spe.getException();
			x.printStackTrace();

		} catch (SAXException sxe) {
			// Error generated during parsing)
			Exception x = sxe;
			if (sxe.getException() != null)
				x = sxe.getException();
			x.printStackTrace();

		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();

		} catch (IOException ioe) {
			// I/O error
			ioe.printStackTrace();
		}
	} // main

	public static void makeFrame() {
		// Set up a GUI framework
		JFrame frame = new JFrame("DOM Echo");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		// Set up the tree, the views, and display it all
		final XMLUIP echoPanel = new XMLUIP();
		frame.getContentPane().add("Center", echoPanel);
		frame.pack();
		frame.setSize(600, 400);
		frame.setVisible(true);
	} // makeFrame

	// An array of names for DOM node-types
	// (Array indexes = nodeType() values.)
	static final String[] typeName = { "none", "Element", "Attr", "Text",
			"CDATA", "EntityRef", "Entity", "ProcInstr", "Comment", "Document",
			"DocType", "DocFragment", "Notation", };

	// This class wraps a DOM node and returns the text we want to
	// display in the tree. It also returns children, index values,
	// and child counts.
	public class AdapterNode {
		org.w3c.dom.Node domNode;

		// Construct an Adapter node from a DOM node
		public AdapterNode(org.w3c.dom.Node node) {
			domNode = node;
		}

		// Return a string that identifies this node in the tree
		public String toString() {
			String s = typeName[domNode.getNodeType()];
			String nodeName = domNode.getNodeName();
			if (!nodeName.startsWith("#")) {
				s += ": " + nodeName;
			}
			if (domNode.getNodeValue() != null) {
				if (s.startsWith("ProcInstr"))
					s += ", ";
				else
					s += ": ";
				// Trim the value to get rid of NL's at the front
				String t = domNode.getNodeValue().trim();
				int x = t.indexOf("\n");
				if (x >= 0)
					t = t.substring(0, x);
				s += t;
			}
			return s;
		}

		/*
		 * Return children, index, and count values
		 */
		public int index(AdapterNode child) {
			// System.err.println("Looking for index of " + child);
			int count = childCount();
			for (int i = 0; i < count; i++) {
				AdapterNode n = this.child(i);
				if (child.domNode == n.domNode)
					return i;
			}
			return -1; // Should never get here.
		}

		public AdapterNode child(int searchIndex) {
			// Note: JTree index is zero-based.
			org.w3c.dom.Node node = domNode.getChildNodes().item(searchIndex);
			return new AdapterNode(node);
		}

		public int childCount() {
			return domNode.getChildNodes().getLength();
		}
	}

	// This adapter converts the current Document (a DOM) into
	// a JTree model.
	public class DomToTreeModelAdapter implements javax.swing.tree.TreeModel {
		// Basic TreeModel operations
		public Object getRoot() {
			// System.err.println("Returning root: " +document);
			return new AdapterNode(document);
		}

		public boolean isLeaf(Object aNode) {
			// Determines whether the icon shows up to the left.
			// Return true for any node with no children
			AdapterNode node = (AdapterNode) aNode;
			if (node.childCount() > 0)
				return false;
			return true;
		}

		public int getChildCount(Object parent) {
			AdapterNode node = (AdapterNode) parent;
			return node.childCount();
		}

		public Object getChild(Object parent, int index) {
			AdapterNode node = (AdapterNode) parent;
			return node.child(index);
		}

		public int getIndexOfChild(Object parent, Object child) {
			AdapterNode node = (AdapterNode) parent;
			return node.index((AdapterNode) child);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {
			// Null. We won't be making changes in the GUI
			// If we did, we would ensure the new value was really new,
			// adjust the model, and then fire a TreeNodesChanged event.
		}

		/*
		 * Use these methods to add and remove event listeners. (Needed to
		 * satisfy TreeModel interface, but not used.)
		 */
		private Vector listenerList = new Vector();

		public void addTreeModelListener(TreeModelListener listener) {
			if (listener != null && !listenerList.contains(listener)) {
				listenerList.addElement(listener);
			}
		}

		public void removeTreeModelListener(TreeModelListener listener) {
			if (listener != null) {
				listenerList.removeElement(listener);
			}
		}

		/*
		 * Invoke these methods to inform listeners of changes. (Not needed for
		 * this example.) Methods taken from TreeModelSupport class described at
		 * http://java.sun.com/products/jfc/tsc/articles/jtree/index.html That
		 * architecture (produced by Tom Santos and Steve Wilson) is more
		 * elegant. I just hacked 'em in here so they are immediately at hand.
		 */
		public void fireTreeNodesChanged(TreeModelEvent e) {
			Enumeration listeners = listenerList.elements();
			while (listeners.hasMoreElements()) {
				TreeModelListener listener = (TreeModelListener) listeners
						.nextElement();
				listener.treeNodesChanged(e);
			}
		}

		public void fireTreeNodesInserted(TreeModelEvent e) {
			Enumeration listeners = listenerList.elements();
			while (listeners.hasMoreElements()) {
				TreeModelListener listener = (TreeModelListener) listeners
						.nextElement();
				listener.treeNodesInserted(e);
			}
		}

		public void fireTreeNodesRemoved(TreeModelEvent e) {
			Enumeration listeners = listenerList.elements();
			while (listeners.hasMoreElements()) {
				TreeModelListener listener = (TreeModelListener) listeners
						.nextElement();
				listener.treeNodesRemoved(e);
			}
		}

		public void fireTreeStructureChanged(TreeModelEvent e) {
			Enumeration listeners = listenerList.elements();
			while (listeners.hasMoreElements()) {
				TreeModelListener listener = (TreeModelListener) listeners
						.nextElement();
				listener.treeStructureChanged(e);
			}
		}
	}
} // XMLUIP.java 