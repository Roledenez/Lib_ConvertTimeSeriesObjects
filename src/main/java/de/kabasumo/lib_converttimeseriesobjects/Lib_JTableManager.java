
package de.kabasumo.lib_converttimeseriesobjects;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains basic methods to manage JTable content.
 * @author Bastian Engelmann
 */
public class Lib_JTableManager {
    
    final static Logger logger = LoggerFactory.getLogger(Lib_JTableManager.class);
    
    /**
     * Imports a csv-file into a Jtable object
     * When calling use as 'charset' for example:
     * Charset.forName("UTF-8")
     * For more see also http://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html
     * 
     * @param FilePath path to 
     * @param delimiter set to the char that separates each data element in the CSV (often: ',' or ';')
     * @param charset see remark above
     * @return JTable
     * @throws IOException 
     */
    public static JTable ImportCSVtoTable (String FilePath, char delimiter, Charset charset) throws IOException
    {
	Vector data = new Vector();
        
        CsvReader csvReader = new CsvReader(FilePath, delimiter, charset);
            
        // read first row as Header, get number of columns
        csvReader.readHeaders();
        
        // Read header content
        int columns = csvReader.getHeaderCount(); // Column number
        
        String[] columnNames = new String[columns]; // String array
        columnNames = csvReader.getHeaders(); // Get Header names
        Vector<String> columnNamesVector = new Vector<String>(Arrays.asList(columnNames)); // Convert array to vector
        
        // getting the data into data vector
        while (csvReader.readRecord()) {
             
              Vector row = new Vector(columns);
              for (int i = 0; i <= columns; i++) {
                  row.addElement( csvReader.get(i) );
              }
              data.addElement( row );
              
        }
               
        //  Create table
        JTable table = new JTable(data, columnNamesVector);
       
        return(table);
    }
    
    
    
    /**
     * Writes a table object to a CSV file. 
     * The table has to be a JTable object. 
     * With outputfile you can specify the location and the name of the CSV file.
     * For the delimiter usually ';' or ',' is chosen.
     * The file charset is depending on the system that creates the CSV file.
     * @param table JTable object
     * @param outputFile location and name of the CSV
     * @param delimiter seperates data entries
     * @see Charset
     * @throws IOException 
     */ 
    public static void ExportTabletoCSV (JTable table, String outputFile, char delimiter) throws IOException
    { 
        // before we open the file check to see if it already exists
        boolean alreadyExists = new File(outputFile).exists();
       
        try {
                // use FileWriter constructor that specifies open for appending
                CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), delimiter);
                
                // if the file didn't already exist then we need to write out the header line
                if (!alreadyExists)
                {
                        for(int i=0;i<=table.getColumnCount()-1;i++)
                        {
                            csvOutput.write(table.getColumnName(i));
                        }
                        csvOutput.endRecord();
                }
                // else assume that the file already has the correct header line
                
                // write records
                for(int i=0;i<=table.getRowCount()-1;i++)
                {
                    for(int j=0;j<=table.getColumnCount()-1;j++)
                    {
                        csvOutput.write(table.getValueAt(i, j).toString());
                    }
                    csvOutput.endRecord();
                }
                
                csvOutput.close();
                
        } catch (IOException e) {
                e.printStackTrace();
                logger.error("StackTrace: {}",e);
        }
        
    }
    
    /**
     * Small method to output a JTable object to the system.out
     * @param mytable 
     */
    public static void PrintTableToConsole(JTable mytable){
        
        System.out.println("Table:");
        for (int i=0; i < mytable.getColumnCount(); i++)
        {
            System.out.print("\t"+mytable.getColumnName(i));
        }       
        System.out.print("\n");
        
        for (int row=0; row < mytable.getRowCount(); row++) 
        {
            System.out.print("#"+row+":");
            
            for (int col=0;col < mytable.getColumnCount(); col++) 
            {
                String ValueString = mytable.getValueAt(row, col).toString();
                System.out.print("\t"+ValueString);
            }
            
            System.out.print("\n");
        }
        
    
    }
  
    /**
     * Small method to output a JTable object to the logger system (info)
     * EXPERIMENTAL!
     * @param mytable 
     */
    public static void PrintTableToLogger(JTable mytable){
        String LoggingString ="Table:\n";
        String ColumnString = "";
                
        for (int i=0; i < mytable.getColumnCount(); i++)
        {
            ColumnString = ColumnString + "\t" + mytable.getColumnName(i);
        }       
        logger.info(ColumnString);
        
        for (int row=0; row < mytable.getRowCount(); row++) 
        {
            LoggingString = LoggingString + "#"+ row+": ";
            
            for (int col=0;col < mytable.getColumnCount(); col++) 
            {
                String ValueString = mytable.getValueAt(row, col).toString();
                LoggingString = LoggingString +"\t"+ValueString;
            }
            
            LoggingString = LoggingString +"\n";
            
        }
        logger.info(LoggingString);
    
    }
    
    
    
}
