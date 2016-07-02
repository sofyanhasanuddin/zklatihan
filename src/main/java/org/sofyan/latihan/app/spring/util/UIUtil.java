package org.sofyan.latihan.app.spring.util;

import org.zkoss.zk.ui.Executions;

public class UIUtil {
	
	public static Object getArg(String argName) {
		return Executions.getCurrent().getArg().get( argName );
	}
	
	public static Object getAttribute(String attrName) {
		return Executions.getCurrent().getAttribute( attrName );
	}

}
