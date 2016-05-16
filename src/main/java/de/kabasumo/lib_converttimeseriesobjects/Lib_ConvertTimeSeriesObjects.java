
package de.kabasumo.lib_converttimeseriesobjects;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Tick;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import javax.swing.JTable;
import org.jfree.data.time.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class converts between different timeseries representations.
 * @author Bastian Engelmann
 */
public class Lib_ConvertTimeSeriesObjects {
    
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    
    public String DateParsePatterns[] = {
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd",
        "yyyy.MM.dd HH:mm:ss",
        "yyyy.MM.dd HH:mm",
        "yyyy.MM.dd",
        "dd-MM-yyyy HH:mm:ss",
        "dd-MM-yyyy HH:mm",
        "dd-MM-yyyy",
        "dd.MM.yyyy HH:mm:ss",
        "dd.MM.yyyy HH:mm",
        "dd.MM.yyyy"
    };
   
    public String DateParsePattern = "";
    
    // http://docs.oracle.com/javase/1.5.0/docs/guide/intl/locale.doc.html
    public String ImportLocaleLanguage=""; 
    public String ImportLocaleCountry=""; 
    public Locale ImportLocale;
    
    public List<String> NecessaryTableColumns;
    public List<String> AdjustedColumnMapping;
    
    /**
     * Standard constructor for "yyyy-MM-dd HH:mm:ss", "en", "US" and
     * table columns: "Timestamps","Open","Close","High","Low","Volume"
     */
    public Lib_ConvertTimeSeriesObjects()
    {
        DateParsePattern = DateParsePatterns[0];
        ImportLocaleLanguage="en";  //"de";
        ImportLocaleCountry="US";  //"DE";
        ImportLocale = new Locale(ImportLocaleLanguage,ImportLocaleCountry);
        // SQLite DB: {"Timestamps","Open","Close","High","Low","Volume"},
        // Comdirect: {"Datum","Er�ffnung","Hoch","Tief","Schluss","Volumen"}
        NecessaryTableColumns = new ArrayList<String>(Arrays.asList("Timestamps","Open","Close","High","Low","Volume"));
        AdjustedColumnMapping = new ArrayList<String>(Arrays.asList("Timestamps","Open","Close","High","Low","Volume")); // No adjustment for standard columns
        
    }
    
    /**
     * Use this constructor to specify DateParsePatterns, language and country for locale.
     * Example for choosing TableHeaders and ColumnMapping:
     * If your file has the following columns "Datum","Eröffnung","Hoch","Tief","Schluss","Volumen"
     * choose Tableheaders and ColumnMapping as following: To set ColumnMapping use
     * the standard columns list "Timestamps","Open","Close","High","Low","Volume" and sort them that it 
     * fits your use case e. g.  "Timestamps,"Open","High","Low","Close","Volume".
     * @param DatePattern see public DateParsePatterns[]
     * @param Language see http://docs.oracle.com/javase/1.5.0/docs/guide/intl/locale.doc.html
     * @param Country see http://docs.oracle.com/javase/1.5.0/docs/guide/intl/locale.doc.html
     * @param TableHeaders "Datum","Eröffnung","Hoch","Tief","Schluss","Volumen"
     * @param ColumnMapping "Timestamps,"Open","High","Low","Close","Volume"
     * @throws IllegalArgumentException 
     */
    public Lib_ConvertTimeSeriesObjects(String DatePattern, String Language, String Country, String[] TableHeaders, String[] ColumnMapping) throws IllegalArgumentException
    {
        if (DatePattern.isEmpty() || Language.isEmpty() || Country.isEmpty() || TableHeaders.length == 0) throw new IllegalArgumentException("One of the constructor's arguments is empty!");
        
        DateParsePattern = DatePattern;
        ImportLocaleLanguage=Language;
        ImportLocaleCountry=Country;
        ImportLocale = new Locale(Language,Country);
        NecessaryTableColumns = new ArrayList<String>(Arrays.asList(TableHeaders));
        AdjustedColumnMapping = new ArrayList<String>(Arrays.asList(ColumnMapping));
    }
    
    /** 
     * Checks:
     * - if table has the necessary number of coloumns
     * - if the necessary column names are existing
     * - if the date pattern for parsing is correct
     * - if table has the structure of NecessaryTableColumns[], e.g. "Timestamps,"Open","Close","High","Low","Volume".
     * Returns false if not
     * @param importtable
     * @param DatePattern Leave empty to take yyyy-MM-dd HH:mm:ss
     * @return true, false
     */
    public Boolean CheckJTableCompatibility(JTable importtable, String DatePattern)
    {
        if (DatePattern.isEmpty()) DatePattern = DateParsePattern;
        
        int NecessaryTableColumns_count = NecessaryTableColumns.size();
        
        // check number of columns
        //
        if (importtable.getColumnCount()!=NecessaryTableColumns_count)
        {
            //System.err.println("Column number is not matching between tables!");
            logger.error("Column number is not matching between tables!");
            return(false);
        }
        if (importtable.getColumnCount()!=6)
        {
            //System.err.println("Error: Recommended number of columns is 6!");
            logger.error("Error: Recommended number of columns is 6!");
        }
        
        // check DatePattern for parsing
        //
        String TestString = importtable.getValueAt(importtable.getRowCount()-1, 0).toString(); // Take element from last row of the table and first columns
        SimpleDateFormat parse_sdf = new SimpleDateFormat(DatePattern);
        try {
            if (parse_sdf.parse(TestString).toString().isEmpty())
            {
                //System.err.println("Error parsing dates (wrong DatePattern?)!");
                logger.error("Error parsing dates (wrong DatePattern?)!");
                return(false);
            }
            
        }
        catch (ParseException e) {
            //System.err.println("Error parsing dates (wrong DatePattern?)!");
            logger.error("Error parsing dates (wrong DatePattern?)!");
            return(false);
        }
                 
        // check parsing of double values with locale for last line and column nr 2 til end
        //
        // http://www.oracle.com/us/technologies/java/locale-140624.html
        
        int CountSuccess = 0;
        for (int k=1; k<NecessaryTableColumns_count;k++)
        {
            if(CheckDoubleValues(importtable.getValueAt(importtable.getRowCount()-1, k).toString()))
            {
                CountSuccess++;
            }
        }
        if (CountSuccess != (NecessaryTableColumns_count - 1))
        {
            //System.err.println("Error parsing double values (wrong locale?)!");
            logger.error("Error parsing double values (wrong locale?)!");
            return(false);
        }
                
        // check if columnnames are matching
        //
        int j=0;
        for (int i=0;i<NecessaryTableColumns_count;i++)
        {
            if (importtable.getColumnName(i).matches(NecessaryTableColumns.get(i)))
            {
                j++; // Count positive matches
            }
            else
            {
                //System.err.println("Column names of tables are not matching!");
                logger.error("Column names of tables are not matching!");
                return(false);
            }               
        }
        if (j == NecessaryTableColumns_count)
        {
            logger.info("Compatibility check for JTable was successful.");
            return (true);
        }
        else
        {
            //System.err.println("Only "+j+" column names out of "+NecessaryTableColumns_count+" are matching!");
            logger.error("Only {} column names out of {} are matching!",j,NecessaryTableColumns_count);
            return(false);
        }   
             
    }
    
    

    /**
     * Validates NumberFormat with ParsePosition.
     * For details (i.e. parsing integer) see http://www.ibm.com/developerworks/library/j-numberformat/. 
     * Returns 'true' if succeeded. 
     * @param sDouble
     * @return boolean
     */
    private boolean CheckDoubleValues(String sDouble)
    { 
        NumberFormat  nfDLocal = NumberFormat.getNumberInstance(ImportLocale); 
        ParsePosition pp = new ParsePosition(0);
        
        Number n;
        double d;

        pp.setIndex( 0 );
        n = nfDLocal.parse( sDouble, pp );

        if( sDouble.length() != pp.getIndex() || n == null )
        {
            //System.out.println( "Double Input Not Acceptable\n" + "\"" + sDouble + "\"");
            logger.error( "Double Input Not Acceptable: {}",sDouble);
            return false;
        }
        else
        {
            d = n.doubleValue();
            //System.out.println( "Double Accepted \n" + d );
            logger.info("Double Accepted: {}", d );
            return true;
        }

    } 
    

    /**
     * Converts JTable into JFreeChart timeseries.
     * Works with "end of day" = daily quotes until year accuracy quotes in table.
     * Leave QuoteType empty for default "close" quotes. 
     * Leave QuoteFrequency empty for default "end of day".
     * @param DBTable
     * @param QuoteType select one out of {"open","close","high","low"}
     * @param QuoteFrequency select one out of {"year","week","month","day","hour","second","minute"}
     * @param TimeseriesName 
     * @return 
     */
    public org.jfree.data.time.TimeSeries JTable_into_JFreeChartTimeseries(JTable DBTable, String QuoteType,String QuoteFrequency,String TimeseriesName)
    {
        // Number format
        NumberFormat nf_in = NumberFormat.getNumberInstance(ImportLocale);
        
        if (!CheckJTableCompatibility(DBTable, DateParsePattern))
        {
            return new org.jfree.data.time.TimeSeries(TimeseriesName);
        }

        org.jfree.data.time.TimeSeries ExportTS = new org.jfree.data.time.TimeSeries(TimeseriesName);
        
        for (int i=0; i < DBTable.getRowCount(); i++)
        {
            double value = 0.0;
            try
            {
                switch (QuoteType.toLowerCase())
                {

                    case "open":
                        value = nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Open")).toString()).doubleValue();
                        break;
                    case "close":
                        value = nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Close")).toString()).doubleValue();
                        break;
                    case "high":
                        value = nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("High")).toString()).doubleValue();
                        break;
                    case "low":
                        value = nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Low")).toString()).doubleValue();
                        break;
                    default:    
                        value = nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Close")).toString()).doubleValue();
                        break;
                }   
            }
            catch (ParseException e)
            {
                logger.error("Parsing error: {}",e);
                return new org.jfree.data.time.TimeSeries(TimeseriesName);
            }
            
            DateTime CalendarDate = DateTime.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Timestamps")).toString(), DateTimeFormat.forPattern(DateParsePattern));
            
            switch(QuoteFrequency.toLowerCase())
            {
                case "year":
                    ExportTS.add(new Year(CalendarDate.getYear()), value);
                    break;
                case "month":
                    ExportTS.add(new Month(CalendarDate.getMonthOfYear(),CalendarDate.getYear()), value);
                    break;
                case "week":
                    ExportTS.add(new Week(CalendarDate.getWeekOfWeekyear(),CalendarDate.getYear()), value);
                    break;    
                case "day":
                    ExportTS.add(new Day(CalendarDate.getDayOfMonth(),CalendarDate.getMonthOfYear(),CalendarDate.getYear()), value);
                    break;
                case "hour":
                    ExportTS.add(new Hour(CalendarDate.getHourOfDay(),CalendarDate.getDayOfMonth(),CalendarDate.getMonthOfYear(),CalendarDate.getYear()), value);
                    break;
                case "minute":
                    ExportTS.add(new Minute(CalendarDate.getMinuteOfHour(),CalendarDate.getHourOfDay(),CalendarDate.getDayOfMonth(),CalendarDate.getMonthOfYear(),CalendarDate.getYear()), value);
                    break;
                case "second":
                    ExportTS.add(new Second(CalendarDate.getSecondOfMinute(),CalendarDate.getMinuteOfHour(),CalendarDate.getHourOfDay(),CalendarDate.getDayOfMonth(),CalendarDate.getMonthOfYear(),CalendarDate.getYear()), value);
                    break;
                default:
                    ExportTS.add(new Day(CalendarDate.getDayOfMonth(),CalendarDate.getMonthOfYear(),CalendarDate.getYear()), value);
                    break;
              
            }
            
        }
        
        return ExportTS;
    
    }
    
    /**
     * Converts a JTable into a TA4J ticklist.
     * @param DBTable
     * @return 
     */
    public List<eu.verdelhan.ta4j.Tick> JTable_into_TA4JTicklist(JTable DBTable)
    {
        // Number format
        NumberFormat nf_in = NumberFormat.getNumberInstance(ImportLocale);
        
        if (!CheckJTableCompatibility(DBTable,DateParsePattern))
        {
            return new ArrayList<Tick>();
        }
        
        List<Tick> ExportTicklist = new ArrayList<Tick>();
        
        for (int i=0; i < DBTable.getRowCount(); i++)
        {
            
            DateTime CalendarDate = DateTime.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Timestamps")).toString(), DateTimeFormat.forPattern(DateParsePattern));
            
            // Attention: Tick(org.joda.time.DateTime endTime, double openPrice, double highPrice, double lowPrice, double closePrice, double volume)
            try
            {
                ExportTicklist.add(new Tick(CalendarDate,
                                        nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Open")).toString()).doubleValue(),
                                        nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("High")).toString()).doubleValue(),
                                        nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Low")).toString()).doubleValue(),
                                        nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Close")).toString()).doubleValue(),
                                        nf_in.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Volume")).toString()).doubleValue()));
            }
            catch (ParseException e)
            {
                logger.error("Parsing error: {}", e);
                return new ArrayList<Tick>();
            }
                    
        }
        
        return ExportTicklist;
    }
    
    /**
     * Converts TA4J List<Tick> into TA4J timeseries.
     * @param ImportTicklist
     * @param TimeseriesName
     * @return 
     */
    public eu.verdelhan.ta4j.TimeSeries TA4JTicklist_into_TA4JTimeseries(List<Tick> ImportTicklist, String TimeseriesName)
    {
        return new eu.verdelhan.ta4j.TimeSeries(TimeseriesName, ImportTicklist);    
    }
    
    /**
     * Converts a JFreeChart timeseries into a TA4J timeseries.
     * Attention: a JFreeChart timeseries has only one value for a date. It is saved as high,low, open and close quote.
     * A single JFreeChart timeseries only contains one price of the four prices of a common tick serie. 
     * Use ConversionMode to decide if all four prices will be set with the price 
     * from JFreeChart timeseries or only the close price. Volume is set to '0'.
     * @param JFreeTimeseries
     * @param ConversionMode "all","close"
     * @return eu.verdelhan.ta4j.TimeSeries
     */
    public eu.verdelhan.ta4j.TimeSeries JFreeChartTimeseries_into_TA4JTimeseries(org.jfree.data.time.TimeSeries JFreeTimeseries, String ConversionMode)
    {
        
        List<Tick> TA4JTicklist = new ArrayList<Tick>();
        
        for (int i=0; i < JFreeTimeseries.getItemCount(); i++)
        {

            org.joda.time.DateTime Endtime = new DateTime(JFreeTimeseries.getTimePeriod(i).getEnd());
            
            // Attention: Tick(org.joda.time.DateTime endTime, double openPrice, double highPrice, double lowPrice, double closePrice, double volume)
            switch (ConversionMode.toLowerCase())
            {
                case "all":
                    TA4JTicklist.add(new Tick(
                            Endtime,
                            JFreeTimeseries.getValue(i).doubleValue(),
                            JFreeTimeseries.getValue(i).doubleValue(),
                            JFreeTimeseries.getValue(i).doubleValue(),
                            JFreeTimeseries.getValue(i).doubleValue(),
                            0));
                    break;
                case "close":
                    TA4JTicklist.add(new Tick(
                            Endtime,
                            0,
                            0,
                            0,
                            JFreeTimeseries.getValue(i).doubleValue(),
                            0));
                    break;
                default:
                    TA4JTicklist.add(new Tick(
                            Endtime,
                            0,
                            0,
                            0,
                            JFreeTimeseries.getValue(i).doubleValue(),
                            0));
                    break;
                    
            }
                    
 
        }
                
                
        return new eu.verdelhan.ta4j.TimeSeries(JFreeTimeseries.getDescription(), TA4JTicklist);    
    }
    
    
    
    /**
     * Converts JTable into TA4J timeseries.
     * @param DBTable
     * @param TimeseriesName
     * @return 
     */
    public eu.verdelhan.ta4j.TimeSeries JTable_into_TA4JTimeseries(JTable DBTable, String TimeseriesName)
    {
        List<Tick> ImportTicklist = new ArrayList<Tick>();
        ImportTicklist = JTable_into_TA4JTicklist(DBTable);
        
        return new eu.verdelhan.ta4j.TimeSeries(TimeseriesName, ImportTicklist);    
    }
    
    
    
    /**
     * Builds a JFreeChart timeseries from a Ta4j timeseries and an TA4J indicator.
     * @param TA4JTimeseries the ta4j timeseries
     * @param TA4JIndicator the ta4j indicator
     * @param TimeseriesName the name of the chart time series
     * @return the JFreeChart timeseries
     */
    public org.jfree.data.time.TimeSeries TA4JIndicator_into_JFreeChartTimeseries(eu.verdelhan.ta4j.TimeSeries TA4JTimeseries, Indicator<Decimal> TA4JIndicator, String TimeseriesName) 
    {
        org.jfree.data.time.TimeSeries JFreeChartTimeSeries = new org.jfree.data.time.TimeSeries(TimeseriesName);
        double TA4JTimeseriesFrequency = FindDominantFrequency(TA4JTimeseries);
        String QuoteFrequency = GetFrequencyAsString(TA4JTimeseriesFrequency);
        
        for (int i = 0; i < TA4JTimeseries.getTickCount(); i++) 
        {
            Tick tick = TA4JTimeseries.getTick(i);
            double value = TA4JIndicator.getValue(i).toDouble();
            
            switch(QuoteFrequency)
            {
                case "year":
                    JFreeChartTimeSeries.add(new Year(tick.getEndTime().getYear()), value);
                    break;
                case "month":
                    JFreeChartTimeSeries.add(new Month(tick.getEndTime().getMonthOfYear(),tick.getEndTime().getYear()), value);
                    break;
                case "week":
                    JFreeChartTimeSeries.add(new Week(tick.getEndTime().getWeekOfWeekyear(),tick.getEndTime().getYear()), value);
                    break;    
                case "day":
                    JFreeChartTimeSeries.add(new Day(tick.getEndTime().getDayOfMonth(),tick.getEndTime().getMonthOfYear(),tick.getEndTime().getYear()), value);
                    break;
                case "hour":
                    JFreeChartTimeSeries.add(new Hour(tick.getEndTime().getHourOfDay(),tick.getEndTime().getDayOfMonth(),tick.getEndTime().getMonthOfYear(),tick.getEndTime().getYear()), value);
                    break;
                case "minute":
                    JFreeChartTimeSeries.add(new Minute(tick.getEndTime().getMinuteOfHour(),tick.getEndTime().getHourOfDay(),tick.getEndTime().getDayOfMonth(),tick.getEndTime().getMonthOfYear(),tick.getEndTime().getYear()), value);
                    break;
                case "second":
                    JFreeChartTimeSeries.add(new Second(tick.getEndTime().getSecondOfMinute(),tick.getEndTime().getMinuteOfHour(),tick.getEndTime().getHourOfDay(),tick.getEndTime().getDayOfMonth(),tick.getEndTime().getMonthOfYear(),tick.getEndTime().getYear()), value);
                    break;
                default:
                    JFreeChartTimeSeries.add(new Day(tick.getEndTime().getDayOfMonth(),tick.getEndTime().getMonthOfYear(),tick.getEndTime().getYear()), value);
                    break;
              
            }
            
        }
        return JFreeChartTimeSeries;
    }
    
    /**
     * Builds a JFreeChart timeseries from a JFreeChart timeseries and an TA4J indicator.
     * @param JFreeTimeseries the JFreeChart timeseries
     * @param TA4JIndicator the TA4JIndicator
     * @param TimeseriesName the name of the JFreeChart timeseries
     * @return the JFreeChart timeseries
     */
    public org.jfree.data.time.TimeSeries TA4JIndicator_into_JFreeChartTimeseries(org.jfree.data.time.TimeSeries JFreeTimeseries, Indicator<Decimal> TA4JIndicator, String TimeseriesName) 
    {
        org.jfree.data.time.TimeSeries JFreeChartTimeSeries = new org.jfree.data.time.TimeSeries(TimeseriesName);
        for (int i = 0; i < JFreeTimeseries.getItemCount(); i++) {
            
            JFreeChartTimeSeries.add(JFreeTimeseries.getTimePeriod(i), TA4JIndicator.getValue(i).toDouble());
        }
        return JFreeChartTimeSeries;
    }
    
    /**
     * Creates ArrayList of durations from TA4J timeseries.
     * @param TA4JTimeseries
     * @return ArrayList <org.joda.time.Duration>
     */
    private ArrayList <org.joda.time.Duration> FindPeriodDurations (eu.verdelhan.ta4j.TimeSeries TA4JTimeseries)
    {
        ArrayList <org.joda.time.Duration> PeriodDurations = new ArrayList<org.joda.time.Duration>();
        
        int TickCount = TA4JTimeseries.getTickCount();
        for (int i = 0; i < TickCount; i++) 
        {
            long TSPeriod = 0;
            Tick RecentTick = TA4JTimeseries.getTick(i);
            if (i < TickCount - 1)
            {    
                Tick NextTick = TA4JTimeseries.getTick(i+1);
                TSPeriod = RecentTick.getBeginTime().getMillis() - NextTick.getBeginTime().getMillis();
            }
            else
            {
                TSPeriod = (RecentTick.getEndTime().getMillis()-RecentTick.getBeginTime().getMillis());
            }
            
            PeriodDurations.add(new org.joda.time.Duration(TSPeriod));
        }
        return PeriodDurations;
    }
    
    /**
     * Creates ArrayList of durations from JFreeChart timeseries.
     * @param JFreeTimeseries
     * @return ArrayList <org.joda.time.Duration>
     */
    private ArrayList <org.joda.time.Duration> FindPeriodDurations(org.jfree.data.time.TimeSeries JFreeTimeseries)
    {
        ArrayList <org.joda.time.Duration> PeriodDurations = new ArrayList<org.joda.time.Duration>();
        
        int TickCount = JFreeTimeseries.getItemCount();        
        for (int i = 0; i < TickCount; i++) 
        {
            long TSPeriod = 0;
            
            if (i < TickCount - 1)
            {
            
                TSPeriod = JFreeTimeseries.getDataItem(i+1).getPeriod().getFirstMillisecond() - JFreeTimeseries.getDataItem(i).getPeriod().getFirstMillisecond();
                
            }
            else
            {
                TSPeriod = JFreeTimeseries.getDataItem(i).getPeriod().getLastMillisecond() - JFreeTimeseries.getDataItem(i).getPeriod().getFirstMillisecond();
            }
            
            PeriodDurations.add(new org.joda.time.Duration(TSPeriod));
        }
        
        
        return PeriodDurations;
    }
    
    /**
     * Creates ArrayList of durations from JTable tick series.
     * @param JFreeTimeseries
     * @return ArrayList <org.joda.time.Duration>
     */
    private ArrayList <org.joda.time.Duration> FindPeriodDurations(JTable DBTable)
    {
        ArrayList <org.joda.time.Duration> PeriodDurations = new ArrayList<org.joda.time.Duration>();
        
                
        for (int i = 1; i < DBTable.getRowCount(); i++) 
        {
            // If JTable has 'n' number of ticks, then the list of durations 
            // has 'n-1' elements 
            DateTime CalendarDate0 = DateTime.parse(DBTable.getValueAt(i, AdjustedColumnMapping.indexOf("Timestamps")).toString(), DateTimeFormat.forPattern(DateParsePattern));
            DateTime CalendarDate1 = DateTime.parse(DBTable.getValueAt(i-1, AdjustedColumnMapping.indexOf("Timestamps")).toString(), DateTimeFormat.forPattern(DateParsePattern));
            long TSPeriod = CalendarDate1.getMillis() - CalendarDate0.getMillis();
            PeriodDurations.add(new org.joda.time.Duration(TSPeriod));
        }
        
        
        return PeriodDurations;
    }
    
    /**
     * Find the dominant frequency in a timeseries signal of type "JFreeTimeseries".
     * @param JFreeTimeseries
     * @return double DominantFrequency
     */ 
    public double FindDominantFrequency(org.jfree.data.time.TimeSeries JFreeTimeseries)
    {
        ArrayList <org.joda.time.Duration> PeriodDurations = new ArrayList<org.joda.time.Duration>();
        PeriodDurations = FindPeriodDurations(JFreeTimeseries);
        
        return CalculateDominantFrequency(PeriodDurations);
    }
    
    /**
     * Find the dominant frequency in a timeseries signal of type "TA4JTimeseries".
     * @param TA4JTimeseries
     * @return double DominantFrequency
     */
    public double FindDominantFrequency(eu.verdelhan.ta4j.TimeSeries TA4JTimeseries)
    {
        ArrayList <org.joda.time.Duration> PeriodDurations = new ArrayList<org.joda.time.Duration>();
        PeriodDurations = FindPeriodDurations(TA4JTimeseries);
        
        return CalculateDominantFrequency(PeriodDurations);
    }
    
    /**
     * Find the dominant frequency in a tickseries of type JTable.
     * @param TA4JTimeseries
     * @return double DominantFrequency
     */
    public double FindDominantFrequency(JTable DBTable)
    {
        ArrayList <org.joda.time.Duration> PeriodDurations = new ArrayList<org.joda.time.Duration>();
        PeriodDurations = FindPeriodDurations(DBTable);
        
        return CalculateDominantFrequency(PeriodDurations);
    }
    
    /**
     * Calculate the frequency in an arraylist of durations.
     * Be carefull: method works only until "year" frequency because of limit of type "double" in the Apache Commons' histogram method
     * @param ArrayList <org.joda.time.Duration> PeriodDurations
     * @return double
     */
    private double CalculateDominantFrequency (ArrayList <org.joda.time.Duration> PeriodDurations)
    {
        
        int PDListSize = PeriodDurations.size();
        
        // Estimate bin count by sqrt(n)     
        BigDecimal bd = new BigDecimal(Math.sqrt(PDListSize));
        bd = bd.setScale(0, RoundingMode.HALF_UP);
        final int bincount = bd.intValue();
         
        // Copy ArrayList into array
        double[] data = new double[PDListSize];
        for (int j=0;j< PDListSize;j++)
        {
            data[j] = PeriodDurations.get(j).getMillis()/1000; // Convert to seconds because of limit of type double
        }
        
        // Establish histogram
        long[] histogram = new long[bincount];
        
        org.apache.commons.math3.random.EmpiricalDistribution distribution = new org.apache.commons.math3.random.EmpiricalDistribution(bincount);
        distribution.load(data);
        
        int k = 0;
        long histogramMaxValue = 0;
        int histogramMaxIndex = 0;
        for(org.apache.commons.math3.stat.descriptive.SummaryStatistics stats: distribution.getBinStats())
        {
            long getNumbers = stats.getN();
            histogram[k] = getNumbers;
            
            if (getNumbers>histogramMaxValue)
            {
                histogramMaxValue = getNumbers;
                histogramMaxIndex = k;
            }
            k++;
            
        }
        
        double[] bounds = distribution.getUpperBounds();
        
        for (int l=0;l<k;l++)
        {
            System.out.println("BinNr. ["+l+"]"+" Frequency: "+histogram[l]+ " Upperbound: "+bounds[l]);
            logger.debug("BinNr. [{}] Frequency: {} Upperbound: {}",l,histogram[l],bounds[l]);
        }

        System.out.println("\n[Max]\nBinNr. ["+ histogramMaxIndex +"] Frequency: "+histogramMaxValue+ " Upperbound: "+bounds[histogramMaxIndex]);
        logger.debug("\n[Max]\nBinNr. [{}] Frequency: {} Upperbound: {}",histogramMaxIndex,histogramMaxValue,bounds[histogramMaxIndex]);
        
        return bounds[histogramMaxIndex];
    }
    
    /**
     * Returns frequency as a string. 
     * Be carefull: method works only until "years". Returns empty string when year accuracy is exceeded
     * @param FrequencySeconds
     * @return FrequencyString
     */
    public String GetFrequencyAsString (double FrequencySeconds)
    {
        
        // second: 1000
        // minute: 60000            
        // hour 3600000
        // day: 86400000
        // 7day week: 604800000
        // 31day month: 2678400000
        // 365day year: 31536000000
    
        if ((FrequencySeconds>0)&&(FrequencySeconds<=60)) return ("second");
        if ((FrequencySeconds>60)&&(FrequencySeconds<=3600)) return ("minute");
        if ((FrequencySeconds>3600)&&(FrequencySeconds<=86400)) return ("hour");
        if ((FrequencySeconds>86400)&&(FrequencySeconds<=604800)) return ("day");
        if ((FrequencySeconds>604800)&&(FrequencySeconds<=2678400)) return ("week");
        if ((FrequencySeconds>2678400)&&(FrequencySeconds<=31536000)) return ("month");
        if ((FrequencySeconds>31536000)) return ("month");
        else return ("");
    }
}
