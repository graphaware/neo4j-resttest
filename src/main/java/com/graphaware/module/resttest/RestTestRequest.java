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
