package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class GVRP {

    public int time0;
    public Depot depot;
    public Set<Demand> demandSet;
    public Set<RechargeStation> rechargeStationSet;
    
    public class Depot{
        private double longitude,latitude;
        private int t1,t2;
    }
    
    public class Demand{
        private double longitude,latitude,weight,volume;
        private int t1,t2;
        private int index;
    }
    
    public class RechargeStation{
        private double longitude,latitude;
        private int index;
    }
    
    public GVRP(String nodeFileName,String vehicleTypeFileName,String distanceTimeFileName) throws IOException{
        demandSet=new HashSet<>();
        rechargeStationSet=new HashSet<>();
        readNodeFile(nodeFileName);
        
    }
    
    public void readNodeFile(String nodeFileName) throws IOException{
        FileInputStream file=new FileInputStream(new File(nodeFileName));
        
        XSSFWorkbook workbook=new XSSFWorkbook(file);
        XSSFSheet sheet=workbook.getSheetAt(0);
        
        Iterator<Row> rowIterator=sheet.iterator();
        rowIterator.next();
        while(rowIterator.hasNext()){
            Row row=rowIterator.next();
            

            Iterator<Cell> cellIterator=row.cellIterator();
            Cell cell=cellIterator.next();
            int index=(int) cell.getNumericCellValue();
            cell=cellIterator.next();
            int type=(int) cell.getNumericCellValue();
            
            //depot
            if(type==1){
                this.depot=new Depot();
                cell=cellIterator.next();
                depot.longitude=cell.getNumericCellValue();
                cell=cellIterator.next();
                depot.latitude=cell.getNumericCellValue();
                
                cellIterator.next();
                cellIterator.next();
                cell=cellIterator.next();
                
                this.time0=timeTransfer(cell.getStringCellValue());
                depot.t1=0;
                cell=cellIterator.next();
                depot.t2=timeTransfer(cell.getStringCellValue());
                if(depot.t2==0){
                    depot.t2=24*60-time0;
                }
            }
            
            //demand
            if(type==2){
                Demand demand=new Demand();
                demand.index=index;
                cell=cellIterator.next();
                demand.longitude=cell.getNumericCellValue();
                cell=cellIterator.next();
                demand.latitude=cell.getNumericCellValue();
                
                cell=cellIterator.next();
                demand.weight=cell.getNumericCellValue();
                cell=cellIterator.next();
                demand.volume=cell.getNumericCellValue();
                cell=cellIterator.next();
                
                demand.t1=timeTransfer(cell.getStringCellValue())-time0;
                demand.t1=Math.max(demand.t1, 0);
                
                cell=cellIterator.next();
                int tempTime=timeTransfer(cell.getStringCellValue());
                if(tempTime==0){
                    depot.t2=24*60-time0;
                }else{
                    depot.t2=tempTime-time0;
                }
                
                demandSet.add(demand);
            }
            
            //recharge station
            if(type==3){
                RechargeStation rechargeStation=new RechargeStation();
                rechargeStation.index=index;
                cell=cellIterator.next();
                rechargeStation.longitude=cell.getNumericCellValue();
                cell=cellIterator.next();
                rechargeStation.latitude=cell.getNumericCellValue();
                
                rechargeStationSet.add(rechargeStation);
            }
            
            
        }
        
    }
    
    public void readVehicleTypeFile(String vehicleTypeFileName){
        
    }
    
    public void readdistanceTimeFile(String distanceTimeFileName){
        
    }
    
    public static int timeTransfer(String timeStr){
        int hour=Integer.parseInt(timeStr.substring(0, 2));
        int minute=Integer.parseInt(timeStr.substring(3));
        return hour*60+minute;
    }
    
    
    public static void main(String[] args) throws IOException {
        GVRP gvrp=new GVRP("./data/A/input_node.xlsx", "", "");
        System.out.println(GVRP.timeTransfer("00:00"));
        
    }
    
}
