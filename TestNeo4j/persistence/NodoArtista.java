package persistence;

import org.neo4j.graphdb.Node;

public class NodoArtista {
    static final String NAME = "nomeArtistico";
    

    // START SNIPPET: the-node
    private final Node underlyingNode;

    NodoArtista( Node nodoArtista ) {
        this.underlyingNode = nodoArtista;
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
        return o instanceof NodoArtista &&
                underlyingNode.equals( ( (NodoArtista)o ).getUnderlyingNode() );
    }

    @Override
    public String toString() {
        return "NodoArtista[" + getName() + "]";
    }


}