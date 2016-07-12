///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package de.kabasumo.lib_converttimeseriesobjects;
//
//import static de.kabasumo.lib_converttimeseriesobjects.Lib_JTableManager.ImportCSVtoTable;
//import static de.kabasumo.lib_converttimeseriesobjects.Lib_JTableManager.PrintTableToConsole;
//import eu.verdelhan.ta4j.Tick;
//import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
//import java.util.List;
//import javax.swing.JTable;
//import org.jfree.data.time.TimeSeries;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.text.NumberFormat;
//import java.text.ParseException;
//import java.util.Locale;
//import static org.junit.Assert.*;
//
//
///**
// *
// * @author bastianengelmann
// */
//public class Lib_ConvertTimeSeriesObjectsTest {
//    private JTable comdirect_bmw_daily;
//    private ClosePriceIndicator testindicator;
//    private eu.verdelhan.ta4j.TimeSeries testta4jtimeseries;
//    private TimeSeries testjfreetimeseries;
//    private String[] tableheaders = {"Datum","Er�ffnung","Hoch","Tief","Schluss","Volumen"};
//    private String[] columnmapping = {"Timestamps","Open","High","Low","Close","Volume"};
//    private eu.verdelhan.ta4j.TimeSeries data;
//
//    public Lib_ConvertTimeSeriesObjectsTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() {
//
//
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//    }
//
//    @Before
//    public void setUp() {
//
//        String SystemCharset="UTF-8"; //http://docs.oracle.com/javase/7/docs/technotes/guides/intl/encoding.doc.html
//        try{
//            comdirect_bmw_daily = ImportCSVtoTable("testfiles/comdirect_bmw_xetra_daily.csv",';',Charset.forName(SystemCharset));
//
//            Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//            testta4jtimeseries = CTSO.JTable_into_TA4JTimeseries(comdirect_bmw_daily, "TestSeries");
//
//            testindicator = new ClosePriceIndicator(testta4jtimeseries);
//
//            testjfreetimeseries = CTSO.JTable_into_JFreeChartTimeseries(comdirect_bmw_daily,"close","day","TestSeries");
//
//        }
//        catch (IOException e)
//        {
//            System.out.println("IOException:"+e);
//        }
//
//        PrintTableToConsole(comdirect_bmw_daily);
//
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    /**
//     * Test of CheckJTableCompatibility method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testCheckJTableCompatibility() {
//        System.out.println("CheckJTableCompatibility");
//        JTable importtable = comdirect_bmw_daily;
//        Boolean expResult = true;
//
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//        Boolean result = CTSO.CheckJTableCompatibility(importtable, "");
//        assertEquals(expResult, result);
//
//    }
//
//    /**
//     * Test of JTable_into_JFreeChartTimeseries method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testJTable_into_JFreeChartTimeseries() {
//        NumberFormat nf_intable = NumberFormat.getNumberInstance(new Locale("de","DE"));
//
//        System.out.println("JTable_into_JFreeChartTimeseries");
//        JTable DBTable = comdirect_bmw_daily;
//        String QuoteType = "close"; // take "close" as default
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//        double FrequencyResult = CTSO.FindDominantFrequency(comdirect_bmw_daily);
//        String QuoteFrequency = CTSO.GetFrequencyAsString(FrequencyResult);
//
//        String TimeseriesName = "TestTimeseries";
//
//        TimeSeries result = CTSO.JTable_into_JFreeChartTimeseries(DBTable, QuoteType, QuoteFrequency, TimeseriesName);
//
//        // Check equal length
//        assertEquals(DBTable.getRowCount(),result.getItemCount());
//
//        // Check default "close" and correct data matching
//        try
//        {
//            assertEquals(nf_intable.parse(DBTable.getValueAt(DBTable.getRowCount()-1, 4).toString()), result.getDataItem(0).getValue());
//        }
//        catch (ParseException e)
//        {
//            System.out.println("Parsing error: "+e);
//        }
//    }
//
//    /**
//     * Test of JTable_into_TA4JTicklist method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testJTable_into_TA4JTicklist() {
//        System.out.println("JTable_into_TA4JTicklist");
//        NumberFormat nf_intable = NumberFormat.getNumberInstance(new Locale("de","DE"));
//        JTable DBTable = comdirect_bmw_daily;
//
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//
//        List<Tick> result = CTSO.JTable_into_TA4JTicklist(DBTable);
//
//        // Check equal length
//        assertEquals(DBTable.getRowCount(),result.size());
//
//        // Check correct data matching
//        try
//        {
//            // Attention: Tick(org.joda.time.DateTime endTime, double openPrice, double highPrice, double lowPrice, double closePrice, double volume)
//            assertEquals(nf_intable.parse(DBTable.getValueAt(0, 4).toString()).doubleValue(), result.get(0).getClosePrice().toDouble(),0.0);
//            assertEquals(nf_intable.parse(DBTable.getValueAt(0, 1).toString()).doubleValue(), result.get(0).getOpenPrice().toDouble(),0.0);
//            assertEquals(nf_intable.parse(DBTable.getValueAt(0, 3).toString()).doubleValue(), result.get(0).getMinPrice().toDouble(),0.0);
//            assertEquals(nf_intable.parse(DBTable.getValueAt(0, 2).toString()).doubleValue(), result.get(0).getMaxPrice().toDouble(),0.0);
//            assertEquals(nf_intable.parse(DBTable.getValueAt(0, 5).toString()).doubleValue(), result.get(0).getVolume().toDouble(),0.0);
//        }
//        catch (ParseException e)
//        {
//            System.out.println("Parsing error: "+e);
//        }
//
//    }
//
//    /**
//     * Test of TA4JTicklist_into_TA4JTimeseries method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testTA4JTicklist_into_TA4JTimeseries() {
//
//        System.out.println("TA4JTicklist_into_TA4JTimeseries");
//
//        NumberFormat nf_intable = NumberFormat.getNumberInstance(new Locale("de","DE"));
//        JTable DBTable = comdirect_bmw_daily;
//
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//        List<Tick> ticklist = CTSO.JTable_into_TA4JTicklist(DBTable);
//        String TimeseriesName = "TestTA4JTimeseries";
//        eu.verdelhan.ta4j.TimeSeries result = CTSO.TA4JTicklist_into_TA4JTimeseries(ticklist, TimeseriesName);
//
//        // Check equal length
//        assertEquals(DBTable.getRowCount(),result.getTickCount());
//
//        // Check correct data matching
//        try
//        {
//            assertEquals(nf_intable.parse(DBTable.getValueAt(DBTable.getRowCount()-1, 4).toString()).doubleValue(), result.getLastTick().getClosePrice().toDouble(),0.01);
//        }
//        catch (ParseException e)
//        {
//            System.out.println("Parsing error: "+e);
//        }
//
//    }
//
//    /**
//     * Test of JFreeChartTimeseries_into_TA4JTimeseries method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testJFreeChartTimeseries_into_TA4JTimeseries() {
//        System.out.println("JFreeChartTimeseries_into_TA4JTimeseries");
//
//        NumberFormat nf_intable = NumberFormat.getNumberInstance(new Locale("de","DE"));
//        JTable DBTable = comdirect_bmw_daily;
//        String QuoteType = "close"; // take "close" as default
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//        double FrequencyResult = CTSO.FindDominantFrequency(comdirect_bmw_daily);
//        String QuoteFrequency = CTSO.GetFrequencyAsString(FrequencyResult);
//
//        String TimeseriesName = "TestTimeseries";
//
//        TimeSeries JFreeTimeseries = CTSO.JTable_into_JFreeChartTimeseries(DBTable, QuoteType, QuoteFrequency, TimeseriesName);
//
//
//        eu.verdelhan.ta4j.TimeSeries result = CTSO.JFreeChartTimeseries_into_TA4JTimeseries(JFreeTimeseries,"close");
//
//        // Check equal length
//        assertEquals(DBTable.getRowCount(),result.getTickCount());
//
//        // Check correct data matching
//        try
//        {
//            assertEquals(nf_intable.parse(DBTable.getValueAt(0, 4).toString()).doubleValue(), result.getLastTick().getClosePrice().toDouble(),0.01);
//        }
//        catch (ParseException e)
//        {
//            System.out.println("Parsing error: "+e);
//        }
//        System.out.println("Table last date:"+DBTable.getValueAt(0, 0).toString());
//        System.out.println("ta4j.TimeSeries last date begin:"+result.getLastTick().getBeginTime().toString());
//        System.out.println("ta4j.TimeSeries last date end:"+result.getLastTick().getEndTime().toString());
//    }
//
//    /**
//     * Test of JTable_into_TA4JTimeseries method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testJTable_into_TA4JTimeseries() {
//        System.out.println("JTable_into_TA4JTimeseries");
//
//        NumberFormat nf_intable = NumberFormat.getNumberInstance(new Locale("de","DE"));
//        JTable DBTable = comdirect_bmw_daily;
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//        String TimeseriesName = "TestTA4JTimeseries";
//
//        eu.verdelhan.ta4j.TimeSeries result = CTSO.JTable_into_TA4JTimeseries(DBTable, TimeseriesName);
//
//        // Check equal length
//        assertEquals(DBTable.getRowCount(),result.getTickCount());
//
//        // Check correct data matching
//        try
//        {
//            assertEquals(nf_intable.parse(DBTable.getValueAt(DBTable.getRowCount()-1, 4).toString()).doubleValue(), result.getLastTick().getClosePrice().toDouble(),0.01);
//        }
//        catch (ParseException e)
//        {
//            System.out.println("Parsing error: "+e);
//        }
//
//    }
//
//    /**
//     * Test of TA4JIndicator_into_JFreeChartTimeseries method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testTA4JIndicator_into_JFreeChartTimeseries_3args_1() {
//        System.out.println("TA4JIndicator_into_JFreeChartTimeseries - ta4jtimeseries");
//
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//
//        TimeSeries result = CTSO.TA4JIndicator_into_JFreeChartTimeseries(testta4jtimeseries, testindicator, "TestTimeseries");
//
//        NumberFormat nf_intable = NumberFormat.getNumberInstance(new Locale("de","DE"));
//
//
//        // Check equal length
//        assertEquals(comdirect_bmw_daily.getRowCount(),result.getItemCount());
//
//        // Check default "close" and correct data matching
//        try
//        {
//            assertEquals(nf_intable.parse(comdirect_bmw_daily .getValueAt(comdirect_bmw_daily.getRowCount()-1, 4).toString()), result.getDataItem(0).getValue());
//        }
//        catch (ParseException e)
//        {
//            System.out.println("Parsing error: "+e);
//        }
//
//    }
//
//    /**
//     * Test of TA4JIndicator_into_JFreeChartTimeseries method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testTA4JIndicator_into_JFreeChartTimeseries_3args_2() {
//        System.out.println("TA4JIndicator_into_JFreeChartTimeseries - JFreeTimeseries");
//
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//
//        TimeSeries result = CTSO.TA4JIndicator_into_JFreeChartTimeseries(testjfreetimeseries, testindicator, "TestTimeseries");
//
//        NumberFormat nf_intable = NumberFormat.getNumberInstance(new Locale("de","DE"));
//
//        // Check equal length
//        assertEquals(comdirect_bmw_daily.getRowCount(),result.getItemCount());
//
//        // Check default "close" and correct data matching
//        try
//        {
//            assertEquals(nf_intable.parse(comdirect_bmw_daily .getValueAt(0, 4).toString()), result.getDataItem(0).getValue());
//        }
//        catch (ParseException e)
//        {
//            System.out.println("Parsing error: "+e);
//        }
//    }
//
//    /**
//     * Test of FindDominantFrequency method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testFindDominantFrequency_JTable() {
//        System.out.println("FindDominantFrequency_JTable");
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//
//        double expResult = 104040.0;
//        double result = CTSO.FindDominantFrequency(comdirect_bmw_daily);
//        assertEquals(expResult, result, 0.0);
//
//    }
//
//    /**
//     * Test of FindDominantFrequency method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testFindDominantFrequency_ojdtTimeSeries() {
//        System.out.println("FindDominantFrequency");
//
//        JTable DBTable = comdirect_bmw_daily;
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//        String TimeseriesName = "TestJFreeTimeseries";
//
//        TimeSeries JFreeTimeseries = CTSO.JTable_into_JFreeChartTimeseries(DBTable,"close","day",TimeseriesName);
//
//        double expResult = 104040.0;
//        double result = CTSO.FindDominantFrequency(JFreeTimeseries);
//
//        assertEquals(expResult, result, 360.0);
//
//    }
//
//    /**
//     * Test of FindDominantFrequency method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testFindDominantFrequency_evtTimeSeries() {
//        System.out.println("FindDominantFrequency");
//
//        JTable DBTable = comdirect_bmw_daily;
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//        String TimeseriesName = "TestTA4JTimeseries";
//
//        eu.verdelhan.ta4j.TimeSeries ta4jseries = CTSO.JTable_into_TA4JTimeseries(DBTable, TimeseriesName);
//
//        double expResult = 104040.0;
//        double result = CTSO.FindDominantFrequency(ta4jseries);
//
//        assertEquals(expResult, result, 360.0);
//    }
//
//    /**
//     * Test of GetFrequencyAsString method, of class Lib_ConvertTimeSeriesObjects.
//     */
//    @Test
//    public void testGetFrequencyAsString() {
//        System.out.println("GetFrequencyAsString");
//        double FrequencySeconds = 846720.0; // 1.4 weeks
//        Lib_ConvertTimeSeriesObjects CTSO = new Lib_ConvertTimeSeriesObjects("dd.MM.yyyy","de","DE",tableheaders,columnmapping);
//        String expResult = "week";
//        String result = CTSO.GetFrequencyAsString(FrequencySeconds);
//        assertEquals(expResult, result);
//
//        FrequencySeconds = 104040.0;
//        expResult = "day";
//        result = CTSO.GetFrequencyAsString(FrequencySeconds);
//        assertEquals(expResult, result);
//    }
//
//}
