/******************************************************************************* 
 * Copyright (c) 2007-2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.test.util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author  valera
 */
public class XMLEntityResolver implements EntityResolver {

    private static final Properties publicEntities = new Properties();
	private static final Properties systemEntities = new Properties();

    public static void registerPublicEntity(String publicId, String url) {
    	System.out.println("XMLEntityResolver");
		publicEntities.setProperty(publicId, url);
    }

    public static void registerPublicEntity(String publicId, Class<?> loader, String resourceName) throws IOException {
    	System.out.println("XMLEntityResolver");
    	URL url = resolve(loader, resourceName);
    	if(url != null) {
			registerPublicEntity(publicId, url.toString());
		}
    }

	public static void registerSystemEntity(String systemId, String url) {
		System.out.println("XMLEntityResolver");
		systemEntities.setProperty(systemId, url);
	}

    public static void registerSystemEntity(String systemId, Class<?> loader, String resourceName) throws IOException {
    	System.out.println("XMLEntityResolver");
    	URL url = resolve(loader, resourceName);
    	if(url != null) {
			registerSystemEntity(systemId, url.toString());
		}
    }
    
    static URL resolve(Class<?> loader, String resourceName) throws IOException {
    	System.out.println("XMLEntityResolver");
    	URL url = loader.getResource(resourceName);
    	return (url == null) ? null : FileLocator.resolve(url);
    }

    public static XMLEntityResolver getInstance() {
    	System.out.println("XMLEntityResolver");
        return new XMLEntityResolver();
    }
    
    boolean deactivate = true;

    private XMLEntityResolver() {System.out.println("XMLEntityResolver");}
    
    public void setDeactivate(boolean b) {
    	System.out.println("XMLEntityResolver");
    	deactivate = b;
    }
    
    public boolean isResolved(String publicId, String systemId) {
    	System.out.println("XMLEntityResolver");
    	if (publicId != null) {
			String url = publicEntities.getProperty(publicId);
			if (url != null) {
				return true;
			}
    	} else if (systemId != null) {
			String url = systemEntities.getProperty(systemId);
			if (url != null) {
				return true;
			}
        }
		return false;
    }

    public InputSource resolveEntity(String publicId, String systemId) 
    	throws SAXException, java.io.IOException {
    	System.out.println("XMLEntityResolver");
		InputSource source = null;
		boolean ok = false;
    	if (publicId != null) {
			String url = publicEntities.getProperty(publicId);
			if (url != null) {
				source = new InputSource(url);
				source.setPublicId(publicId);
				ok = true;
			}
    	}
    	if (!ok && systemId != null) {
			String url = systemEntities.getProperty(systemId);
			if (url != null) {
				source = new InputSource(url);
				source.setSystemId(systemId);
			}
        }

		if(deactivate && (systemId != null) && (source == null) && (systemId.toLowerCase().endsWith(".dtd"))) { // this deactivates DTD //$NON-NLS-1$
			source = new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes())); //$NON-NLS-1$
		}

        return source;
    }

}