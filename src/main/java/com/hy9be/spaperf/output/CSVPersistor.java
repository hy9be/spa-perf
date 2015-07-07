package com.hy9be.spaperf.output;

import com.hy9be.spaperf.metrics.BaseMetricGroup;

import java.io.FileWriter;
import java.util.List;

/**
 * Created by hyou on 6/20/15.
 */
public class CSVPersistor {
    public CSVPersistor(String fileName, String[] selectedPerformanceCounter) {
        try {
            FileWriter myCSV = new FileWriter(fileName);
            // generator column header for the csv file
            myCSV.write("Time Unit: ms, ");
            for (String counterName : selectedPerformanceCounter) {
                myCSV.write(counterName + ", ");
                myCSV.write(counterName + "(max), ");
                myCSV.write(counterName + "(count), ");
            }
            myCSV.write("\n");
            myCSV.close();
        } catch (Exception ex) {
            System.out.println("File writing error!\n");
        }
    }

    public static void csvResult(List<BaseMetricGroup> performanceResult, String[] selectedPerformanceCounters, String actionName, String fileName) {

        //open the existing file
        try {
            FileWriter myCSV = new FileWriter(fileName, true);
            // write row header
            myCSV.write(actionName + ", ");
            //combine the perf data from all the BaseMetrics objects
            for (BaseMetricGroup perfMetrics : performanceResult) {
                for(String counterKey: selectedPerformanceCounters)
                    if(perfMetrics.performanceResult.containsKey(counterKey)) {
                        for (int j = 0; j < 3; j++)
                            myCSV.write(perfMetrics.performanceResult.get(counterKey)[j] + ", ");
                    }
            }
            myCSV.write("\n");
            myCSV.close();
        }
        catch (Exception ex) {
            System.out.println("File writing error2!\n");
        }
    }
}
