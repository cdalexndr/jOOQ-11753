package org.jooq.mcve.test.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractTest {

    public Connection connection;
    public DSLContext ctx;

    private static class TestExecuteListener extends DefaultExecuteListener {

        public static List<Consumer<ExecuteContext>> listeners = new ArrayList<>();

        @Override
        public void executeStart(ExecuteContext executeContext) {
            listeners.forEach(l -> l.accept(executeContext));
        }
    }

    public static void addQueryStartListener(Consumer<ExecuteContext> listener) {
        TestExecuteListener.listeners.add(listener);
    }

    @Before
    public void setup() throws Exception {
        connection = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/postgres?currentSchema=mcve", "postgres", "postgres");
        Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
        configuration.set(new DefaultExecuteListenerProvider(new TestExecuteListener()));
        ctx = DSL.using(configuration);
    }

    @After
    public void after() throws Exception {
        ctx = null;
        connection.close();
        connection = null;
    }
}
