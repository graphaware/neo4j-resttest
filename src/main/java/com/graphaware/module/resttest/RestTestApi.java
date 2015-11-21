/*
 * Copyright (c) 2013-2015 GraphAware
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

package com.graphaware.module.resttest;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.common.policy.spel.SpelNodeInclusionPolicy;
import com.graphaware.common.policy.spel.SpelNodePropertyInclusionPolicy;
import com.graphaware.common.policy.spel.SpelRelationshipInclusionPolicy;
import com.graphaware.common.policy.spel.SpelRelationshipPropertyInclusionPolicy;
import com.graphaware.runtime.RuntimeRegistry;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;
import com.graphaware.test.unit.GraphUnit;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * REST API for {@link com.graphaware.test.unit.GraphUnit}.
 */
@Controller
@RequestMapping("/resttest")
public class RestTestApi {

    private final GraphDatabaseService database;

    @Autowired
    public RestTestApi(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void clearDatabase() {
        try (Transaction tx = database.beginTx()) {
            GraphUnit.clearGraph(database);
            tx.success();
        }
    }

    @RequestMapping(value = "/assertSameGraph", method = RequestMethod.POST)
    @ResponseBody
    public String assertSameGraph(@RequestBody RestTestRequest request, HttpServletResponse response) throws IOException {
        if (request.getCypher() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cypher statement must be provided");
        }

        try {
            GraphUnit.assertSameGraph(database, request.getCypher(), resolveInclusionPolicies(request));
            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        } catch (AssertionError error) {
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            return error.getMessage();
        }
    }

    @RequestMapping(value = "/assertSubgraph", method = RequestMethod.POST)
    @ResponseBody
    public String assertSubgraph(@RequestBody RestTestRequest request, HttpServletResponse response) throws IOException {
        if (request.getCypher() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cypher statement must be provided");
        }

        try {
            GraphUnit.assertSubgraph(database, request.getCypher(), resolveInclusionPolicies(request));
            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        } catch (AssertionError error) {
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            return error.getMessage();
        }
    }

    @RequestMapping(value = "/assertEmpty", method = RequestMethod.POST)
    @ResponseBody
    public String assertEmpty(@RequestBody(required = false) RestTestRequest request, HttpServletResponse response) throws IOException {
        try {
            GraphUnit.assertEmpty(database, resolveInclusionPolicies(request));
            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        } catch (AssertionError error) {
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            return error.getMessage();
        }
    }

    private InclusionPolicies resolveInclusionPolicies(RestTestRequest request) {
        InclusionPolicies policies;
        if (RuntimeRegistry.getRuntime(database) == null) {
            policies = InclusionPolicies.all();
        } else {
            policies = InclusionPoliciesFactory.allBusiness();
        }

        if (request == null) {
            return policies;
        }

        if (request.getNode() != null) {
            policies = policies.with(new SpelNodeInclusionPolicy(request.getNode()));
        }

        if (request.getRelationship() != null) {
            policies = policies.with(new SpelRelationshipInclusionPolicy(request.getRelationship()));
        }

        if (request.getNodeProperty() != null) {
            policies = policies.with(new SpelNodePropertyInclusionPolicy(request.getNodeProperty()));
        }

        if (request.getRelationshipProperty() != null) {
            policies = policies.with(new SpelRelationshipPropertyInclusionPolicy(request.getRelationshipProperty()));
        }

        return policies;
    }
}
