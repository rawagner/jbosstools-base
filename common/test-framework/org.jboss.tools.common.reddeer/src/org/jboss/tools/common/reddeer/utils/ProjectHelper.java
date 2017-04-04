/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.common.reddeer.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.reddeer.common.wait.TimePeriod;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.eclipse.core.resources.Project;
import org.jboss.reddeer.eclipse.ui.dialogs.PropertyDialog;
import org.jboss.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.jboss.reddeer.swt.api.TreeItem;
import org.jboss.reddeer.swt.condition.ControlIsEnabled;
import org.jboss.reddeer.swt.condition.ShellIsAvailable;
import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.tab.DefaultTabItem;
import org.jboss.reddeer.swt.impl.tree.DefaultTree;
import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
import org.jboss.reddeer.workbench.core.condition.JobIsRunning;

public class ProjectHelper {
	
	/**
	 * Add specified libraries to project build path
	 * @param projectName name of project to which libraries are added
	 * @param libraryPathMap Map of library path and library name which are to be added to build path.
	 * Library path should end with File.separator 
	 */
	public static void addLibrariesIntoProject(String projectName, Map<String, String> libraryPathMap) {
		
		for(String lib: libraryPathMap.keySet()){
			FileUtils.copyFileIntoProjectFolder(projectName, new File(libraryPathMap.get(lib)+lib));
		}
		
		ProjectExplorer pe = new ProjectExplorer();
		pe.open();
		Project p = pe.getProject(projectName);
		p.select();
		new ContextMenu("Refresh").select();
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		PropertyDialog pd = p.openProperties();
		new DefaultShell("Properties for "+projectName);
		new DefaultTreeItem("Java Build Path").select();
		new DefaultTabItem("Libraries").activate();
		
		new PushButton("Add JARs...").click();
		new DefaultShell("JAR Selection");
		List<TreeItem> librariesToAdd = new ArrayList<TreeItem>();
		for (String library : libraryPathMap.keySet()) {
			librariesToAdd.add(new DefaultTreeItem(projectName, library));
		}
		new DefaultTree().selectItems(librariesToAdd.toArray(new TreeItem[librariesToAdd.size()]));
		new WaitUntil(new ControlIsEnabled(new PushButton("OK")));
		new PushButton("OK").click();
		new WaitWhile(new ShellIsAvailable("JAR Selection"));
		pd.ok();
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);

	}

}
