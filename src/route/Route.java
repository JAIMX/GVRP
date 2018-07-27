package route;

import java.io.CharConversionException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.chainsaw.Main;

import model.GVRP;
import model.GVRP.Edge;
import model.GVRP.Node;

public class Route {


    public int vehicleTypeIndex;
    public double totalWeight, totalVolume;
    public LinkedList<Integer> visitNodeList,visitNodeTimeList;

    public double transCost, chargeCost, waitCost, fixedCost;
    GVRP modelData;

    // for tryInsert
    public int tryInsertDemandIndex;
    public boolean ifCanInsert;
    public Route bestTryInsertRoute;

    public Route(int vehicleTypeIndex, GVRP gvrp) {
        this.modelData = gvrp;
        this.vehicleTypeIndex = vehicleTypeIndex;
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
        this.modelData = gvrp;
        this.vehicleTypeIndex = vehicleTypeIndex;
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

        if ((totalWeight + demand.weight > modelData.vehicleList.get(vehicleTypeIndex - 1).weight)
                || (totalVolume + demand.volume > modelData.vehicleList.get(vehicleTypeIndex - 1).volume)) {
            this.ifCanInsert = false;
        } else {

            double minCostPlus=Double.MAX_VALUE;
            Route bestUpdateRouteRecord;
            
            // try to insert different positions
            for (int i = 1; i < this.visitNodeList.size(); i++) {
                LinkedList<Integer> tempVisitNodeList = (LinkedList<Integer>) this.visitNodeList.clone();
                tempVisitNodeList.add(i, demand.index);

                Route tempRoute = new Route(this.vehicleTypeIndex, this.modelData, tempVisitNodeList);
                boolean isFeasible=checkFeasibility(tempRoute);
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
        
        
    }
    
    public boolean updateTimeWindow(Route route){
        visitNodeTimeList=new LinkedList<>();
        visitNodeTimeList.add(0);
        
        for(int i=1;i<visitNodeList.size();i++){
            int u=visitNodeList.get(i-1);
            int v=visitNodeList.get(i);
            Node node=modelData.nodeList.get(v);
            int edgeIndex=modelData.edgeForNode.get(u).get(v);
            
            if(modelData.preprocessRecord.contains(edgeIndex)){
                return false;
            }
            
            Edge edge=modelData.edgeSet.get(edgeIndex);
            int arriveTime;
            if(i==1){
                arriveTime=Math.max(node.t1, visitNodeTimeList.get(i-1)+edge.spendTime);
            }else{
                arriveTime=Math.max(node.t1, visitNodeTimeList.get(i-1)+edge.spendTime+30);
            }
            visitNodeTimeList.add(arriveTime);
            if(arriveTime>node.t2){
                return false;
            }
            
        }
        
        return true;
    }
    
    

    public static void main(String[] args) {
        LinkedList<Integer> list1 = new LinkedList<>();
        list1.add(0);
        list1.add(4);
        LinkedList<Integer> list2 = (LinkedList<Integer>) list1.clone();
        System.out.println(list2.toString());
        list2.add(2, 5);
        System.out.println(list2.toString());
    }

}
