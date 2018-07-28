import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.poi.hssf.record.common.FeatSmartTag;

import model.GVRP;
import model.GVRP.Edge;
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

    public void initialize() throws FileNotFoundException {

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


        List<Integer> visitList = new ArrayList<>();
        while (!queue1.isEmpty()) {
            Node node = queue1.poll();
            visitList.add(node.index);
        }
        while (!queue2.isEmpty()) {
            Node node = queue2.poll();
            visitList.add(node.index);
        }

        
        
//----------------------------------------------------------------------------------------------------------------------------//       
        
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

//        int count=0;
        for (int demandIndex : visitList) {
        	

//            count++;
//            System.out.println("#"+count);
//            System.out.println(demandIndex);
//            System.out.println(routeList.size());
//            System.out.println();

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
            
            
            
            
            
            
            
            

        }
        
        
        
        List<Route> feasibleRouteList=new ArrayList<>();
        
        //we pick up one best route for each routesForVehicle
        for(int i=0;i<routeList.size();i++){
            List<Route> routesForVehicle=routeList.get(i);
            
            double minTotalCost=Double.MAX_VALUE;
            Route routeRecord=null;
            for(int j=0;j<routesForVehicle.size();j++){
                Route route=routesForVehicle.get(j);
                
                if(!route.isFailed&&minTotalCost>route.totalCost){
                    minTotalCost=route.totalCost;
                    routeRecord=route;
                }
            }
            
            if(routeRecord!=null){
                feasibleRouteList.add(routeRecord);
            }
        }
        
        System.out.println("Verify: "+verify(feasibleRouteList));
        
        
        //calculate information for route in order to output
        double cost=0;
        for(int i=0;i<feasibleRouteList.size();i++){
            Route route=feasibleRouteList.get(i);
            

            route.caluculateInfo();

            
            cost+=route.totalCost;
        }
        System.out.println(cost);
        
        
        //output
        PrintWriter pw=new PrintWriter(new File("Result.csv"));
        StringBuilder builder = new StringBuilder();
        String ColumnNamesList = "trans_code,vehicle_type,dist_seq,distribute_lea_tm,distribute_arr_tm,distance,trans_cost,charge_cost,wait_cost,fixed_use_cost,total_cost,charge_cnt";
        builder.append(ColumnNamesList +"\n");
        for(int i=0;i<1;i++){
            Route route=feasibleRouteList.get(i);
            
            if(i<10){
                builder.append("DP000");
            }else if (i<100) {
                builder.append("DP00");
            }else if (i<1000) {
                builder.append("DP0");
            }else{
                builder.append("DP");
            }
            
            builder.append("i"+","+route.vehicleTypeIndex+",");
            
            //node list
            for(int j=0;j<route.visitNodeList.size();j++){
                int nodeIndex=route.visitNodeList.get(j);
                if(j==0){
                    builder.append(nodeIndex);
                }else{
                    builder.append(";"+nodeIndex);
                }
            }
            builder.append(","+(route.leaveTime+modelData.time0)+(route.backTime+modelData.time0));
            builder.append(","+route.totalRun+","+route.transCost+","+route.chargeCost+","+route.waitCost+","+route.fixedCost+","+route.totalCost+","+route.chargeAmount);
            builder.append('\n');

        }
        
        pw.write(builder.toString());
        pw.close();
        System.out.println("done!");
        

    }
    
    public boolean verify(List<Route> feasibleRouteList){
        
        Set<Integer> ifCover=new HashSet<>();
        for(Route route:feasibleRouteList){
            for(int nodeIndex:route.visitNodeList){
                ifCover.add(nodeIndex);
            }
        }
        
        //check ifCover
        for(int i=1;i<modelData.numOfDemand+1;i++){
            if(!ifCover.contains(i)){
                System.out.println("Not cover all demands!!!");
                System.out.println("not cover demand "+i);
                return false;
            }
        }
        
        

        for(Route route:feasibleRouteList){
            
            double totalWeight=0;
            double totoalVolume=0;
            
            for(int nodeIndex:route.visitNodeList){
                Node node=modelData.nodeList.get(nodeIndex);
                
                if(node.typeIndex==2){
                    //check weight and volume
                    totalWeight+=node.weight;
                    totoalVolume+=node.volume;
                }
                
                
            }
            
            //check weight and volume
            if(totalWeight>modelData.vehicleList.get(route.vehicleTypeIndex-1).weight){
                System.out.println("weight limit error!!!");
                return false;
            }
            
            if(totoalVolume>modelData.vehicleList.get(route.vehicleTypeIndex-1).volume){
                System.out.println("volume limit error!!!");
                return false;
            }
            
            
            //improve time window 
            if(route.waitCost>0.001){
                int maxDelay=Integer.MAX_VALUE;
                for(int i=0;i<route.visitNodeList.size();i++){
                    int nodeIndex=route.visitNodeList.get(i);
                    Node node=modelData.nodeList.get(nodeIndex);
                    
                    maxDelay=Math.min(maxDelay, node.t2-route.visitNodeTimeList.get(i));
                }
                
                route.updateTimeWindow(maxDelay);
            }
            
            //check time window
            for(int i=0;i<route.visitNodeList.size();i++){
                int arriveTime=route.visitNodeTimeList.get(i);
                Node node=modelData.nodeList.get(route.visitNodeList.get(i));
                if(arriveTime<node.t1||arriveTime>node.t2){
                    System.out.println("Time window error!!!");
                    return false;
                }
            }
            
            
            
            //check charge constraint
            int powerUsed=0;
            for(int i=1;i<route.visitNodeList.size();i++){
                int u=route.visitNodeList.get(i-1);
                int v=route.visitNodeList.get(i);
                Node nodeU=modelData.nodeList.get(u);
                int edgeIndex=modelData.edgeForNode.get(u).get(v);
                Edge edge=modelData.edgeSet.get(edgeIndex);
                
                if(nodeU.typeIndex==1||nodeU.typeIndex==3){
                    powerUsed=edge.distance;
                }else{
                    powerUsed+=edge.distance;
                }
                
                if(powerUsed>modelData.vehicleList.get(route.vehicleTypeIndex-1).drivingRange){
                    System.out.println("Exceed driving range limit!!!");
                    return false;
                }
            }
        }
        

        return true;
    }
    
    
    

    public static void main(String[] args) throws IOException {
        GVRP gvrp = new GVRP("./data/A/input_node.xlsx", "./data/A/input_vehicle_type.xlsx",
                "./data/A/input_distance-time.txt");
        gvrp.preprocess();
        Solver solver = new Solver(gvrp);
        solver.initialize();
    }
}
