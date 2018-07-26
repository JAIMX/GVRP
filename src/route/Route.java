package route;

import java.util.LinkedList;
import java.util.List;

public class Route {

    class VisitNode{
        int arriveTime;
        int rechargeBackward,rechargeForward;
    }
    
    
    int vehicleTypeIndex;
    double totalWeight, totalVolume;
    List<VisitNode> visitNodeList;
    boolean ifFeasible;
    
    public Route(int vehicleTypeIndex){
        this.vehicleTypeIndex=vehicleTypeIndex;
        totalVolume=0;
        totalWeight=0;
        visitNodeList=new LinkedList<VisitNode>();
        ifFeasible=true;
    }
    
    
    
}
