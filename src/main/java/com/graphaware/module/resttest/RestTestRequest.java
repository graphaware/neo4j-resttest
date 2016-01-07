/*
 * Copyright (c) 2013-2016 GraphAware
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


import com.fasterxml.jackson.annotation.JsonProperty;

public class RestTestRequest {

    private String cypher;
    private String node;
    @JsonProperty("node.property")
    private String nodeProperty;
    private String relationship;
    @JsonProperty("relationship.property")
    private String relationshipProperty;

    public String getCypher() {
        return cypher;
    }

    public void setCypher(String cypher) {
        this.cypher = cypher;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getNodeProperty() {
        return nodeProperty;
    }

    public void setNodeProperty(String nodeProperty) {
        this.nodeProperty = nodeProperty;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getRelationshipProperty() {
        return relationshipProperty;
    }

    public void setRelationshipProperty(String relationshipProperty) {
        this.relationshipProperty = relationshipProperty;
    }
}
