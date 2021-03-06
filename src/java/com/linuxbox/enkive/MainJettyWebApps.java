/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.linuxbox.enkive;

import org.springframework.context.ApplicationContext;

public class MainJettyWebApps extends MainConsole {
	static final String[] CONFIG_FILES = { "jetty-server-webapps.xml" };
	static final String DESCRIPTION = "com.linuxbox.enkive.MainJettyWebApps";

	public MainJettyWebApps(String[] arguments) {
		super(arguments, CONFIG_FILES, DESCRIPTION);
	}

	@Override
	protected void postStartup() {
		super.postStartup();
		out.println("Jetty will be starting...");
	}

	protected void runCoreFunctionality(ApplicationContext context) {
//		Server server = context.getBean("Server",
//				org.eclipse.jetty.server.Server.class);

		super.runCoreFunctionality(context);

//		try {
//			server.stop();
//			System.exit(0);
//		} catch (Exception e) {
//			LOGGER.error("Error force stopping Jetty server.", e);
//			System.exit(1);
//		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Main main = new MainJettyWebApps(args);
		main.run();
	}
}
