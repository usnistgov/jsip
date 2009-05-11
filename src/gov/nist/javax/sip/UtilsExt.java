/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package gov.nist.javax.sip;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public interface UtilsExt {

	/**
	 * Generate a call identifier. This is useful when we want to generate a
	 * call identifier in advance of generating a message.
	 * @since 2.0
	 */
	public String generateCallIdentifier(String address);

	/**
	 * Generate a tag for a FROM header or TO header. Just return a random 4
	 * digit integer (should be enough to avoid any clashes!) Tags only need to
	 * be unique within a call.
	 * 
	 * @return a string that can be used as a tag parameter.
	 * 
	 * synchronized: needed for access to 'rand', else risk to generate same tag
	 * twice
	 * @since 2.0
	 */
	public String generateTag();
	/**
	 * Generate a cryptographically random identifier that can be used to
	 * generate a branch identifier.
	 * 
	 * @return a cryptographically random gloablly unique string that can be
	 *         used as a branch identifier.
	 * @since 2.0
	 */
	public String generateBranchId();
}
