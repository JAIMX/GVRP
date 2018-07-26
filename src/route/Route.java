package route;

import java.io.CharConversionException;
import java.util.LinkedList;
import java.util.List;

import model.GVRP;
import model.GVRP.Edge;
import model.GVRP.Node;

public class Route{

    public class VisitNode{
        int nodeIndex;
        int arriveTime;
        int rechargeBackward,rechargeForward;
    }
    
    
    public int vehicleTypeIndex;
    public double totalWeight, totalVolume;
    public List<VisitNode> visitNodeList;
//    public boolean isFeasible; //violate the limit of weight, volume or time window
    
    public double transCost,chargeCost,waitCost,fixedCost;
    GVRP modelData;
    
    
    //for tryInsert
    public int tryInsertDemandIndex;
    public boolean ifCanInsert;
    public List<Double> tryInsertCostPlus;
    
    
    public Route(int vehicleTypeIndex,GVRP gvrp){
        this.modelData=gvrp;
        this.vehicleTypeIndex=vehicleTypeIndex;
        totalVolume=0;
        totalWeight=0;
//        isFeasible=true;
        transCost=0;
        chargeCost=0;
        waitCost=0;
        fixedCost=modelData.vehicleList.get(vehicleTypeIndex-1).vehicleFixedCost;
        
        visitNodeList=new LinkedList<VisitNode>();
        
        VisitNode node0=new VisitNode();
        node0.nodeIndex=0;
        visitNodeList.add(node0);    
        VisitNode node1=new VisitNode();
        node1.nodeIndex=0;
        visitNodeList.add(node1);
        
    }
    
    public void tryInsert(Node demand){
        
    }
    
    
    
    
}
