package com.ashimjk.data.processor.batch.syncup;

public class SyncUpQuery {

    public static final String SYNC_UP_SELECT_QUERY = """
            SELECT
                CUSTOMER_ID,
                SURNAME,
                C_SCORE,
                AGE,
                TENURE
            """;

    public static final String SYNC_UP_FROM_QUERY = """
            FROM (
                SELECT
                    CUSTOMER_ID,
                    SURNAME,
                    C_SCORE,
                    AGE,
                    TENURE
                FROM CUSTOMERS
                WHERE AGE > 30
                    AND TENURE >= 5
                    AND C_SCORE > 600
                OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            )
            """;

    public static final String SYNC_UP_COUNT_QUERY = """
            SELECT
                COUNT(*)
            FROM CUSTOMERS
            WHERE AGE > 30
                AND TENURE >= 5
                AND C_SCORE > 600
            """;
}
