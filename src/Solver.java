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
        for (Node node : modelData.nodeList) {
            Vector vectorNode = new Vector(node.latitude - depot.latitude, node.longitude - depot.longitude);
            double cosValue = (vectorNode.x * vector0.x + vectorNode.y * vector0.y)
                    / (Math.sqrt(Math.pow(vectorNode.x, 2) + Math.pow(vectorNode.y, 2))
                            + Math.sqrt(Math.pow(vector0.x, 2) + Math.pow(vector0.y, 2)));
            cos[node.index]=cosValue;
        }
        
        

    }

    public static void main(String[] args) {

    }
}
