/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group and Project Path Variable Support
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;

/**
 * Object for describing the characteristics of linked resources that are stored in the project
 * description.
 */
public class LinkDescription implements Comparable {
	public static final URI VIRTUAL_LOCATION= getVirtualLocation();

	private static URI getVirtualLocation() {
		try {
			return new URI("virtual:/virtual"); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			//cannot happen
			return null;
		}
	}

	private URI localLocation;

	/**
	 * The project relative path.
	 */
	private IPath path;

	/**
	 * The resource type (IResource.FILE or IResoruce.FOLDER)
	 */
	private int type;

	public LinkDescription() {
		this.path= Path.EMPTY;
		this.type= -1;
	}

	public LinkDescription(IResource linkedResource, URI location) {
		super();
		Assert.isNotNull(linkedResource);
		Assert.isNotNull(location);
		this.type= linkedResource.getType();
		this.path= linkedResource.getProjectRelativePath();
		this.localLocation= location;
	}

	public boolean equals(Object o) {
		if (!(o.getClass() == LinkDescription.class))
			return false;
		LinkDescription other= (LinkDescription)o;
		return localLocation.equals(other.localLocation) && path.equals(other.path) && type == other.type;
	}

	public URI getLocationURI() {
		return localLocation;
	}

	/**
	 * Returns the project relative path of the resource that is linked.
	 * 
	 * @return the project relative path of the resource that is linked.
	 */
	public IPath getProjectRelativePath() {
		return path;
	}

	public int getType() {
		return type;
	}

	public boolean isGroup() {
		return localLocation.equals(VIRTUAL_LOCATION);
	}

	public int hashCode() {
		return type + path.hashCode() + localLocation.hashCode();
	}

	public void setLocationURI(URI location) {
		this.localLocation= location;
	}

	public void setPath(IPath path) {
		this.path= path;
	}

	public void setType(int type) {
		this.type= type;
	}

	/**
	 * Compare link descriptions in a way that sorts them topologically by path. This is important
	 * to ensure we process links in topological (breadth-first) order when reconciling links. See
	 * {@link Project#reconcileLinksAndGroups(ProjectDescription)}.
	 */
	public int compareTo(Object o) {
		LinkDescription that= (LinkDescription)o;
		IPath path1= this.getProjectRelativePath();
		IPath path2= that.getProjectRelativePath();
		int count1= path1.segmentCount();
		int compare= count1 - path2.segmentCount();
		if (compare != 0)
			return compare;
		for (int i= 0; i < count1; i++) {
			compare= path1.segment(i).compareTo(path2.segment(i));
			if (compare != 0)
				return compare;
		}
		return 0;
	}
}
