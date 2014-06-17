/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.module.resttest;

import com.graphaware.test.integration.GraphAwareApiTest;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.test.util.TestUtils.post;
import static org.junit.Assert.assertEquals;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Integration test for {@link RestTestApi}.
 */
public class RestTestApiTest extends GraphAwareApiTest {

    private static final String FULL_QUERY = "CREATE (one:Person {name:'One'})-[:FRIEND_OF]->(two:Person {name:'Two'})";
    private static final String SUB_QUERY = "CREATE (one:Person {name:'One'})";
    private static final String WRONG_QUERY = "CREATE (one:Person {name:'One'})-[:FRIEND_OF {key:'value'}]->(two:Person {name:'Two'})";

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        new ExecutionEngine(database).execute(FULL_QUERY);
    }

    @Test
    public void shouldReturnOKWhenTestPasses() {
        post(getUrl() + "/assertSameGraph", FULL_QUERY, HttpStatus.SC_OK);
        post(getUrl()+ "/assertSubgraph", SUB_QUERY, HttpStatus.SC_OK);
    }

    @Test
    public void shouldReturn4xxWhenTestFails() {
        assertEquals("No corresponding relationship found to: (:Person {name: One})-[:FRIEND_OF {key: value}]->(:Person {name: Two})", post(getUrl() + "/assertSameGraph", WRONG_QUERY, HttpStatus.SC_EXPECTATION_FAILED));
        assertEquals("No corresponding relationship found to: (:Person {name: One})-[:FRIEND_OF {key: value}]->(:Person {name: Two})", post(getUrl() + "/assertSubgraph", WRONG_QUERY, HttpStatus.SC_EXPECTATION_FAILED));
    }

    @Test
    public void canClearDatabase() {
        post(getUrl() + "/clear", HttpStatus.SC_OK);

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(0, count(at(getDatabase()).getAllNodes()));
            tx.success();
        }
    }

    private String getUrl() {
        return baseUrl() + "/resttest/";
    }
}
