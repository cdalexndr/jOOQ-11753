package org.jooq.impl;

import java.sql.PreparedStatement;

public class ProviderEnabledPreparedStatementHelper {

    public static PreparedStatement getDelegate(PreparedStatement stmt) {
        if (stmt instanceof ProviderEnabledPreparedStatement) {
            return ((ProviderEnabledPreparedStatement) stmt).getDelegate();
        }
        return stmt;
    }
}
