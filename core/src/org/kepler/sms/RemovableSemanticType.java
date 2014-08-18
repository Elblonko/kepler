/*
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
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

package org.kepler.sms;

/**
 * Created by IntelliJ IDEA.
 * User: sean
 * Date: Apr 13, 2010
 * Time: 10:46:23 AM
 */

public class RemovableSemanticType {

	public RemovableSemanticType(SemanticType semanticType, boolean isRemovable) {
		setSemanticType(semanticType);
		setIsRemovable(isRemovable);
	}

	public void setIsRemovable(boolean removable) {
		isRemovable = removable;
	}

	public void setSemanticType(SemanticType semanticType) {
		this.semanticType = semanticType;
	}

	public SemanticType getSemanticType() {
		return semanticType;
	}

	public boolean isRemovable() {
		return isRemovable;
	}

	private boolean isRemovable;
	private SemanticType semanticType;
}
