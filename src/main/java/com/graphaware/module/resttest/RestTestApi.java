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

import com.graphaware.test.unit.GraphUnit;
import org.apache.commons.lang.CharEncoding;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static com.graphaware.common.util.PropertyContainerUtils.deleteNodeAndRelationships;

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
    @Transactional
    public void clearDatabase() {
        for (Node node : GlobalGraphOperations.at(database).getAllNodes()) {
            deleteNodeAndRelationships(node);
        }
    }

    @RequestMapping(value = "/assertSameGraph", method = RequestMethod.POST)
    @ResponseBody
    public String assertSameGraph(@RequestBody String cypher, HttpServletResponse response) throws UnsupportedEncodingException {
        try {
            GraphUnit.assertSameGraph(database, URLDecoder.decode(cypher, CharEncoding.UTF_8));
            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        } catch (AssertionError error) {
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            return error.getMessage();
        }
    }

    @RequestMapping(value = "/assertSubgraph", method = RequestMethod.POST)
    @ResponseBody
    public String assertSubgraph(@RequestBody String cypher, HttpServletResponse response) throws UnsupportedEncodingException {
        try {
            GraphUnit.assertSubgraph(database, URLDecoder.decode(cypher, CharEncoding.UTF_8));
            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        } catch (AssertionError error) {
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            return error.getMessage();
        }
    }
}
