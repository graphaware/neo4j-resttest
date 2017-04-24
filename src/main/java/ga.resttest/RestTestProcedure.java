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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.inclusion.spel.SpelNodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.spel.SpelNodePropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.spel.SpelRelationshipInclusionPolicy;
import com.graphaware.common.policy.inclusion.spel.SpelRelationshipPropertyInclusionPolicy;
import com.graphaware.module.resttest.RestTestRequest;
import com.graphaware.runtime.RuntimeRegistry;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;
import com.graphaware.test.unit.GraphUnit;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;

public class RestTestProcedure {

    private static final Log LOG = LoggerFactory.getLogger(RestTestProcedure.class);

    @Context
    public GraphDatabaseService database;

    @Procedure(value = "ga.resttest.clearDatabase",mode = Mode.WRITE)
    @Description("Clear the database content.")
    public void clearDatabase() {
            GraphUnit.clearGraph(database);
    }

    @UserFunction(value = "ga.resttest.assertSameGraph")
    @Description("ga.resttest.assertSameGraph(inclusionPolicy) - assert the state of the database.")
    public boolean assertSameGraph(@Name("inclusionPolicy") String param) {
        RestTestRequest request = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            if(param!=null && !param.isEmpty()) {
                request = mapper.readValue(param, RestTestRequest.class);
            }

            if (request.getCypher() == null) {
                throw new RuntimeException( "Cypher statement must be provided");
            }

            return GraphUnit.areSameGraph(database, request.getCypher(), resolveInclusionPolicies(request));

        } catch (IOException e) {
            throw new RuntimeException( e);
        }
    }

    @UserFunction
    @Description("ga.resttest.assertSubgraph(inclusionPolicy) - assert the state of the database.")
    public boolean assertSubgraph(@Name("inclusionPolicy") String param) {
        RestTestRequest request = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            if(param!=null && !param.isEmpty()) {
                request = mapper.readValue(param, RestTestRequest.class);
            }

            if (request.getCypher() == null) {
                throw new RuntimeException( "Cypher statement must be provided");
            }

            return GraphUnit.isSubgraph(database, request.getCypher(), resolveInclusionPolicies(request));
        } catch (IOException e) {
            throw new RuntimeException( e);
        }
    }

    @UserFunction
    @Description("ga.resttest.assertEmpty(inclusionPolicy) - returns a true if database is empty, false if not")
    public boolean assertEmpty(@Name("inclusionPolicy") String param) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            RestTestRequest request = null;
            if (param != null && !param.isEmpty()) {
                request = mapper.readValue(param, RestTestRequest.class);
            }

            return GraphUnit.isEmpty(database, resolveInclusionPolicies(request));
        } catch (IOException error) {
            return false;
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