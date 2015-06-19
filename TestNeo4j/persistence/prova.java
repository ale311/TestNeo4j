package persistence;

import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class prova {

	public enum EmployeeRelationship implements RelationshipType {
		RIPORTA_A, AFFERISCE, SECONDO_LIVELLO
	}

	public void boh() {
		GraphDatabaseService graphDb = new EmbeddedGraphDatabase("var/graphdb",
				null, null);
		graphDb.shutdown();

		Transaction tx = graphDb.beginTx();
		try {

			// creazione dei centri di costo
			Node cc1 = graphDb.createNode();
			cc1.setProperty("Nome", "CC1");

			Node cc2 = graphDb.createNode();
			cc2.setProperty("Nome", "CC2");

			Node rossi = graphDb.createNode();
			rossi.setProperty("Nome", "Mario");
			rossi.setProperty("Cognome", "Rossi");

			Node verdi = graphDb.createNode();
			verdi.setProperty("Nome", "Giuseppe");
			verdi.setProperty("Cognome", "Verdi");

			Node bianchi = graphDb.createNode();
			bianchi.setProperty("Nome", "Carlo");
			bianchi.setProperty("Cognome", "Bianchi");

			// Sappiamo che c’è una posizione vacante
			// nell’organigramma. Quindi senza nome.
			Node posizioneVacante = graphDb.createNode();

			// Creiamo le relazioni
			rossi.createRelationshipTo(cc1, EmployeeRelationship.AFFERISCE);

			verdi.createRelationshipTo(cc1, EmployeeRelationship.AFFERISCE);
			verdi.createRelationshipTo(rossi, EmployeeRelationship.RIPORTA_A);

			// Bianchi afferisce al secondo centro di costo
			// solo dal 2011. Lo possiamo impostare
			Relationship bianchiCC2 = bianchi.createRelationshipTo(cc2,
					EmployeeRelationship.AFFERISCE);
			bianchiCC2.setProperty("anno", 2011);

			// Sappiamo che Bianchi riporterà ad
			// un manager non ancora designato
			bianchi.createRelationshipTo(posizioneVacante,
					EmployeeRelationship.RIPORTA_A);

			// Ma della posizione vacante sappiamo però anche
			// i centri di costo e il superiore gerarchico
			posizioneVacante.createRelationshipTo(rossi,
					EmployeeRelationship.RIPORTA_A);
			posizioneVacante.createRelationshipTo(cc2,
					EmployeeRelationship.AFFERISCE);

			tx.success();

		} finally {
			tx.finish();
		}

		Index<Node> employees = graphDb.index().forNodes("employees");
		Node bianchi = null;
		employees.add(bianchi, "codice", 100101);
		Node rossi = graphDb.index().forNodes("employees")
				.get("codice", 100101).getSingle();
		employees.add(rossi, "codice", 100102);
		Node verdi = null;
		employees.add(verdi, "codice", 100103);

		employees = graphDb.index().forNodes("employees");
		employees.add(bianchi, "Cognome", "Bianchi");
		employees.add(rossi, "Cognome", "Rossi");
		employees.add(verdi, "Cognome", "Verdi");
		for (Node n : graphDb.index().forNodes("employees")
				.get("Cognome", "Bianchi"))
			System.out.println("Trovato " + n.getProperty("Nome"));
	}

	public void boh2() {
		List<Node> managers = null;
		for (Node manager : managers)
			for (Node n : elencoSecondoLivello(manager))
				n.createRelationshipTo(manager,
						EmployeeRelationship.SECONDO_LIVELLO);

	}

	Iterable<Node> elencoSecondoLivello(final Node manager) {

		Traverser trv = manager.traverse(Traverser.Order.BREADTH_FIRST,

		// ci fermiamo ai nodi di secondo livello
				new StopEvaluator() {
					public boolean isStopNode(
							TraversalPosition traversalPosition) {
						return traversalPosition.depth() == 2;
					}
				},

				new ReturnableEvaluator() {
					public boolean isReturnableNode(
							TraversalPosition traversalPosition) {

						// vogliamo solo i nodi di secondo livello
						if (traversalPosition.depth() != 2)
							return false;

						// ma non vogliamo i nodi già collegati
						// direttamente al manager
						for (Relationship rel : traversalPosition.currentNode()
								.getRelationships(
										EmployeeRelationship.RIPORTA_A,
										Direction.OUTGOING)) {
							Node primo = null;
							if (rel.getEndNode() == primo)
								return false;
						}
						return true;
					}
				}, EmployeeRelationship.RIPORTA_A, Direction.INCOMING);

		return trv;
	}

	void trovaPerCentroDiCosto(Node cc) {

		// il metodo traverse crea un Traverser,
		// un iteratore che attraversa il grafo
		// in un modo preciso
		Traverser traverser = cc.traverse(

		// facciamo una ricerca Depth-First
				Traverser.Order.DEPTH_FIRST,

				// ci fermiamo al primo livello di profondità
				StopEvaluator.DEPTH_ONE,

				// vogliamo tutti i nodi tranne il primo...
				ReturnableEvaluator.ALL_BUT_START_NODE,

				// ...che abbiano la relazione AFFERISCE
				// con il nodo di partenza...
				EmployeeRelationship.AFFERISCE,

				// ...in entrata (i dipendenti afferiscono
				// ai centri di costo, non viceversa)
				Direction.INCOMING);

		// A questo punto possiamo iterare
		for (Node node : traverser) {
			if (node.hasProperty("Cognome"))
				System.out.println(node.getProperty("Cognome"));
		}
	}
}
