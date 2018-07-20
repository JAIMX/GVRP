package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class GVRP {

    public int time0;
    public Depot depot;
    public Map<Integer,Demand> demandSet;
    public Map<Integer,RechargeStation> rechargeStationSet;
    public List<vehicle> vehicleSet;
    public Map<Integer,Edge> edgeSet;

    public class Depot {
        private double longitude, latitude;
        private int t1, t2;
    }

    public class Demand {
        private double longitude, latitude, weight, volume;
        private int t1, t2;
        private int index;
    }

    public class RechargeStation {
        private double longitude, latitude;
        private int index;
    }

    public class vehicle {
        private int index, drivingRange, vehicleFixedCost, chargeTime;
        private String name;
        private double volume, weight, unitTransCost;

    }
    
    public class Edge{
        private int index,u,v,distance,spendTime;
    }

    public GVRP(String nodeFileName, String vehicleTypeFileName, String distanceTimeFileName) throws IOException {
        demandSet = new HashMap<>();
        rechargeStationSet = new HashMap<>();
        vehicleSet=new ArrayList<>();
        edgeSet=new HashMap<>();
        
        readNodeFile(nodeFileName);
        readVehicleTypeFile(vehicleTypeFileName);
        readDistanceTimeFile(distanceTimeFileName);

    }

    public void readNodeFile(String nodeFileName) throws IOException {
        FileInputStream file = new FileInputStream(new File(nodeFileName));

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Iterator<Cell> cellIterator = row.cellIterator();
            Cell cell = cellIterator.next();
            int index = (int) cell.getNumericCellValue();
            cell = cellIterator.next();
            int type = (int) cell.getNumericCellValue();

            // depot
            if (type == 1) {
                this.depot = new Depot();
                cell = cellIterator.next();
                depot.longitude = cell.getNumericCellValue();
                cell = cellIterator.next();
                depot.latitude = cell.getNumericCellValue();

                cellIterator.next();
                cellIterator.next();
                cell = cellIterator.next();

                this.time0 = (int) (cell.getNumericCellValue() * 24 * 60);
                depot.t1 = 0;
                cell = cellIterator.next();
                depot.t2 = (int) cell.getNumericCellValue() * 24 * 60 - time0;

            }

            // demand
            if (type == 2) {
                Demand demand = new Demand();
                demand.index = index;
                cell = cellIterator.next();
                demand.longitude = cell.getNumericCellValue();
                cell = cellIterator.next();
                demand.latitude = cell.getNumericCellValue();

                cell = cellIterator.next();
                demand.weight = cell.getNumericCellValue();
                cell = cellIterator.next();
                demand.volume = cell.getNumericCellValue();
                cell = cellIterator.next();

                // demand.t1=timeTransfer(cell.getStringCellValue())-time0;
                demand.t1 = (int) (cell.getNumericCellValue() * 24 * 60 - time0);
                demand.t1 = Math.max(demand.t1, 0);

                cell = cellIterator.next();
                demand.t2 = (int) (cell.getNumericCellValue() * 24 * 60 - time0);

                demandSet.put(demand.index,demand);
            }

            // recharge station
            if (type == 3) {
                RechargeStation rechargeStation = new RechargeStation();
                rechargeStation.index = index;
                cell = cellIterator.next();
                rechargeStation.longitude = cell.getNumericCellValue();
                cell = cellIterator.next();
                rechargeStation.latitude = cell.getNumericCellValue();

                rechargeStationSet.put(rechargeStation.index,rechargeStation);
            }

        }

    }

    public void readVehicleTypeFile(String vehicleTypeFileName) throws IOException {
        FileInputStream file = new FileInputStream(new File(vehicleTypeFileName));

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next();
        while(rowIterator.hasNext()){
            Row row = rowIterator.next();

            vehicle vehicle=new vehicle();
            Iterator<Cell> cellIterator = row.cellIterator();
            Cell cell = cellIterator.next();
            vehicle.index=(int) cell.getNumericCellValue();
            
            cell=cellIterator.next();
            vehicle.name=cell.getStringCellValue();
            
            cell=cellIterator.next();
            vehicle.volume=cell.getNumericCellValue();
            
            cell=cellIterator.next();
            vehicle.weight=cell.getNumericCellValue();
            
            cell=cellIterator.next();
            cell=cellIterator.next();
            vehicle.drivingRange=(int) cell.getNumericCellValue();
            
            cell=cellIterator.next();
            vehicle.chargeTime=(int) (cell.getNumericCellValue()*60);
            
            cell=cellIterator.next();
            vehicle.unitTransCost=cell.getNumericCellValue();
            
            cell=cellIterator.next();
            vehicle.vehicleFixedCost=(int) cell.getNumericCellValue();
            
            vehicleSet.add(vehicle);
            
        }
    }

    public void readDistanceTimeFile(String distanceTimeFileName) throws IOException {
        Scanner in = new Scanner(Paths.get(distanceTimeFileName));
        in.nextLine();
        
        while(in.hasNextLine()){
            String line=in.nextLine();
            String[] result=line.split(",");
            int index=Integer.parseInt(result[0]);
            Edge edge=new Edge();
            edge.u=Integer.parseInt(result[1]);
            edge.v=Integer.parseInt(result[2]);
            edge.distance=Integer.parseInt(result[3]);
            edge.spendTime=Integer.parseInt(result[4]);
            edge.index=index;
            
//            System.out.println(edge.u+"->"+edge.v+" "+edge.distance+" "+edge.spendTime);
            edgeSet.put(index, edge);
        }
        System.out.println(edgeSet.size());
        
    }


    public static void main(String[] args) throws IOException {
        GVRP gvrp = new GVRP("./data/A/input_node.xlsx", "./data/A/input_vehicle_type.xlsx", "./data/A/input_distance-time.txt");

    }

}
