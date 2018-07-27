import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import model.GVRP;
import model.GVRP.Node;
import route.Route;

public class Solver {
    GVRP modelData;

    private class Vector {
        double x, y;

        public Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public Solver(GVRP gvrp) {
        this.modelData = gvrp;
    }

    public void initialize() {

        Node demand0 = modelData.nodeList.get(1);
        Node depot = modelData.nodeList.get(0);
        Vector vector0 = new Vector(demand0.latitude - depot.latitude, demand0.longitude - depot.longitude);
        double y = -vector0.x / vector0.y;
        Vector vector1 = new Vector(1, y);

        double[] cos = new double[modelData.numOfDemand + 1];

        Comparator<Node> cosSort1 = new Comparator<GVRP.Node>() {

            @Override
            public int compare(Node node1, Node node2) {
                if (cos[node1.index] - cos[node2.index] > 0) {
                    return 1;
                }
                if (cos[node1.index] - cos[node2.index] < 0) {
                    return -1;
                }
                return 0;

            }
        };

        Comparator<Node> cosSort2 = new Comparator<GVRP.Node>() {

            @Override
            public int compare(Node node1, Node node2) {
                if (cos[node1.index] - cos[node2.index] > 0) {
                    return -1;
                }
                if (cos[node1.index] - cos[node2.index] < 0) {
                    return 1;
                }
                return 0;

            }
        };

        PriorityQueue<Node> queue1 = new PriorityQueue<Node>(cosSort2);
        PriorityQueue<Node> queue2 = new PriorityQueue<Node>(cosSort1);

        for (Node node : modelData.nodeList) {
            if (node.typeIndex == 2) {
                Vector vectorNode = new Vector(node.latitude - depot.latitude, node.longitude - depot.longitude);

                double cosValue = (vectorNode.x * vector0.x + vectorNode.y * vector0.y)
                        / (Math.sqrt(Math.pow(vectorNode.x, 2) + Math.pow(vectorNode.y, 2))
                                * Math.sqrt(Math.pow(vector0.x, 2) + Math.pow(vector0.y, 2)));
                cos[node.index] = cosValue;

                double direction = vectorNode.x * vector1.x + vectorNode.y * vector1.y;
                if (direction > 0) {
                    queue2.add(node);
                } else {
                    queue1.add(node);
                }

            }
        }

        // System.out.println(queue1.size());
        // System.out.println(queue2.size());
        //
        // while(!queue1.isEmpty()) {
        // Node tempNode=queue1.poll();
        // System.out.println(cos[tempNode.index]);
        // }
        //
        // System.out.println();
        // while(!queue2.isEmpty()) {
        // Node tempNode=queue2.poll();
        // System.out.println(cos[tempNode.index]);
        // }

        List<Integer> visitList = new ArrayList<>();
        while (!queue1.isEmpty()) {
            Node node = queue1.poll();
            visitList.add(node.index);
        }
        while (!queue2.isEmpty()) {
            Node node = queue2.poll();
            visitList.add(node.index);
        }

        // we will initialize a feasible answer based on the sort of visitList
        // for demands
        List<List<Route>> routeList = new ArrayList<>();// for nodes
                                                        // combination<for
                                                        // different vehicle
                                                        // types>
        List<Route> route0 = new ArrayList<Route>();
        for (int i = 0; i < modelData.numOfVehicleType; i++) {
            Route route = new Route(i + 1, modelData);
            route0.add(route);
        }
        routeList.add(route0);

        int count=0;
        for (int demandIndex : visitList) {
        	

            count++;
            System.out.println("#"+count);
            System.out.println(demandIndex);
            System.out.println(routeList.size());
            System.out.println();

            int listIndexRecord=-1;
            double minObjRecord=Double.MAX_VALUE;
            
            Node demand = modelData.nodeList.get(demandIndex);
            for (int i=0;i<routeList.size();i++ ) {
                List<Route> routesForVehicle = routeList.get(i);
                
//                System.out.println("i= "+i);
                for (int j=0;j<routesForVehicle.size();j++) {
                    Route route = routesForVehicle.get(j);
                    
//                    System.out.println("j="+j);
                    if(route.isFailed==false){
                        route.tryInsert(demand);
                        
                        if(route.ifCanInsert){
                            if(minObjRecord>(route.bestTryInsertRoute.totalCost-route.totalCost)){
                                minObjRecord=route.bestTryInsertRoute.totalCost-route.totalCost;
                                listIndexRecord=i;
                            }
                        }
                    }

                    

                }
                
            }
            
            
            //if can not find a place to insert, we add a new route to routeList and try to insert demand
            if(listIndexRecord<0){
                route0=new ArrayList<>();
                for(int i=0;i<modelData.numOfVehicleType;i++){
                    Route route=new Route(i+1,modelData);
                    route.tryInsert(demand);
                    
                    if(route.bestTryInsertRoute!=null) {
                        route=route.bestTryInsertRoute;
                    }else {
                    	route.isFailed=true;
                    }

                    route0.add(route);
                }
                routeList.add(route0);
            }else{ // if we find a place to insert, change the route for all vehicles, is some one fails, we drop this vehicle type
                List<Route> routesForVehicle=routeList.get(listIndexRecord);
                for(int i=0;i<routesForVehicle.size();i++){
                    Route route=routesForVehicle.get(i);
                    if(route.ifCanInsert){
//                        route=route.bestTryInsertRoute;
                        routesForVehicle.set(i, route.bestTryInsertRoute);
                    }else{
                        route.isFailed=true;
                    }
                }
                
            }
            
            
            
            
            
            
        	//check for null object in routeList
        	for(int i=0;i<routeList.size();i++) {
        		List<Route> routesForVehicle=routeList.get(i);
        		for(int j=0;j<routesForVehicle.size();j++) {
        			if(routesForVehicle.get(j)==null) {
        				System.out.println("Error accurs!!!");
        				System.out.println("demandIndex="+demandIndex);
        				System.out.println("i="+i);
        				System.out.println("j="+j);
        			}
        		}
        	}
            
            

        }
        
        
        
        List<Route> feasibleRouteList=new ArrayList<>();
        
        //we pick up one best route for each routesForVehicle
        for(int i=0;i<routeList.size();i++){
            List<Route> routesForVehicle=routeList.get(i);
            
            double minTotalCost=Double.MAX_VALUE;
            Route routeRecord=null;
            for(int j=0;j<routesForVehicle.size();j++){
                Route route=routesForVehicle.get(j);
                if(minTotalCost>route.totalCost){
                    minTotalCost=route.totalCost;
                    routeRecord=route;
                }
            }
            
            if(routeRecord!=null){
                feasibleRouteList.add(routeRecord);
            }
        }
        
        System.out.println(feasibleRouteList.size());
        
        

    }

    public static void main(String[] args) throws IOException {
        GVRP gvrp = new GVRP("./data/A/input_node.xlsx", "./data/A/input_vehicle_type.xlsx",
                "./data/A/input_distance-time.txt");
        gvrp.preprocess();
        Solver solver = new Solver(gvrp);
        solver.initialize();
    }
}
