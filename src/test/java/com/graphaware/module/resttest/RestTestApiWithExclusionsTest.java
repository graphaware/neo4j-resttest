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
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.test.util.TestUtils.jsonAsString;
import static com.graphaware.test.util.TestUtils.post;
import static org.junit.Assert.assertEquals;
import static org.neo4j.tooling.GlobalGraphOperations.at;
import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Integration test for {@link com.graphaware.module.resttest.RestTestApi}.
 */
public class RestTestApiWithExclusionsTest extends GraphAwareApiTest {

    private static final String FULL_QUERY = "CREATE (one:Person {name:'One', timestamp:12312312321})-[:FRIEND_OF {timestamp:32423423423}]->(two:Person {name:'Two', timestamp:32423524524}), (two)-[:EXCLUDED]->(three {name:'shouldBeIgnored'})";

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        new ExecutionEngine(database).execute(FULL_QUERY);
    }

    @Test
    public void shouldReturnOKWhenTestPasses() {
        post(getUrl() + "/assertSameGraph", jsonAsString("query-with-exclusions"), OK.value());
        post(getUrl()+ "/assertSubgraph", jsonAsString("subquery-with-exclusions"), OK.value());
    }

    @Test
    public void shouldReturn4xxWhenTestFails() {
        assertEquals("No corresponding relationship found to (:Person {name: One})-[:FRIEND_OF {key: value}]->(:Person {name: Two}) in existing database",
                post(getUrl() + "/assertSameGraph", jsonAsString("wrong-query-with-exclusions"), EXPECTATION_FAILED.value()));
        assertEquals("No corresponding relationship found to (:Person {name: One})-[:FRIEND_OF {key: value}]->(:Person {name: Two}) in existing database",
                post(getUrl() + "/assertSubgraph", jsonAsString("wrong-query-with-exclusions"), EXPECTATION_FAILED.value()));
    }

    @Test
    public void canClearDatabase() {
        post(getUrl() + "/clear", OK.value());

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(0, count(at(getDatabase()).getAllNodes()));
            tx.success();
        }
    }

    private String getUrl() {
        return baseUrl() + "/resttest/";
    }
}
