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

import com.graphaware.runtime.RuntimeRegistry;
import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.proc.Procedures;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.graphaware.test.util.TestUtils.jsonAsString;
import static org.junit.Assert.assertEquals;

public class RestTestProcedureWithRuntimeTest extends GraphAwareIntegrationTest {

    private static final String FULL_QUERY = "CREATE (one:Person {name:'One'})-[:FRIEND_OF]->(two:Person {name:'Two'})";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        RuntimeRegistry.getStartedRuntime(getDatabase());
    }

    @Override
    protected String configFile() {
        return "test-neo4j.conf";
    }

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        database.execute(FULL_QUERY);
    }

    @Override
    protected void registerProcedures(Procedures procedures) throws Exception {
        procedures.registerProcedure(RestTestProcedure.class);
        procedures.registerFunction(RestTestProcedure.class);
    }

    @Test
    public void shouldReturnTrueWhenTestPasses() throws InterruptedException {
        Map<String, Object> map = new HashMap<>();
        map.put("value", jsonAsString("query"));

        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertSameGraph({value}) AS value", map);
            assertEquals(true, res.next().get("value"));
            tx.close();
        }


        map.put("value", jsonAsString("subquery"));
        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertSubgraph({value}) AS value", map);
            assertEquals(true, res.next().get("value"));
            tx.close();
        }
    }

    @Test
    public void shouldReturnFalseWhenTestFails() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", jsonAsString("wrong-query"));

        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertSameGraph({value}) AS value", map);
            assertEquals(false, res.next().get("value"));
            tx.close();
        }


        map.put("value", jsonAsString("wrong-query"));
        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertSubgraph({value}) AS value", map);
            assertEquals(false, res.next().get("value"));
            tx.close();
        }
    }

    @Test
    public void canClearDatabase() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", null);
        try (Transaction tx = getDatabase().beginTx()) {
            getDatabase().execute("CALL ga.resttest.clearDatabase()");
            Result res = getDatabase().execute("RETURN ga.resttest.assertEmpty({value}) AS value", map);
            assertEquals("The database is not empty, but assertEmpty return true", true, res.next().get("value"));
            tx.close();
        }
    }

    @Test
    public void databaseWithDataShouldNotBeEmpty() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", null);
        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertEmpty({value}) AS value", map);
            assertEquals("The database is not empty, but assertEmpty return true", false, res.next().get("value"));
            tx.close();
        }

        map = new HashMap<>();
        map.put("value", "{}");
        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertEmpty({value}) AS value", map);
            assertEquals("The database is not empty, but assertEmpty return true", false, res.next().get("value"));
            tx.close();
        }

        map = new HashMap<>();
        map.put("value", "{\"cypher\":\"\"}");
        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertEmpty({value}) AS value", map);
            assertEquals("The database is not empty, but assertEmpty return true", false, res.next().get("value"));
            tx.close();
        }
        map = new HashMap<>();
        map.put("value", "{\"cypher\": null}");
        try (Transaction tx = getDatabase().beginTx()) {
            Result res = getDatabase().execute("RETURN ga.resttest.assertEmpty({value}) AS value", map);
            assertEquals("The database is not empty, but assertEmpty return true", false, res.next().get("value"));
            tx.close();
        }
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

    private void testResult(String call, Map<String, Object> params, Consumer<Result> resultConsumer) {
        try (Transaction tx = getDatabase().beginTx()) {
            Map<String, Object> p = (params == null) ? Collections.<String, Object>emptyMap() : params;
            resultConsumer.accept(getDatabase().execute(call, p));
            tx.close();
        }
    }


}
