package prova;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

import scala.annotation.meta.param;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Track;
import de.umass.lastfm.User;

public class JavaAPIExample{
	
	private static final String username = "ale_311";
	private static final String apiKey ="95f57bc8e14bd2eee7f1df8595291493";
	private static final String DB_PATH = "util/neo4j-community-2.2.3/data/graph.db";
	
	public enum MieRelazioni implements RelationshipType{
		ASCOLTA, COMPONE, AMICO, VICINO, TAGGED;
	}
	public static void main(String[] args) throws IOException {
		System.out.println( "Starting database ..." );
		FileUtils.deleteRecursively( new File( DB_PATH ) );
		
		HashSet<Track> insiemeTracce = new HashSet<Track>();
 
		// START SNIPPET: startDb 
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		
		
		ResourceIterator<Node> resultIterator = null;
		
		try ( Transaction tx = graphDb.beginTx() ){
		Label labelUtente = DynamicLabel.label("Utente");
		Label labelTraccia = DynamicLabel.label("Traccia");
		Label labelArtista = DynamicLabel.label("Artista");
		String queryString ="";
		//String queryString = "MERGE (u: Utente {Utente: {username}}) RETURN u";
		Map<String,Object> parameters = new HashMap<String, Object>();
		//parameters.put("Utente", username);
		//resultIterator = graphDb.execute(queryString, parameters).columnAs("u");
		//System.out.println( "Users created" );
		
		
		

//		try ( ResourceIterator<Node> users = graphDb.findNodes( labelUtente, "username", "ale_311" ) ){
//			
//			while ( users.hasNext() ){
//				userNodes.add( users.next() );
//			}
//			if (userNodes.size()!=0)
//				System.out.println( "The username of user  is " + userNodes.get(0).getProperty("username") );
//			else
//				System.out.println("utente non trovato");
//		}
		
		
		//Accedo a last per estrarre gli amici dell'utente selezionato
		PaginatedResult<User> amici = User.getFriends(username, apiKey);
		//costruisco relazione tra vicini e utente
		for(User u : amici){
			String friendName = u.getName();
			
			queryString = "merge(u1:Utente{Utente:{username}})"+
							"merge(u2:Utente{Utente:{vicino}})"+
							"merge(u1)-[:AMICO]-(u2)";
			parameters.put("username", username);
			parameters.put("vicino", friendName);
			resultIterator = graphDb.execute(queryString, parameters).columnAs("utente e suoi amici");
		
		}
		parameters.clear();
		//scorro la collezione dei vicini e li collego alle tracce ascoltate da loro
		for(User u : amici){
			String friendName = u.getName();
			PaginatedResult<Track> ascoltiDeiVicini = User.getRecentTracks(friendName, 1, 50, apiKey);
			for (Track tracciaCorrente : ascoltiDeiVicini){
				//inserisco la traccia nel mio SET
				insiemeTracce.add(tracciaCorrente);
				//collego l'utente alla traccia
				String nomeTraccia = tracciaCorrente.getName();
				queryString = "merge(u:Utente{Utente:{username}})"+
								"merge(t:Traccia{Traccia:{Traccia}})"+	
								"merge(u)-[:ASCOLTA]-(t)";
				parameters.put("username", friendName);
				parameters.put("Traccia", nomeTraccia);
				resultIterator = graphDb.execute(queryString, parameters).columnAs("utente ascolta tracce");
			}
			
		}
		parameters.clear();
		
		//Accedo a lastfm per estrarre i max 200 ascolti dell'utente selezionato
		PaginatedResult<Track> insieme_ascolti = User.getRecentTracks(username, 1, 50, apiKey);
		for (Track tracciaCorrente : insieme_ascolti){
			//inserisco le tracce ascoltate nel mio SET
			insiemeTracce.add(tracciaCorrente);
			String nomeTraccia = tracciaCorrente.getName();
			queryString = "merge(u:Utente{Utente:{username}})"+
							"merge(t:Traccia{Traccia:{Traccia}})"+	
							"merge(u)-[:ASCOLTA]-(t)";
			parameters.put("username", username);
			parameters.put("Traccia", nomeTraccia);
			resultIterator = graphDb.execute(queryString, parameters).columnAs("utente ascolta tracce");
		}
		parameters.clear();
		
		
		//scansiono la collezione di tracce per associare la relazione COMPONE
		//con Artista, presa sempre da LAST
		for (Track tracciaCorrente : insiemeTracce){
			
			String nomeTraccia = tracciaCorrente.getName();
			System.out.println(nomeTraccia);
			String artistaTraccia = tracciaCorrente.getArtist();
			System.out.println(artistaTraccia);
			String mbidSTRING = tracciaCorrente.getMbid();
			System.out.println(mbidSTRING);
			
			queryString = "merge(t:Traccia{Traccia:{Traccia}}) "+ 
							"merge(a:Artista{Artista:{Artista}})"+
							"merge(a)-[:COMPONE]-(t)";
			parameters.put("Traccia", nomeTraccia);
			parameters.put("Artista", artistaTraccia);
			resultIterator = graphDb.execute(queryString, parameters).columnAs("artista compone tracce");
			
			
//			queryString = "merge(t:Traccia{Traccia:{Traccia}}) return t";
//			parameters.put("Traccia", nomeTraccia);
//			resultIterator = graphDb.execute(queryString, parameters).columnAs("traccia");
//			
//			queryString = "merge(a:Artista{Artista:{Artista}}) return a";
//			parameters.put("Artista", artistaTraccia);
//			resultIterator = graphDb.execute(queryString, parameters).columnAs("artista");
//			
//			queryString = "merge(a)-[:COMPONE]-(t)";
//			resultIterator = graphDb.execute(queryString).columnAs("artistaaaaa");
//			String queryStringTraccia = "MERGE (t: Traccia{Traccia:{Traccia}}) return t";
//			Map<String, Object>parametersTraccia = new HashMap<String, Object>();
//			parametersTraccia.put("Traccia", nomeTraccia);
//			resultIterator = graphDb.execute(queryStringTraccia, parameters).columnAs("t");
//			String queryStringArtista = "MERGE (a:Artista{Artista:{artista}}) return a";
//			Map<String, Object>parametersArtista = new HashMap<String, Object>();
//			parametersArtista.put("Artista", artistaTraccia);
//			resultIterator = graphDb.execute(queryStringArtista, parameters).columnAs("a");
		}
			
		
			
		
//		Relationship relationship = userNodes.get(0).createRelationshipTo(userNodes.get(0), MieRelazioni.ASCOLTA);
//		relationship.setProperty("review", "ciao");

		tx.success();
		}
		
	}
}