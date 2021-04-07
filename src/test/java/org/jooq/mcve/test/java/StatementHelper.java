package org.jooq.mcve.test.java;

import java.sql.PreparedStatement;

import org.jooq.impl.ProviderEnabledPreparedStatementHelper;

public class StatementHelper {

    public static String getSQL(PreparedStatement preparedStatement) {
        return ProviderEnabledPreparedStatementHelper.getDelegate(preparedStatement).toString();
    }

}
