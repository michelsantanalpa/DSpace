/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.Serializable;

public interface RootObject<PK extends Serializable> {

    int getType();

    String getName();

    PK getID();

    String getHandle();
}
