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
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.util.Collections;
import java.util.Map;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.test.util.TestUtils.jsonAsString;
import static org.junit.Assert.assertEquals;
import static org.neo4j.tooling.GlobalGraphOperations.at;
import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Integration test for {@link RestTestApi}.
 */
public class RestTestApiTest extends GraphAwareApiTest {

    private static final String FULL_QUERY = "CREATE (one:Person {name:'One'})-[:FRIEND_OF]->(two:Person {name:'Two'})";
    private static final Map<String, String> DEFAULT_HEADERS = Collections.singletonMap("Content-Type", "application/json");

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        database.execute(FULL_QUERY);
    }

    @Test
    public void shouldReturnOKWhenTestPasses() {
        httpClient.post(getUrl() + "assertSameGraph", jsonAsString("query"), OK.value());
        httpClient.post(getUrl() + "assertSubgraph", jsonAsString("subquery"), OK.value());
    }

    @Test
    public void shouldReturn4xxWhenTestFails() {
        assertEquals("No corresponding relationship found to (:Person {name: One})-[:FRIEND_OF {key: value}]->(:Person {name: Two}) in existing database",
                httpClient.post(getUrl() + "assertSameGraph", jsonAsString("wrong-query"), EXPECTATION_FAILED.value()));
        assertEquals("No corresponding relationship found to (:Person {name: One})-[:FRIEND_OF {key: value}]->(:Person {name: Two}) in existing database",
                httpClient.post(getUrl() + "assertSubgraph", jsonAsString("wrong-query"), EXPECTATION_FAILED.value()));
    }

    @Test
    public void canClearDatabase() {
        httpClient.post(getUrl() + "clear", null, DEFAULT_HEADERS, OK.value());

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(0, count(at(getDatabase()).getAllNodes()));
            tx.success();
        }
    }

    @Test
    public void databaseWithDataShouldNotBeEmpty() {
        assertEquals("The database is not empty, there are nodes",
                httpClient.post(getUrl() + "assertEmpty", null, DEFAULT_HEADERS, EXPECTATION_FAILED.value()));

        assertEquals("The database is not empty, there are nodes",
                httpClient.post(getUrl() + "assertEmpty", "{}", null, DEFAULT_HEADERS, EXPECTATION_FAILED.value()));

        assertEquals("The database is not empty, there are nodes",
                httpClient.post(getUrl() + "assertEmpty", "{\"cypher\":\"\"}", null, DEFAULT_HEADERS, EXPECTATION_FAILED.value()));

        assertEquals("The database is not empty, there are nodes",
                httpClient.post(getUrl() + "assertEmpty", "{\"cypher\": null}", null, DEFAULT_HEADERS, EXPECTATION_FAILED.value()));
    }

    @Test
    public void emptyDbShouldPassEmptyTest() {
        httpClient.post(getUrl() + "clear", null, DEFAULT_HEADERS, OK.value());
        httpClient.post(getUrl() + "assertEmpty", null, DEFAULT_HEADERS, OK.value());
    }

    @Test
    public void nonEmptyDbShouldPassEmptyTestWithExclusions() {
        httpClient.post(getUrl() + "assertEmpty", jsonAsString("empty-with-exclusions"), OK.value());
    }

    private String getUrl() {
        return baseUrl() + "/resttest/";
    }
}
