import java.util.*;
import java.io.*;

public class RoadTrip {
	
	private class Graph {
		// Adjacency Matrix implementation of weighted, undirected graph.
		private int adjMatrix[][];
		private int numVertices;

		// Initialize the matrix
		public Graph(int numVertices) {
			this.numVertices = numVertices;
		    adjMatrix = new int[numVertices][numVertices];
		 }

		// Add edges
		public void addEdge(int i, int j, int weight) {
		    adjMatrix[i][j] = weight;
		    adjMatrix[j][i] = weight;
		}

//		// Remove edges (currently not needed)
//		public void removeEdge(int i, int j) {
//		    adjMatrix[i][j] = 0;
//		    adjMatrix[j][i] = 0;
//		}

		// Print the matrix
		public String toString() {
		    StringBuilder s = new StringBuilder();
		    for (int i = 0; i < numVertices; i++) {
		      s.append(i + ": ");
		      for (int j : adjMatrix[i]) {
		        s.append(j + " ");
		      }
		      s.append("\n");
		    }
		    return s.toString();
		}
	}
	
	private ArrayList<Integer> cityNum = new ArrayList<Integer>(); // to store city names
	private ArrayList<String> cityName = new ArrayList<String>(); // to store number equivalent of city name
	private int numCity = 0; // to keep track of unique cities
	private Hashtable<String, Integer> possibleAttractions = new Hashtable<String, Integer>(); // to store attraction name as key and city number (from cityNum) as value
	ArrayList<String> detours = new ArrayList<String>(); // list of possible attractions on the way to ending city
	private int vertices = 291; // 291 unique cities based on previous calculation
	int INFINITY = Integer.MAX_VALUE;
	Graph g = new Graph(vertices);
	
	public void readRoads(String filename) { // reads roads.csv
		try {
			Scanner scan = new Scanner(new File(filename));
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				String[] arr = line.split(",");
				if(!cityName.contains(arr[0])) { // to handle duplicate starting city
					cityName.add(arr[0]);
					cityNum.add(numCity++);
				}
				if(!cityName.contains(arr[1])) { // to handle duplicate target city
					cityName.add(arr[1]);
					cityNum.add(numCity++);
				}
				// takes names of both cities, gets their position on the cityNum array list, adds those cities and the distance between them 
				String firstCityName = arr[0];
				String secondCityName = arr[1];
				int firstCityPos = cityNum.get(cityName.indexOf(firstCityName));
				int secondCityPos = cityNum.get(cityName.indexOf(secondCityName));
				g.addEdge(firstCityPos, secondCityPos, Integer.parseInt(arr[2]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readAttractions(String filename) { // reads attractions.csv
		try {
			Scanner scan = new Scanner(new File(filename));
			while(scan.hasNextLine()) {
				String line = scan.nextLine();
				String [] arr = line.split(",");
				if(arr[0].equals("Attraction") && arr[1].equals("Location")) continue; // to skip the first line of attractions.csv
				possibleAttractions.put(arr[0], cityNum.get(cityName.indexOf(arr[1])));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean validAttraction(String attraction) { // checks if inputed attraction is valid
		return possibleAttractions.containsKey(attraction);
	}
	
	private boolean validCity(String city) { // checks if inputed city is valid
		return cityName.contains(city);
	}
	
	private ArrayList<Integer> route(String startingCity, String endingCity, ArrayList<String> attractions) { // takes combines starting city, attractions, and ending city to route
		ArrayList<Integer> route = new ArrayList<Integer>();
		route.add(cityNum.get(cityName.indexOf(startingCity)));
		if (!attractions.isEmpty()) {
			for (String place : attractions) {
				route.add(possibleAttractions.get(place));
			}
		}
		route.add(cityNum.get(cityName.indexOf(endingCity)));
		return route;
	}
	
	private ArrayList<String> dijkstra(ArrayList<Integer> route) { // performs Dijkstra's algorithm to return an array list of the route that the user should take in sorted order of nearest cities/attractions

		int sourceVertex = route.get(0);
		
		// Dijkstra's table
		ArrayList<Boolean> visited = new ArrayList<Boolean>(); // "known" vertices
		int [] path = new int[vertices];// path to get to vertex
		int [] cost = new int [vertices]; // costs to get to vertex
		
		// sets up table
		for (int i = 0; i < path.length; i++) {
			path[i] = -1;
			cost[i] = INFINITY;
			visited.add(false);
		}
		
		cost[sourceVertex] = 0;
		
		for (int i = 0; i < vertices - 1; i++) { // for every vertex
			
			// find lowest cost vertex and set visited to true
			int lowestVertex = leastCostUnknownVertex(visited, cost);
			visited.set(lowestVertex, true);
			
			// iterate through all adjacent vertices
			for (int vertex = 0; vertex < vertices; vertex++) {
				
				// check if there is an edge between the current vertex and the next
				if (g.adjMatrix[lowestVertex][vertex] > 0) {
					
					// if the vertex has not yet been visited
					if(visited.get(vertex).equals(false)) {
						
						// set weight as lowest cost
						int lowestCost = g.adjMatrix[lowestVertex][vertex] + cost[lowestVertex];
					
						// if lowest cost is less than the cost of the vertex
						if(lowestCost < cost[vertex]) {
							cost[vertex] = lowestCost;
							path[vertex] = lowestVertex;
						}
					}
				}
			}
		}
		
		// Printing Dijkstra's table
//		System.out.println("City    Visited    Path    Cost");
//		for (int i = 0; i < vertices; i++) {
//			System.out.println(i + "    " + visited.get(i) + "    " + path[i] + "    " + cost[i]);
//		}

		Hashtable<Integer, Integer> attractionMap = new Hashtable<Integer, Integer>(); // key = cost to get to attraction, value = city num of attraction
		for (int i = 1; i < route.size() - 1; i++) {
			attractionMap.put(cost[route.get(i)], route.get(i));
		}
		ArrayList<Integer> attractionWeights = new ArrayList<Integer>(); // store attraction weights then store it
		for (int i = 1; i < route.size() - 1; i++) {
			attractionWeights.add(cost[route.get(i)]);
		}
		Collections.sort(attractionWeights);
		
		ArrayList<Integer> sortedPath = new ArrayList<Integer>(); // create the path where attractions are sorted based on distance from start city
		for(int i = 0; i < route.size(); i++) {
			if (i > 0 && i < route.size() - 1) { // adding attractions
				if (sortedPath.contains(attractionMap.get(attractionWeights.get(i-1)))){ // in case user enters duplicate cities, i.e. inputs Alcatraz with San Francisco as start
					continue;
				}else {
					sortedPath.add(attractionMap.get(attractionWeights.get(i-1)));
				}
			} else {
				sortedPath.add(route.get(i));
			}
		}
		ArrayList<String> finalPath = stackIt(sortedPath, path);
		return finalPath;
	}
	
	private ArrayList<String> stackIt(ArrayList<Integer> sortedPath, int [] path) { // takes the stack returned in getPath and converts it to string array list
		ArrayList<String> finalPath = new ArrayList<String>();
		
		for (int i = 0; i < sortedPath.size() - 1; i++) {
			Stack<String> stack = getPath(sortedPath, path);
			while(!stack.empty()) {
				finalPath.add(stack.pop());
			}
			sortedPath.remove(0);
		}
		return finalPath;
	}
	
	private Stack<String> getPath(ArrayList<Integer> sortedPath, int [] path) { // returns a stacked path to get from start to destination
		Stack<String> citiesByName = new Stack<String>();
		int start = sortedPath.get(0);
		int end = sortedPath.get(1);
		ArrayList<Integer> p = new ArrayList<Integer>();
		while(true) {
			if(end == start) {
				p.add(end);
				break;
			} else {
				p.add(end);
				end = path[end];
			}
		}
		for (Integer cityNum : p) {
			citiesByName.add(cityName.get(cityNum));
		}
		return citiesByName;
	}
	
	private void print(ArrayList<String> finalPath) { // print the route that the user should take
		System.out.println("The shortest path is: ");
		for (int i = 0; i < finalPath.size() - 1; i++) {
			if (finalPath.get(i) == finalPath.get(i+1)) {
				continue;
			} else {
				System.out.println(finalPath.get(i) + " => " + finalPath.get(i+1));
			}
		}
	}
	
	private int leastCostUnknownVertex(ArrayList<Boolean> visited, int [] costs) { // finds the least cost unknown vertex
		int minimum = INFINITY;
		int vertexIndex = -1;

		for (int i = 0; i < vertices; i++) {
			if (visited.get(i) == false && costs[i] < minimum) {
				minimum = costs[i];
				vertexIndex = i;
			}
		}
		return vertexIndex;
	}
	
	private void questions() { // continuously prompts user for valid cities and attractions then outputs shortest path
		RoadTrip r = new RoadTrip();
		// Where to input files
		r.readRoads("roads.csv");
		r.readAttractions("attractions.csv");
		
		while (true) {
			String startingCity = "";
			String endingCity = "";
			Scanner scan = new Scanner(System.in);
			boolean keepGoing = true;
			
			while (keepGoing) { // allows program to continuously prompt user for valid answers until EXIT
				System.out.println("Name of starting city (or EXIT to quit): ");
				startingCity = scan.nextLine();
				if (startingCity.equalsIgnoreCase("exit")) {
					System.out.println("The program will now exit. ");
					System.exit(1);
				}
				if (r.validCity(startingCity)) {
					keepGoing = false;
				} else {
					System.out.println("Please enter a valid city (i.e. San Francisco CA): ");
				}
			}

			while (!keepGoing) { // !keepGoing because it was previously set as false
				System.out.println("Name of ending city: ");
				endingCity = scan.nextLine();
				if (r.validCity(endingCity)) {
					keepGoing = true;
				} else {
					System.out.println("Please enter a valid city (i.e. San Francisco CA): ");
				}
			}
						
			ArrayList<String> attractions = new ArrayList<String>();
			
			while(true) {
				System.out.println("List an attraction along the way (or ENOUGH to stop listing): ");
				String attraction = scan.nextLine();
				if (attraction.equalsIgnoreCase("enough")) {
					break;
				}
				if(r.validAttraction(attraction)) {
					attractions.add(attraction);
				} else {
					System.out.println("Attraction '" + attraction + "' is unknown. ");
				}
			}
			
			ArrayList<Integer> cityInputNums = r.route(startingCity, endingCity, attractions);
			ArrayList<String> finalPath = new ArrayList<String>();
			int i = cityInputNums.size() - 1;
			while (i != 0) { // perform Dijkstra to find the shortest path to get from city to city
				ArrayList<String> sortedPath = r.dijkstra(cityInputNums);
				cityInputNums.remove(0);
				for (int k =0; k < sortedPath.size(); k++) {
					finalPath.add(sortedPath.get(k));
				}
				i--;
			}
			r.print(finalPath);
		}
	}
	
	public static void main(String[] args) {
		RoadTrip r = new RoadTrip();
		r.questions();
	}

}
