/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.projection;


import org.eclipse.jface.text.Position;


/**
 * Internal class. Do not use. Only public for testing purposes.
 * <p>
 * A fragment is a range of the master document that has an image, the so called
 * segment, in a projection document.
 *
 * @since 3.0
 */
public class Fragment extends Position {

	/**
	 * The corresponding segment of this fragment.
	 */
	public Segment segment;

	/**
	 * Creates a new fragment covering the given range.
	 *
	 * @param offset the offset of the fragment
	 * @param length the length of the fragment
	 */
	public Fragment(int offset, int length) {
		super(offset, length);
	}
}
