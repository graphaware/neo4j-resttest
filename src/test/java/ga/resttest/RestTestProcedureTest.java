/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package ga.resttest;

import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.proc.Procedures;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.test.util.TestUtils.jsonAsString;
import static org.junit.Assert.*;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RestTestProcedureTest extends EmbeddedDatabaseIntegrationTest {

    @Override
    protected void registerProcedures(Procedures procedures) throws Exception {
        super.registerProcedures(procedures);
        procedures.registerProcedure(RestTestProcedure.class);
        procedures.registerFunction(RestTestProcedure.class);
    }

    @Test
    public void clearDatabase() {
        try (Transaction tx = getDatabase().beginTx()) {

            getDatabase().execute("CALL ga.resttest.clearDatabase()");
            getDatabase().execute("CREATE (n:TestNode)");
            assertEquals(1, count(getDatabase().getAllNodes()));

            getDatabase().execute("CALL ga.resttest.clearDatabase()");
            assertEquals(0, count(getDatabase().getAllNodes()));

            tx.close();
        }
    }

    @Test
    public void databaseWithDataShouldNotBeEmpty() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", null);
        testResult("RETURN ga.resttest.assertEmpty({value}) AS value", map, result -> assertEquals("The database is not empty, there are nodes", true, result.next().get("value")));

        map = new HashMap<>();
        map.put("value", "{}");
        testResult("RETURN ga.resttest.assertEmpty({value}) AS value", map, result -> assertEquals("The database is not empty, there are nodes", true, result.next().get("value")));


        map = new HashMap<>();
        map.put("value", "{\"cypher\":\"\"}");
        testResult("RETURN ga.resttest.assertEmpty({value}) AS value", map, result -> assertEquals("The database is not empty, there are nodes", true, result.next().get("value")));

        map = new HashMap<>();
        map.put("value", "{\"cypher\": null}");
        testResult("RETURN ga.resttest.assertEmpty({value}) AS value", map, result -> assertEquals("The database is not empty, there are nodes", true, result.next().get("value")));
    }


    @Test
    public void emptyDbShouldPassEmptyTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", null);
        getDatabase().execute("CALL ga.resttest.clearDatabase()");
        testResult("RETURN ga.resttest.assertEmpty({value}) AS value", map, result -> assertEquals("The database is not empty, there are nodes", true, result.next().get("value")));
    }

    @Test
    public void nonEmptyDbShouldPassEmptyTestWithExclusions() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", jsonAsString("empty-with-exclusions"));
        getDatabase().execute("CREATE (n:Person)");

        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertEmpty({value}) AS value", map);
            assertEquals("The database contains other nodes than Person", true, res.next().get("value"));
            tx.close();
        }
    }

    @Test
    public void nonEmptyDbShouldFailedEmptyTestWithExclusions() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", jsonAsString("empty-with-exclusions"));
        getDatabase().execute("CREATE (n:NotPerson)");

        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertEmpty({value}) AS value", map);
            assertEquals("The database contains other nodes than Person", false, res.next().get("value"));
            tx.close();
        }
    }

    private void testResult(String call, Map<String, Object> params, Consumer<Result> resultConsumer) {
        try (Transaction tx = getDatabase().beginTx()) {
            Map<String, Object> p = (params == null) ? Collections.<String, Object>emptyMap() : params;
            resultConsumer.accept(getDatabase().execute(call, p));
            tx.close();
        }
    }


}
