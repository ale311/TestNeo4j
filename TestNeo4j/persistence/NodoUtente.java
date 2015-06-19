package persistence;

import org.neo4j.graphdb.Node;

public class NodoUtente {
    static final String NAME = "name";

    // START SNIPPET: the-node
    private final Node underlyingNode;

    NodoUtente( Node nodoConcerto ) {
        this.underlyingNode = nodoConcerto;
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
        return o instanceof NodoUtente &&
                underlyingNode.equals( ( (NodoUtente)o ).getUnderlyingNode() );
    }

    @Override
    public String toString() {
        return "NodoUtente[" + getName() + "]";
    }


}