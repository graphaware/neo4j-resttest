GraphAware Neo4j RestTest
=========================

[![Build Status](https://travis-ci.org/graphaware/neo4j-resttest.png)](https://travis-ci.org/graphaware/neo4j-resttest) | <a href="http://graphaware.com/downloads/" target="_blank">Downloads</a> | Latest Release: 2.2.2.31.13

GraphAware RestTest is a simple library for testing code that talks to Neo4j running in <a href="http://docs.neo4j.org/chunked/stable/server-installation.html" target="_blank">standalone server</a> mode.

Getting the Software
--------------------

You will need the <a href="https://github.com/graphaware/neo4j-framework" target="_blank">GraphAware Neo4j Framework</a> and GraphAware Neo4j RestTest .jar files (both of which you can <a href="http://graphaware.com/downloads/" target="_blank">download here</a>) dropped
into the `plugins` directory of your Neo4j installation. After Neo4j restart, you will be able to use the RestTest APIs.

Releases are synced to <a href="http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22resttest%22" target="_blank">Maven Central repository</a>.

#### Note on Versioning Scheme

The version number has two parts. The first four numbers indicate compatibility with Neo4j GraphAware Framework.
 The last number is the version of the RestTest library. For example, version 2.1.2.7.2 is version 2 of RestTest
 compatible with GraphAware Neo4j Framework 2.1.2.7.

Using GraphAware RestTest
-------------------------

RestTest allows you to assert the state of the database running in server mode and to clear it. It is in a sense equivalent
to using <a href="http://graphaware.com/neo4j/2014/05/29/graph-unit-neo4j-unit-testing.html" target="_blank">GraphUnit</a> in
embedded mode.

### REST API

When deployed in server mode, there are three URLs that you can issue POST requests to:
* `http://your-server-address:7474/graphaware/resttest/clear` to clear your database. No body required.
* `http://your-server-address:7474/graphaware/resttest/assertSameGraph` to assert the state of the database. You need to provide a body described shortly.
* `http://your-server-address:7474/graphaware/resttest/assertSubgraph` to assert the state of the database. You need to provide a body described shortly.
* `http://your-server-address:7474/graphaware/resttest/assertEmpty` to assert the database is empty. You need to provide a body described shortly.

The body where required needs to provide a Cypher CREATE statement, representing the state of the database being asserted,
for example:

```json
{
    "cypher": "CREATE (one:Person {name:'One'})-[:FRIEND_OF]->(two:Person {name:'Two'})"
}
```

The second API call is used to verify that the graph in the database is exactly the same as the graph created by the Cypher
CREATE statement provided in the body of the request. This means that the nodes, their properties and labels, relationships,
and their properties and labels must be exactly the same. Note that Neo4j internal node/relationship IDs are ignored.
In case the graphs aren't identical, the assertion fails and you will get a response with EXPECTATION_FAILED (417) status code.
If the test passes, you will get an OK (200).

It is possible to use expressions to include/exclude certain nodes, relationships, and properties thereof from the comparisons.
For example, for the purposes of comparison, if we only wanted to include nodes labelled `Person`, relationships with type `FRIEND_OF`, and ignore any
`timestamp` properties on both nodes and relationships, the body of the POST request would look like this:

```json
{
    "cypher": "CREATE (one:Person {name:'One'})-[:FRIEND_OF]->(two:Person {name:'Two'})",
    "node":"hasLabel('Person')",
    "relationship":"isType('FRIEND_OF')",
    "node.property":"key != 'timestamp'",
    "relationship.property":"key != 'timestamp'"
}
```

The third API call is used to verify that the graph created by provided Cypher statement is a subgraph of the graph in the database.
Request body options and response codes are same as above.

Finally, the API call that ensures that the database is empty has the body as an optional requirement and, of course,
no Cypher is required.

License
-------

Copyright (c) 2014 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
