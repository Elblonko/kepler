/*
 * Copyright (c) 2009-2010 The Regents of the University of California.
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

/**
 *  A tableModel that uses PersistentVector for disk-based storage. 
 * This should allow for very large tables
 */

package util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;
import java.util.Vector;

public class PersistentTableModel extends javax.swing.table.AbstractTableModel {
	PersistentVector pv;
	Vector colNames = null;
	Vector colTypes = null;

	String delimiter = "\t";

	String field_delimiter = "#x09";

	// first row of data (allow for comments preceeding data)
	int firstRow = 0;

	/*
	 * a stack for saving model changes. onntent of stack is an array St[] of
	 * strings St[0] = row; St[1] = col; St[2] = old cell value St[3] = new cell
	 * value; St[4] = description; St[5] = user annotation
	 */
	Stack changeLogStack;
	/*
	 * flag to set whether or not changes are saved.
	 */
	boolean logFlag = false;

	/**
	 * flag which is set to true whenever anything in the table has been changed
	 */
	boolean changeFlag = false;

	public PersistentTableModel(PersistentVector perV) {
		super();
		pv = perV;
		changeLogStack = new Stack();
		changeFlag = false;
	}

	public PersistentTableModel(PersistentVector perV, Vector colNames) {
		super();
		pv = perV;
		this.colNames = colNames;
		changeLogStack = new Stack();
		changeFlag = false;
	}

	public PersistentVector getPersistentVector() {
		return pv;
	}

	public void setPersistentVector(PersistentVector pv) {
		this.pv = pv;
	}

	public boolean getChangeFlag() {
		return changeFlag;
	}

	public void setChangeFlag(boolean cflag) {
		changeFlag = cflag;
	}

	public void setFieldDelimiter(String s) {
		this.field_delimiter = s.trim();
	}

	public void saveAsFile(String fileName) {
		pv.writeObjects(fileName);
	}

	public Class getColumnClass(int col) {
		if (colTypes == null) {
			this.initColTypes();
		}
		return (Class) colTypes.elementAt(col);
	}

	/**
	 * Utility method for constructing the list of column types for this table
	 * model. The default column type is a String. All values in a column must
	 * be of the same type if they are to be considered as anything other than a
	 * String. Missing values (i.e. "N/A", "-") in numeric columns will force
	 * the column type to be String.
	 */
	private void initColTypes() {
		this.colTypes = new Vector();
		// iterate over columns
		for (int col = 0; col < this.getColumnCount(); col++) {
			Vector classList = new Vector();
			// iterate over the rows
			for (int i = 0; i < this.getRowCount(); i++) {
				// find the class of each entry in the column
				Object value = this.getValueAt(i, col);
				if (value == null) {
					continue;
				}
				Class c = value.getClass();
				if (!classList.contains(c)) {
					classList.add(c);
				}
			}
			// if there is a single class in this whole column, then set it as
			// the colType
			if (classList.size() == 1) {
				this.colTypes.add(classList.elementAt(0));
			} else {
				// default to string (if there were a mix of types)
				this.colTypes.add(String.class);
			}
		}
	}

	/**
	 * Attempts to cast the given String object as a number: First as an
	 * Integer, then as a Double. If neither cast is successful, the original
	 * String version is used
	 * 
	 * @param obj
	 *            String that may actually be a Number
	 * @return an Object that had [possibly] been recast as a Number
	 */
	private Object recast(String obj) {
		Object temp = null;
		// int
		try {
			temp = Integer.parseInt(obj);
			if (temp != null) {
				return temp;
			}
		} catch (Exception e) {
			// not an int
		}
		// double
		try {
			temp = Double.parseDouble(obj);
			if (temp != null) {
				return temp;
			}
		} catch (Exception e) {
			// not a double
		}
		// default to the string
		return obj;
	}

	public String getColumnName(int col) {
		String colName = null;
		Object temp = colNames.elementAt(col);
		if (temp instanceof String) {
			colName = (String) temp;
		} else {
			colName = temp.toString();
		}
		return colName;
	}

	public boolean isCellEditable(int rowindex, int colindex) {
		int rlast = getRowCount() - 1;
		int clast = getColumnCount() - 1;
		if (rowindex == rlast) {
			Vector vec = new Vector();
			for (int i = 0; i < getColumnCount(); i++) {
				vec.addElement("");
			}
			addRow(vec);
		}
		return true;
	}

	public int getColumnCount() {
		int numCols = 0;
		if (colNames != null) {
			numCols = colNames.size();
		} else {
			String[] firstRecord = (String[]) pv.elementAt(firstRow);
			numCols = firstRecord.length;
		}
		return numCols;
	}

	public int getRowCount() {
		return pv.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Object ocell;
		Object obj = pv.elementAt(rowIndex);
		// support for vector implementations of a "record"
		if (obj instanceof Vector) {
			obj = this.columnValuesAsArray((Vector) obj);
		}
		String[] record = (String[]) obj;
		if (record.length > columnIndex) {
			ocell = record[columnIndex];
		} else {
			ocell = "";
		}
		// return (" "+ocell);
		return recast((String) ocell);
	}

	/**
	 * converts an array to a vector
	 * 
	 * @param str
	 *            a String array of data for each column in a row
	 * @return a vector with each elements being column data for the row
	 */
	private Vector columnValuesAsVector(String[] str) {
		Vector res = new Vector();
		int imax = str.length;
		for (int i = 0; i < imax; i++) {
			res.addElement(str[i]);
		}
		return res;
	}

	/**
	 * converts a vector to an array
	 */
	private String[] columnValuesAsArray(Vector vec) {
		String[] res = new String[vec.size()];
		for (int i = 0; i < vec.size(); i++) {
			res[i] = (String) vec.elementAt(i);
		}
		return res;
	}

	private boolean inDelimiterList(String token, String delim) {
		boolean result = false;
		int test = delim.indexOf(token);
		if (test > -1) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	private String getDelimiterString() {
		String str = "";
		String temp = field_delimiter;
		if (temp.startsWith("#x")) {
			temp = temp.substring(2);
			if (temp.equals("0A"))
				str = "\n";
			if (temp.equals("09"))
				str = "\t";
			if (temp.equals("20"))
				str = " ";
		} else {
			str = temp;
		}
		return str;
	}

	/**
	 * sorts the table; set sortdir=1 for ascending set sortdir = -1 for
	 * descending
	 */
	public void sort(int colnum, int sortdir) {
		String[] o1Str;
		String[] o2Str;
		String token1;
		String token2;
		final int cn = colnum;
		final int sdir = sortdir;
		long start = System.currentTimeMillis();
		Collections.sort(pv.objectList, new Comparator() {
			String[] o1Str;
			String[] o2Str;
			String token1;
			String token2;

			public int compare(Object o1, Object o2) {
				int res = 0;
				try {
					if (Long.class.isInstance(o1)) {
						o1Str = (String[]) pv.obj.readObject(((Long) o1)
								.longValue());
					} else {
						o1Str = (String[]) o1;
					}
					token1 = o1Str[cn];
					if (Long.class.isInstance(o2)) {
						o2Str = (String[]) pv.obj.readObject(((Long) o2)
								.longValue());
					} else {
						o2Str = (String[]) o2;
					}
					token2 = o2Str[cn];
					res = token1.compareTo(token2);
					res = sdir * res;
				} catch (Exception w) {
				}
				return res;
			}
		});

		long stop = System.currentTimeMillis();
		int time = (int) (stop - start);
		System.out.println("Time = " + time);
		changeFlag = true;
		fireTableStructureChanged();
	}

	public void setValueAt(Object obj, int row, int col) {
		String[] rowA = (String[]) pv.elementAt(row);
		String currentValue = rowA[col];
		rowA[col] = ((String) obj).trim();
		pv.setElementAt(rowA, row);
		pushLogValues(row, col, currentValue, rowA[col], "new cell entry", "");
	}

	public Stack getLogStack() {
		return changeLogStack;
	}

	public void setLogStack(Stack st) {
		changeLogStack = st;
	}

	public void clearLogStack() {
		changeLogStack = new Stack();
	}

	public void setLogFlag(boolean bol) {
		logFlag = bol;
	}

	private void pushLogValues(int row, int col, String oldVal, String newVal,
			String desc, String annotation) {
		changeFlag = true;
		if (logFlag) {
			String[] log = new String[6];
			log[0] = (new Integer(row)).toString();
			log[1] = (new Integer(col)).toString();
			log[2] = oldVal;
			log[3] = newVal;
			log[4] = desc;
			log[5] = annotation;
			changeLogStack.push(log);
		}
	}

	/**
	 * add a row to the end of the data vec is assumed to be a Vector of strings
	 */
	public void addRow(Vector vec) {
		while (vec.size() < getColumnCount())
			vec.addElement(" ");
		String[] record = new String[getColumnCount()];
		for (int i = 0; i < getColumnCount(); i++) {
			record[i] = (String) vec.elementAt(i);
		}
		pv.addElement(record);
		pushLogValues(getRowCount(), -1, "N/A", "N/A",
				"added new row at end of data", "");
		fireTableRowsInserted(pv.size(), pv.size());
	}

	/**
	 * insert a row at indicated position vec is assumed to be a Vector of
	 * strings
	 */
	public void insertRow(int row, Vector vec) {
		while (vec.size() < getColumnCount())
			vec.addElement(" ");
		String[] record = new String[getColumnCount()];
		for (int i = 0; i < getColumnCount(); i++) {
			record[i] = (String) vec.elementAt(i);
		}
		pv.insertElementAt(record, row);
		pushLogValues(row, -1, "N/A", "N/A", "added new row at" + row, "");
		fireTableRowsInserted(row, row);
	}

	/**
	 * delete a row at indicated position vec is assumed to be a Vector of
	 * strings
	 */
	public void deleteRow(int row) {
		pv.removeElementAt(row);
		pushLogValues(row, -1, "N/A", "N/A", "deleted row at" + row, "");
		fireTableRowsDeleted(row, row);
	}

	/**
	 * add a column after the last current column new column is filled with
	 * spaces
	 */
	public void addColumn() {
		PersistentVector newpv = new PersistentVector();
		for (int i = firstRow; i < getRowCount(); i++) {
			Object obj = pv.elementAt(i);
			String[] record = (String[]) obj;
			Vector vals = columnValuesAsVector(record);
			vals.addElement("");
			String[] newRecord = columnValuesAsArray(vals);
			newpv.addElement(newRecord);
		}
		// Get fields of pv
		String oldFieldDelimiter = pv.getFieldDelimiter();
		int oldFirstRow = pv.getFirstRow();
		Vector oldHeaderLinesVector = pv.getHeaderLinesVector();
		pv.delete();
		pv = newpv;
		pv.setFieldDelimiter(oldFieldDelimiter);
		pv.setFirstRow(oldFirstRow);
		pv.setHeaderLinesVector(oldHeaderLinesVector);
		// pv.setFieldDelimiter("#x09");
		pushLogValues(-1, getColumnCount(), "N/A", "N/A",
				"added column at end", "");
		fireTableStructureChanged();
	}

	/**
	 * add a column after the last current column new column is filled with
	 * spaces
	 */
	public void insertColumn(int colnum) {
		PersistentVector newpv = new PersistentVector();
		for (int i = firstRow; i < getRowCount(); i++) {
			Object obj = pv.elementAt(i);
			String[] record = (String[]) obj;
			Vector vals = columnValuesAsVector(record);
			vals.insertElementAt("", colnum);
			String[] newRecord = columnValuesAsArray(vals);
			newpv.addElement(newRecord);
		}
		// Get fields of pv
		String oldFieldDelimiter = pv.getFieldDelimiter();
		int oldFirstRow = pv.getFirstRow();
		Vector oldHeaderLinesVector = pv.getHeaderLinesVector();
		pv.delete();
		pv = newpv;
		pv.setFieldDelimiter(oldFieldDelimiter);
		pv.setFirstRow(oldFirstRow);
		pv.setHeaderLinesVector(oldHeaderLinesVector);
		// pv.setFieldDelimiter("#x09");
		fireTableStructureChanged();
		pushLogValues(-1, colnum, "N/A", "N/A", "added column at " + colnum, "");
	}

	/**
	 * deletes the indicated column
	 */
	public void deleteColumn(int col) {
		PersistentVector newpv = new PersistentVector();
		for (int i = firstRow; i < getRowCount(); i++) {
			Object obj = pv.elementAt(i);
			String[] record = (String[]) obj;
			Vector vals = columnValuesAsVector(record);
			vals.removeElementAt(col);
			String[] newRecord = columnValuesAsArray(vals);
			newpv.addElement(newRecord);
		}
		// Get fields of pv
		String oldFieldDelimiter = pv.getFieldDelimiter();
		int oldFirstRow = pv.getFirstRow();
		Vector oldHeaderLinesVector = pv.getHeaderLinesVector();
		pv.delete();
		pv = newpv;
		pv.setFieldDelimiter(oldFieldDelimiter);
		pv.setFirstRow(oldFirstRow);
		pv.setHeaderLinesVector(oldHeaderLinesVector);
		// pv.setFieldDelimiter("#x09");
		pushLogValues(-1, col, "N/A", "N/A", "deleted column", "");
		fireTableStructureChanged();
	}
}