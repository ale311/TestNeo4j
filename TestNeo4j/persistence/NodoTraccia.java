package persistence;

import org.neo4j.graphdb.Node;

public class NodoTraccia {
    static final String NAME = "name";

    // START SNIPPET: the-node
    private final Node underlyingNode;

    NodoTraccia( Node nodoTraccia ) {
        this.underlyingNode = nodoTraccia;
    }

    protected Node getUnderlyingNode() {
        return underlyingNode;
    }

    // END SNIPPET: the-node
    
    // START SNIPPET: delegate-to-the-node
    public String getName() {
        return (String)underlyingNode.getProperty( NAME );
    }

    // END SNIPPET: delegate-to-the-node

    // START SNIPPET: override
    @Override
    public int hashCode() {
        return underlyingNode.hashCode();
    }

    @Override
    public boolean equals( Object o ) {
        return o instanceof NodoTraccia &&
                underlyingNode.equals( ( (NodoTraccia)o ).getUnderlyingNode() );
    }

    @Override
    public String toString() {
        return "NodoTraccia[" + getName() + "]";
    }


}