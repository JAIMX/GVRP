package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class GVRP {

    public int time0,numOfDemand;
    public List<vehicle> vehicleList;
    public List<Node> nodeList;
    public Map<Integer,Edge> edgeSet;
    public Set<Integer> preprocessRecord;
    
    public double weightMax,volumeMax;
    public int distanceMax;
    
    public Map<Integer,Integer> nearestRechargeStation; //demandIndex->edgeIndex
    

    public class vehicle {
        private int index, drivingRange, vehicleFixedCost, chargeTime;
        private String name;
        private double volume, weight, unitTransCost;

    }
    
    public class Node{
        public double longitude, latitude, weight, volume;
        public int t1,t2;
        public int typeIndex,index;
    }
    
    public class Edge{
        private int index,u,v,distance,spendTime;
    }

    public GVRP(String nodeFileName, String vehicleTypeFileName, String distanceTimeFileName) throws IOException {
        vehicleList=new ArrayList<>();
        nodeList=new ArrayList<>();
        edgeSet=new HashMap<>();
        preprocessRecord=new HashSet<>();
        numOfDemand=0;
        nearestRechargeStation=new HashMap<>();
        
        readNodeFile(nodeFileName);
        readVehicleTypeFile(vehicleTypeFileName);
//        readDistanceTimeFile(distanceTimeFileName);

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

            Node node=new Node();
            node.index=index;
            node.typeIndex=type;
            
            // depot
            if (type == 1) {
                cell = cellIterator.next();
                node.longitude = cell.getNumericCellValue();
                cell = cellIterator.next();
                node.latitude = cell.getNumericCellValue();

                cellIterator.next();
                cellIterator.next();
                cell = cellIterator.next();

                this.time0 = (int) (cell.getNumericCellValue() * 24 * 60);
                node.t1 = 0;
                cell = cellIterator.next();
                node.t2 = (int) cell.getNumericCellValue() * 24 * 60 - time0;
                

            }

            // demand
            if (type == 2) {
                numOfDemand++;
                cell = cellIterator.next();
                node.longitude = cell.getNumericCellValue();
                cell = cellIterator.next();
                node.latitude = cell.getNumericCellValue();

                cell = cellIterator.next();
                node.weight = cell.getNumericCellValue();
                cell = cellIterator.next();
                node.volume = cell.getNumericCellValue();
                cell = cellIterator.next();

                // demand.t1=timeTransfer(cell.getStringCellValue())-time0;
                node.t1 = (int) (cell.getNumericCellValue() * 24 * 60 - time0);
                node.t1 = Math.max(node.t1, 0);

                cell = cellIterator.next();
                node.t2 = (int) (cell.getNumericCellValue() * 24 * 60 - time0);
                

            }

            // recharge station
            if (type == 3) {
                cell = cellIterator.next();
                node.longitude = cell.getNumericCellValue();
                cell = cellIterator.next();
                node.latitude = cell.getNumericCellValue();
                
                node.t1=nodeList.get(0).t1;
                node.t2=nodeList.get(0).t2;
            }
            
            nodeList.add(node);
        }
        


    }

    public void readVehicleTypeFile(String vehicleTypeFileName) throws IOException {
        
        this.weightMax=Double.MIN_VALUE;
        this.volumeMax=Double.MIN_VALUE;
        this.distanceMax=Integer.MIN_VALUE;
        
        
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
            this.volumeMax=Math.max(this.volumeMax, vehicle.volume);
            
            cell=cellIterator.next();
            vehicle.weight=cell.getNumericCellValue();
            this.weightMax=Math.max(this.weightMax, vehicle.weight);
            
            cell=cellIterator.next();
            cell=cellIterator.next();
            vehicle.drivingRange=(int) cell.getNumericCellValue();
            this.distanceMax=Math.max(this.distanceMax, vehicle.drivingRange);
            
            cell=cellIterator.next();
            vehicle.chargeTime=(int) (cell.getNumericCellValue()*60);
            
            cell=cellIterator.next();
            vehicle.unitTransCost=cell.getNumericCellValue();
            
            cell=cellIterator.next();
            vehicle.vehicleFixedCost=(int) cell.getNumericCellValue();
            
            vehicleList.add(vehicle);
            
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
            
            edgeSet.put(index, edge);
        }
        
        //calculate nearestRechargeStation
        Map<Integer,Integer> nearestRecord=new HashMap<>();
        for(int i=0;i<numOfDemand;i++){
            nearestRecord.put(i+1, Integer.MAX_VALUE);
        }
        
        for(int edgeIndex:edgeSet.keySet()){
            Edge edge=edgeSet.get(edgeIndex);
            Node u=nodeList.get(edge.u);
            Node v=nodeList.get(edge.v);
            
            if(u.typeIndex==2&&v.typeIndex!=2){
                if(edge.distance<nearestRecord.get(edge.u)){
                    nearestRechargeStation.put(edge.u, edgeIndex);
                    nearestRecord.put(edge.u,edge.distance);
                }
            }
        }
        
        
        
        
    }
    
    public void preprocess(){
        
        Map<Integer,Integer> distanceToDepot=new HashMap<>();
        for(int edgeIndex:edgeSet.keySet()){
            Edge edge=edgeSet.get(edgeIndex);
            if(edge.u==0){
                distanceToDepot.put(edge.v, edge.spendTime);
            }
        }
        
        
        
        for(int edgeIndex:edgeSet.keySet()){
            Edge edge=edgeSet.get(edgeIndex);
            

            Node u=nodeList.get(edge.u);
            Node v=nodeList.get(edge.v);
            
            //consider the situation edge(u,v) exceed maxmal weight or volume (6096/1211100)
            if(u.typeIndex==2&&v.typeIndex==2){
                if((u.weight+v.weight>weightMax)||(u.volume+v.volume>volumeMax)){
                    preprocessRecord.add(edgeIndex);
                    continue;
                }
            }
            
            //consider edge(u,v) violate time window situations(428131/1211100)
            if(u.typeIndex!=1){
                if(u.t1+30+edge.spendTime>v.t2){
                    preprocessRecord.add(edgeIndex);
                    continue;
                }
            }
            
            //consider the situation after passing by edge(u,v), it impossible to comeback to depot on time(no use)
//            if(u.typeIndex!=1&&v.typeIndex!=1){
//                if(u.t1+30+edge.spendTime+30+distanceToDepot.get(edge.v)>nodeList.get(0).t2){
//                    preprocessRecord.add(edgeIndex);
//                    continue;
//                }
//            }
            
            //consider recharge constraints(no use)
//            if(u.typeIndex==2&&v.typeIndex==2){
//                Edge edge1=edgeSet.get(nearestRechargeStation.get(edge.u));
//                Edge edge2=edgeSet.get(nearestRechargeStation.get(edge.v));
//                
//                if(edge1.distance+edge.distance+edge2.distance>distanceMax){
//                    preprocessRecord.add(edgeIndex);
//                    continue;
//                }
//                
//            }
            
            
            
        }
        
        System.out.println("||-----------------------Preprocess----------------------------||");
        System.out.println("We have "+edgeSet.size()+" initial edges in the network");
        System.out.println("After preprocessing, there are "+(edgeSet.size()-preprocessRecord.size())+" edges left in the network.");
        
        for(int edgeIndex:preprocessRecord){
            edgeSet.remove(edgeIndex);
        }
        
    }


    public static void main(String[] args) throws IOException {
        GVRP gvrp = new GVRP("./data/A/input_node.xlsx", "./data/A/input_vehicle_type.xlsx", "./data/A/input_distance-time.txt");
//        gvrp.preprocess();

    }

}
