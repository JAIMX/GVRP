package route;

import java.io.CharConversionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.management.modelmbean.ModelMBeanAttributeInfo;

import org.apache.log4j.chainsaw.Main;

import model.GVRP;
import model.GVRP.Edge;
import model.GVRP.Node;

public class Route {


    public boolean isFailed;
    public int vehicleTypeIndex,drivingRange,totalWaitingTime;
    public double totalWeight, totalVolume;
    public LinkedList<Integer> visitNodeList,visitNodeTimeList;

    public double transCost, chargeCost, waitCost, fixedCost,totalCost;
    GVRP modelData;

    // for tryInsert
    public int tryInsertDemandIndex;
    public boolean ifCanInsert;
    public Route bestTryInsertRoute;
    
    //for output information
    public int leaveTime,backTime,totalRun,chargeAmount;
    

    public Route(int vehicleTypeIndex, GVRP gvrp) {
        this.isFailed=false;
        this.modelData = gvrp;
        this.vehicleTypeIndex = vehicleTypeIndex;
        this.drivingRange=modelData.vehicleList.get(vehicleTypeIndex-1).drivingRange;
        totalVolume = 0;
        totalWeight = 0;
        transCost = 0;
        chargeCost = 0;
        waitCost = 0;
        fixedCost = modelData.vehicleList.get(vehicleTypeIndex - 1).vehicleFixedCost;

        visitNodeList = new LinkedList<Integer>();

        visitNodeList.add(0);
        visitNodeList.add(0);

    }

    public Route(int vehicleTypeIndex, GVRP gvrp, LinkedList<Integer> visitNodeList) {
        this.isFailed=false;
        this.modelData = gvrp;
        this.vehicleTypeIndex = vehicleTypeIndex;
        this.drivingRange=modelData.vehicleList.get(vehicleTypeIndex-1).drivingRange;
        totalVolume = 0;
        totalWeight = 0;
        transCost = 0;
        chargeCost = 0;
        waitCost = 0;
        fixedCost = modelData.vehicleList.get(vehicleTypeIndex - 1).vehicleFixedCost;

        this.visitNodeList = visitNodeList;

    }

    public void tryInsert(Node demand) {

        this.tryInsertDemandIndex = demand.index;
        this.ifCanInsert=false;
        
        if ((totalWeight + demand.weight > modelData.vehicleList.get(vehicleTypeIndex - 1).weight)
                || (totalVolume + demand.volume > modelData.vehicleList.get(vehicleTypeIndex - 1).volume)) {
            this.ifCanInsert = false;
        } else {

            double minCostPlus=Double.MAX_VALUE;
            Route bestUpdateRouteRecord=null;
            
            // try to insert different positions
            for (int i = 1; i < this.visitNodeList.size(); i++) {
                LinkedList<Integer> tempVisitNodeList = (LinkedList<Integer>) this.visitNodeList.clone();
                tempVisitNodeList.add(i, demand.index);

                Route tempRoute = new Route(this.vehicleTypeIndex, this.modelData, tempVisitNodeList);
                boolean isFeasible=checkFeasibility(tempRoute);
                if(isFeasible){
                    this.ifCanInsert=true;
                    if(minCostPlus>tempRoute.transCost+tempRoute.waitCost+tempRoute.chargeCost){
                        bestUpdateRouteRecord=tempRoute;
                        minCostPlus=tempRoute.transCost+tempRoute.waitCost+tempRoute.chargeCost;
                    }
                }
            }
            
            if(this.ifCanInsert){
                this.bestTryInsertRoute=bestUpdateRouteRecord;
                
            }
            
            

        }

    }
    
    
    

    public boolean checkFeasibility(Route route) {
        
        //we first check time window feasibility
        boolean isTimeFeasible=updateTimeWindow(route);
        if(!isTimeFeasible){
            return false;
        }
        
        //check recharge feasibility
        boolean isRechargeFeasible=updateRechargeStation(route);
        if(!isRechargeFeasible){
            return false;
        }
        
        return true;
    }
    
    public boolean updateTimeWindow(Route route){
        route.visitNodeTimeList=new LinkedList<>();
        route.visitNodeTimeList.add(0);
        route.totalWaitingTime=0;
        
        for(int i=1;i<route.visitNodeList.size();i++){
            int u=route.visitNodeList.get(i-1);
            int v=route.visitNodeList.get(i);
            Node node=modelData.nodeList.get(v);
            int edgeIndex=modelData.edgeForNode.get(u).get(v);
            
            if(modelData.preprocessRecord.contains(edgeIndex)){
                return false;
            }
            
            Edge edge=modelData.edgeSet.get(edgeIndex);
            int arriveTime;
            if(i==1){
                arriveTime=Math.max(node.t1, route.visitNodeTimeList.get(i-1)+edge.spendTime);
                if(node.t1>route.visitNodeTimeList.get(i-1)+edge.spendTime){
                    route.totalWaitingTime+=node.t1-(route.visitNodeTimeList.get(i-1)+edge.spendTime);
                }
            }else{
                arriveTime=Math.max(node.t1, route.visitNodeTimeList.get(i-1)+edge.spendTime+30);
                if(node.t1>route.visitNodeTimeList.get(i-1)+edge.spendTime+30){
                    route.totalWaitingTime+=node.t1-(route.visitNodeTimeList.get(i-1)+edge.spendTime+30);
                }
            }
            route.visitNodeTimeList.add(arriveTime);
            if(arriveTime>node.t2){
                return false;
            }
            
        }
        
        return true;
    }
    
    public void updateTimeWindow(int maxDelay){
        
        if(maxDelay>0){

            int arriveTime=visitNodeTimeList.get(1);
            
            visitNodeTimeList=new LinkedList<>();
            visitNodeTimeList.add(0);
            
//            visitNodeTimeList.set(1, arriveTime+maxDelay);
            visitNodeTimeList.add(arriveTime+maxDelay);
            totalWaitingTime=0;
            
            for(int i=2;i<visitNodeList.size();i++){
                int u=visitNodeList.get(i-1);
                int v=visitNodeList.get(i);
                Node node=modelData.nodeList.get(v);
                int edgeIndex=modelData.edgeForNode.get(u).get(v);
                Edge edge=modelData.edgeSet.get(edgeIndex);
                
                arriveTime=Math.max(node.t1, visitNodeTimeList.get(i-1)+edge.spendTime+30);
                visitNodeTimeList.add(arriveTime);
                if(node.t1>visitNodeTimeList.get(i-1)+edge.spendTime+30){
                    totalWaitingTime+=node.t1-(visitNodeTimeList.get(i-1)+edge.spendTime+30);
                }
                
            }
            
            waitCost=24*totalWaitingTime/60.0;
            this.totalCost=transCost+chargeCost+waitCost+fixedCost;
            
            
        }
        
        //update leave time for depot
        int u=visitNodeList.get(0);
        int v=visitNodeList.get(1);
        Node node=modelData.nodeList.get(v);
        int edgeIndex=modelData.edgeForNode.get(u).get(v);
        Edge edge=modelData.edgeSet.get(edgeIndex);
        visitNodeTimeList.set(0, visitNodeTimeList.get(1)-edge.spendTime);
        
        
    }
    
    public boolean updateRechargeStation(Route route){
        
        boolean isFeasible=true;
        
        boolean ifFinish=false;
        while(!ifFinish){
            
            int listIndexRecord=-1;
            int[] powerUsed=new int[route.visitNodeList.size()];
            for(int i=0;i<route.visitNodeList.size();i++){
                if(i==0){
                    powerUsed[i]=0;
                }else{
                    int u=route.visitNodeList.get(i-1);
                    int v=route.visitNodeList.get(i);
                    Node nodeU=modelData.nodeList.get(u);
                    int edgeIndex=modelData.edgeForNode.get(u).get(v);
                    Edge edge=modelData.edgeSet.get(edgeIndex);
                    
                    if(nodeU.typeIndex==1||nodeU.typeIndex==3){
                        powerUsed[i]=edge.distance;
                    }else{
                        powerUsed[i]=edge.distance+powerUsed[i-1];
                    }
                }
                
                if(powerUsed[i]>route.drivingRange){
                    listIndexRecord=i;
                    break;
                }
                
            }
            
            // we need to add recharge station node
            if(listIndexRecord>=0){
                boolean ifCanFindRechargeStation=false;
                
                int nodeIndex=route.visitNodeList.get(listIndexRecord-1);
                int nearestEdgeIndex=modelData.nearestRechargeStation.get(nodeIndex);
                Edge nearestEdge=modelData.edgeSet.get(nearestEdgeIndex);
                
                if(powerUsed[listIndexRecord-1]+nearestEdge.distance<route.drivingRange){
                    route.visitNodeList.add(listIndexRecord, nearestEdge.v);
                    ifCanFindRechargeStation=true;
                }else{
                    if(listIndexRecord-2>=0){
                        nodeIndex=route.visitNodeList.get(listIndexRecord-2);
                        nearestEdgeIndex=modelData.nearestRechargeStation.get(nodeIndex);
                        nearestEdge=modelData.edgeSet.get(nearestEdgeIndex);
                        if(powerUsed[listIndexRecord-2]+nearestEdge.distance<route.drivingRange){
                            route.visitNodeList.add(listIndexRecord-1, nearestEdge.v);
                            ifCanFindRechargeStation=true;
                        }
                    }
                    
                }
                
                if(!ifCanFindRechargeStation){
                    isFeasible=false;
                    break;
                }
                
                
            }else{
                ifFinish=true;
            }
            
            
            
        }
        
        // we need to check time window
        if(ifFinish){
            if(updateTimeWindow(route)){ //we need to calculate : totalWeight, totalVolume, transCost, chargeCost, waitCost;
                route.totalWeight=0;
                route.totalVolume=0;
                route.transCost=0;
                route.chargeCost=0;
                route.waitCost=0;
                
                for(int i=1;i<route.visitNodeList.size();i++){
                    int u=route.visitNodeList.get(i-1);
                    int v=route.visitNodeList.get(i);
                    Node node=modelData.nodeList.get(v);
                    int edgeIndex=modelData.edgeForNode.get(u).get(v);
                    Edge edge=modelData.edgeSet.get(edgeIndex);
                    
                    if(node.typeIndex==2){
                        route.totalWeight+=node.weight;
                        route.totalVolume+=node.volume;
                    }
                    
                    route.transCost+=edge.distance;
                    
                    if(node.typeIndex==3){
                        route.chargeCost++;
                    }
                    
                }
                
                route.transCost=route.transCost*modelData.vehicleList.get(route.vehicleTypeIndex-1).unitTransCost/1000.0;
                route.chargeCost=route.chargeCost*50;
//                route.waitCost=24*route.waitCost/60.0;
                route.waitCost=24*route.totalWaitingTime/60.0;
                route.totalCost=route.transCost+route.chargeCost+route.waitCost+route.fixedCost;
                
            }else{
                isFeasible=false;
            }
        }
        
        
        
        return isFeasible;
        
        
    }
    
    public void caluculateInfo(){
        this.leaveTime=visitNodeTimeList.getFirst();
        this.backTime=visitNodeTimeList.getLast();
        
        totalRun=0;
        chargeAmount=0;
        for(int i=1;i<visitNodeList.size();i++){
            int u=visitNodeList.get(i-1);
            int v=visitNodeList.get(i);
            Node node=modelData.nodeList.get(v);
            int edgeIndex=modelData.edgeForNode.get(u).get(v);
            Edge edge=modelData.edgeSet.get(edgeIndex);
            
            
            totalRun+=edge.distance;
            
            if(node.typeIndex==3){
                chargeAmount++;
            }
            
        }
        
        this.transCost=totalRun*modelData.vehicleList.get(vehicleTypeIndex-1).unitTransCost/1000.0;
        this.chargeCost=chargeAmount*50;
        
        //calculate waiting cost
        totalWaitingTime=0;
        int u=visitNodeList.get(0);
        int v=visitNodeList.get(1);
        Node node=modelData.nodeList.get(v);
        int edgeIndex=modelData.edgeForNode.get(u).get(v);
        Edge edge=modelData.edgeSet.get(edgeIndex);
        
        int actualArriveTime=visitNodeTimeList.get(0)+edge.spendTime;
        totalWaitingTime+=Math.max(0, node.t1-actualArriveTime);
        int arriveTime=Math.max(node.t1, actualArriveTime);
        
        for(int i=2;i<visitNodeList.size();i++){
            u=visitNodeList.get(i-1);
            v=visitNodeList.get(i);
            node=modelData.nodeList.get(v);
            edgeIndex=modelData.edgeForNode.get(u).get(v);
            edge=modelData.edgeSet.get(edgeIndex);
            
            actualArriveTime=arriveTime+edge.spendTime+30;
            totalWaitingTime+=Math.max(0, node.t1-actualArriveTime);
            arriveTime=Math.max(node.t1, actualArriveTime);
        }
        
        this.waitCost=24*totalWaitingTime/60.0;
        this.totalCost=transCost+chargeCost+waitCost+fixedCost;
        
    }

    public static void main(String[] args) throws IOException {
//    	java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");
//    	double number=4.347;
//    	System.out.println(number);
//    	
//    	number=Double.valueOf(df.format(number));
//    	System.out.println(number);
    	
    	double   f   =   111231.5;
    	BigDecimal   b   =   new   BigDecimal(f);
    	double   f1   =   b.setScale(2,   RoundingMode.HALF_UP).doubleValue();
    	System.out.println(f1);


        
        
    }

}
