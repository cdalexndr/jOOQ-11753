package org.jooq.mcve.test.java;

import static org.jooq.mcve.java.Tables.TEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.mcve.java.tables.records.TestRecord;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(AbstractTest.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    //fixes nano precision not equals
    private void assertEqualsTime(TemporalAccessor first, TemporalAccessor second) {
        assertEquals(FORMATTER.format(first), FORMATTER.format(second));
    }

    private static int getHoursTzOffset() {
        return (int) Duration.ofMillis(
                TimeZone.getDefault()
                        .getOffset(Instant.now().toEpochMilli()))
                .toHours();
    }

    @Before
    public void prepare() {
        ctx.deleteFrom(TEST).execute();
        assertNotEquals("Default timezone must not be UTC", getHoursTzOffset(), 0);
    }

    @Test
    //setting a java "Instant" to a "timestamp without timezone" db field
    public void testInsertInstantTimestampWithoutTz() {
        Instant now = Instant.now();
        Field<Instant> rawUpdateCol = DSL.field("update", SQLDataType.INSTANT);
        TestRecord result = ctx.insertInto(TEST)
                .columns(rawUpdateCol)
                .values(now)
                .returning(TEST.ID)
                .fetchOne();
        Instant read = ctx.select(rawUpdateCol)
                .from(TEST)
                .fetchOne(rawUpdateCol);
        assertEqualsTime(read, now);
    }

    @Test
    public void testBindTimestampWithTimezone() {
        LocalDateTime now = LocalDateTime.now();
        SelectConditionStep<Record1<Integer>> ltQuery = ctx.selectCount()
                .from(TEST)
                .where(TEST.UPDATE.lt(now));
        addQueryStartListener(executeContext -> {
            String sql = StatementHelper.getSQL(executeContext.statement());
            log.info("PgPreparedStatement: {}", sql);
            Pattern regex = Pattern.compile("cast\\('.*\\+" + String.format("%02d", getHoursTzOffset()));
            assertFalse("Found cast with timezone", regex.matcher(sql).find());
        });
        int count = ltQuery.fetch().map(r -> r.component1()).iterator().next();
    }
}
