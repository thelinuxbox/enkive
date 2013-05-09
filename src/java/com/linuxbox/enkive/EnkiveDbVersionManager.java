package com.linuxbox.enkive;

import com.linuxbox.util.Version;
import com.linuxbox.util.dbmigration.DbVersionManager;

public class EnkiveDbVersionManager extends DbVersionManager {
	public EnkiveDbVersionManager() throws DbVersionManagerException {
		super();
		associateHelp(ProductInfo.V1_2P, 0);
		associateHelp(ProductInfo.V1_3A, 1);
	}

	protected void associateHelp(Version swVersion, int dbVersion)
			throws DbVersionManagerException {
		DbVersion dbv = new DbVersion(dbVersion);
		associate(swVersion, dbv);
	}
}