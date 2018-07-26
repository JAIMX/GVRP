import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import model.GVRP;
import model.GVRP.Node;

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
        
        
        double[] cos = new double[modelData.numOfDemand+1];
        
        Comparator<Node> cosSort1=new Comparator<GVRP.Node>() {
        	
        	@Override
        	public int compare(Node node1,Node node2) {
        		if(cos[node1.index]-cos[node2.index]>0) {
        			return 1;
        		}
        		if(cos[node1.index]-cos[node2.index]<0) {
        			return -1;
        		}
        		return 0;
        		
        	}
		};
		
        
        Comparator<Node> cosSort2=new Comparator<GVRP.Node>() {
        	
        	@Override
        	public int compare(Node node1,Node node2) {
        		if(cos[node1.index]-cos[node2.index]>0) {
        			return -1;
        		}
        		if(cos[node1.index]-cos[node2.index]<0) {
        			return 1;
        		}
        		return 0;
        		
        	}
		};
		
		PriorityQueue<Node> queue1=new PriorityQueue<Node>(cosSort2);
		PriorityQueue<Node> queue2=new PriorityQueue<Node>(cosSort1);
		
		

        for (Node node : modelData.nodeList) {
        	if(node.typeIndex==2) {
                Vector vectorNode = new Vector(node.latitude - depot.latitude, node.longitude - depot.longitude);
                double cosValue = (vectorNode.x * vector0.x + vectorNode.y * vector0.y)
                        / (Math.sqrt(Math.pow(vectorNode.x, 2) + Math.pow(vectorNode.y, 2))
                                + Math.sqrt(Math.pow(vector0.x, 2) + Math.pow(vector0.y, 2)));
                cos[node.index]=cosValue;
                
                
                double direction=vectorNode.x*vector1.x+vectorNode.y*vector1.y;
                if(direction>0) {
                	queue2.add(node);
                }else {
                	queue1.add(node);
                }
                
        	}
        }
        
//        while(!queue1.isEmpty()) {
//        	Node tempNode=queue1.poll();
//        	System.out.println(cos[tempNode.index]);
//        }
//        
//        System.out.println();
//        while(!queue2.isEmpty()) {
//        	Node tempNode=queue2.poll();
//        	System.out.println(cos[tempNode.index]);
//        }
        
        List<Integer> visitList=new ArrayList<>();
        while(!queue1.isEmpty()) {
        	Node node=queue1.poll();
        	visitList.add(node.index);
        }
        while(!queue2.isEmpty()) {
        	Node node=queue2.poll();
        	visitList.add(node.index);
        }
        
        
        
        //we will initialize a feasible answer based on the sort of  visitList for demands
        
        
        
        
        
        
        

		
        
        
        
        

    }

    public static void main(String[] args) throws IOException {
        GVRP gvrp = new GVRP("./data/A/input_node.xlsx", "./data/A/input_vehicle_type.xlsx", "./data/A/input_distance-time.txt");
//      gvrp.preprocess();
    	Solver solver=new Solver(gvrp);
    	solver.initialize();
    }
}
