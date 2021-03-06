/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.common.model.java.handlers;

import java.util.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.*;

import org.jboss.tools.common.model.*;
import org.jboss.tools.common.model.plugin.ModelMessages;
import org.jboss.tools.common.model.plugin.ModelPlugin;
import org.jboss.tools.common.meta.action.impl.*;

public class OpenJavaSourceHandler extends AbstractHandler {

    public OpenJavaSourceHandler() {}

    public boolean isEnabled(XModelObject object) {
        if(object == null) return false;
        String type = getType(object);
        if(type == null || type.length() == 0) return false;
        return true;
    }
    
    protected String getType(XModelObject object) {
    	String attr = getAttribute();
    	if(attr == null) return null;
    	return object.getAttributeValue(attr);
    }

    protected String getAttribute() {
        return action.getProperty("attribute"); //$NON-NLS-1$
    }

    public void executeHandler(XModelObject object, Properties p) throws XModelException {
        if(!isEnabled(object)) return;
        String type = getType(object);
		type = type.replace('.', '/') + ".java"; //$NON-NLS-1$

		if(p == null || p.getProperty("property") == null) { //$NON-NLS-1$
			String n = action.getProperty("property"); //$NON-NLS-1$
			if(n != null) {
				String v = object.getAttributeValue(n);
				if(v != null) {
					if(p == null) p = new Properties();
					p.put("property", v); //$NON-NLS-1$
				}
			}
		}

		try {
			open(object.getModel(), type, p);
		} catch (CoreException e) {
			throw new XModelException(e);
		}
    }
    
    public static void open(XModel model, String type, Properties p) throws XModelException, CoreException {
		IProject project = (IProject)model.getProperties().get(XModelObjectConstants.PROJECT);
		IJavaProject javaProject = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
		IJavaElement javaElement = javaProject.findElement(new Path(type));
		if (javaElement == null) {
			String message = "Cannot find java source.";
			if(p != null && XModelObjectConstants.TRUE.equals(p.getProperty("ignoreWarning"))) { //$NON-NLS-1$
				p.setProperty("error", message); //$NON-NLS-1$
			} else {
				ServiceDialog d = model.getService();
				d.showDialog(ModelMessages.WARNING, message, new String[]{"Close"}, null, ServiceDialog.WARNING);
			}
		} else {
			if(p != null && XModelObjectConstants.TRUE.equals(p.getProperty("onlySelectIfOpen"))) { //$NON-NLS-1$
				IEditorInput ii = EditorUtility.getEditorInput(javaElement);
				IWorkbenchPage page = getWorkbenchPage();
				if(page == null) return;
				IEditorPart editor = page.findEditor(ii);
				if(editor == null) return; else page.bringToTop(editor);
			}
			IJavaElement child = getElement(javaElement, p);
			if(child != null) {
				JavaUI.revealInEditor(JavaUI.openInEditor(javaElement), child);
			} else {
				JavaUI.openInEditor(javaElement);
			}
		}
    }
    
    private static IJavaElement getElement(IJavaElement javaElement, Properties p) {
    	if(p == null || !(javaElement instanceof IParent)) return null;
    	String childName = p.getProperty("property"); //$NON-NLS-1$
    	if(childName == null) return null;
    	IJavaElement[] cs = null;
    	try {
    		cs = ((IParent)javaElement).getChildren();
    	} catch (JavaModelException e) {
    		//ignore
    	}
    	if(cs != null) for (int i = 0; i < cs.length; i++) {
    		if(cs[i] instanceof IType) {
    			IType t = (IType)cs[i];
    			if(childName.length() > 0) {
    				String getter = "get" + childName.substring(0, 1).toUpperCase() + childName.substring(1); //$NON-NLS-1$
    				IMethod m = t.getMethod(getter, new String[0]);
    				if(m != null && m.exists()) return m;
    			}
    			IField f = t.getField(childName);
    			if(f != null && f.exists()) return f;
    		} else {
    			if(childName.equals(cs[i].getElementName())) return cs[i];
    		}
    	}
    	return null;
    }

	private static IWorkbenchPage getWorkbenchPage() {
		ModelPlugin plugin = ModelPlugin.getDefault();
		IWorkbench workbench = (plugin == null) ? null : plugin.getWorkbench();
		IWorkbenchWindow window = (workbench == null) ? null : workbench.getActiveWorkbenchWindow();
		return (window == null) ? null : window.getActivePage();
	}
	
}
