package org.iiitb.DataAnonymizer.services;

import javax.swing.filechooser.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import javax.xml.parsers.*;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataSource;

import java.nio.charset.Charset;
import java.sql.*;
import java.text.DecimalFormat;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.criteria.*;
import org.deidentifier.arx.metric.Metric;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.apache.commons.csv.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.json.*;
@SuppressWarnings("unused")
@Path("/anonymizer")
public class Anonymizer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2218891792103799574L;
	/**
	 * 
	 */
	private DataSource input_source;
	public String input_format = null;
	
	// Default Path files
	private static String path = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
	private String fileSource_Path = path+"/Anonymization_resources/DataSources/";
	private String configSource_Path = path+"/Anonymization_resources/Configurations/";
	private String heirarchySource_Path = path+"/Anonymization_resources/Heirarchies/";
	private String outputSource_Path = path+"/Anonymization_resources/results/";

	public DataDefinition def = null;
	public Data data = null;
	
	// Details of user
	
	private String username=null;
	private String password=null;
	
	@POST
	@Path("/signin")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String signin(String text) throws IOException
	{		
		path = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		File dir = new File(path+"/Anonymization_resources");
		if(!dir.exists())
		{
			new File(path+"/Anonymization_resources").mkdir();
			new File(path+"/Anonymization_resources/DataSources").mkdir();
			new File(path+"/Anonymization_resources/Configurations").mkdir();
			new File(path+"/Anonymization_resources/Heirarchhies").mkdir();
			new File(path+"/Anonymization_resources/results").mkdir();
			
		}
		else
		{
			System.out.println("Directory Exists");
		}
		try
		{
			JSONObject jsonObject=new JSONObject(text);
			username=jsonObject.getString("Username");
	        password=jsonObject.getString("Password");
	        System.out.println(username + " ,"+ password);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	    if(username.equals("admin@iiitb") && password.equals("admin"))
		{
			return "success";
		}
		else
		{
			return "fail";
		}
  	
	}
	
	//API to get the list of all available data sources of user
	
	@GET
	@Path("/getSources")
	@Produces(MediaType.TEXT_PLAIN)
	public String getSources() throws JSONException
	{
		path = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		
		String directory_name = path+"/Anonymization_resources/DataSources/";

		JSONObject data = new JSONObject();
		
		File sourceDirectory = new File(directory_name);
		JSONArray csvarray=new JSONArray();
		JSONArray xlsarray=new JSONArray();
		File[] flist = sourceDirectory.listFiles();
		for (File file : flist){
            if (file.isFile()){
          
            	String fname = file.getName();
            	String[] names = fname.split("\\.");
            	
            		
            		if(names[1].equals("csv"))
            		{
            			 csvarray.put(fname);
            		}
            		else if(names[1].equals("xls"))
            		{
            			xlsarray.put(fname);
            		}
            	
            }
        }
		data.put("csv", csvarray);
		data.put("xls", xlsarray);
		return data.toString();
	}
	
	// API to get all the available configurations of user
	
	@GET
	@Path("/getConfigurations")
	@Produces(MediaType.TEXT_PLAIN)
	public String getConfigurations() throws JSONException
	{
		path = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		
		String directory_name = path+"/Anonymization_resources/Configurations/";
		

		JSONObject data = new JSONObject();
		
		File sourceDirectory = new File(directory_name);
		JSONArray xmlarray=new JSONArray();
		File[] flist = sourceDirectory.listFiles();
		for (File file : flist){
            if (file.isFile()){
          
            	String fname = file.getName();
            	String[] names = fname.split("\\.");
            	
            		
            		if(names[1].equals("xml"))
            		{
            			 xmlarray.put(fname);
            		}
            		
            	
            }
        }
		data.put("xml", xmlarray);
		return data.toString();

	}
	
	// API to upoad the data source file to the server
	
	@POST
	@Path("/uploadSource")
	@Consumes({MediaType.MULTIPART_FORM_DATA})	
	public Response uploadSource(  @FormDataParam("file") InputStream fileInputStream,
	                                @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
	{
		path = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		
	    String UPLOAD_PATH = path+"/Anonymization_resources/DataSources/";
	    try
	    {
	    	System.out.println(fileMetaData.getFileName());
	        int read = 0;
	        byte[] bytes = new byte[1024];
	 
	        OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + fileMetaData.getFileName()));
	    	System.out.println(UPLOAD_PATH+fileMetaData.getFileName());
	        while ((read = fileInputStream.read(bytes)) != -1)
	        {
	            out.write(bytes, 0, read);
	        }
	        out.flush();
	        out.close();
	    } catch (IOException e)
	    {
	        throw new WebApplicationException("Error while uploading file. Please try again !!");
	    }
	    return Response.ok("Data uploaded successfully !!").build();
	}
	
	// API to upload the configuration file to the server
	
	@POST
	@Path("/uploadConfig")
	@Consumes({MediaType.MULTIPART_FORM_DATA})	
	public Response uploadConfig(  @FormDataParam("file") InputStream fileInputStream,
	                                @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
	{
		path = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		
	    String UPLOAD_PATH = path+"/Anonymization_resources/Configurations/";
	    try
	    {
	    	System.out.println(fileMetaData.getFileName());
	        int read = 0;
	        byte[] bytes = new byte[1024];
	 
	        OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + fileMetaData.getFileName()));
	    	System.out.println(UPLOAD_PATH+fileMetaData.getFileName());
	        while ((read = fileInputStream.read(bytes)) != -1)
	        {
	            out.write(bytes, 0, read);
	        }
	        out.flush();
	        out.close();
	    } catch (IOException e)
	    {
	        throw new WebApplicationException("Error while uploading file. Please try again !!");
	    }
	    return Response.ok("Data uploaded successfully !!").build();
	}
	
	// API to create a new configuration
	@POST
	@Path("/createConfig")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String createConfig(String text)
	{
		path = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
	
		try 
		{
			String UPLOAD_PATH = path+"/Anonymization_resources/Configurations/";
			JSONObject data = new JSONObject(text);
			String file_name = data.getString("file_name");
			data.remove("file_name");
			JSONArray attr_list = data.getJSONArray("attributes");
			JSONObject privacy_model = data.getJSONObject("privacy_data");
			java.nio.file.Path path = Paths.get(UPLOAD_PATH+ file_name);
			try (BufferedWriter writer = Files.newBufferedWriter(path))
			{
			    writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
			    		"<config\n" + 
			    		"	xmlns=\"http://www.iiitb.ac.in/config\"\n" + 
			    		" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
			    		" xsi:schemaLocation=\"http://www.iiitb.ac.in/config config.xsd\">\n" + 
			    		"");
			    writer.write("<AttributeData>");
			    for(int i=0;i<attr_list.length();i++)
			    {
			    	JSONObject attribute = attr_list.getJSONObject(i);
			    	writer.write("<Attribute attribute_name = \"" + attribute.getString("attribute_name") +"\">\n" + 
			    			"		<AttributeType>"+ attribute.getString("attributeType")+"</AttributeType>\n" + 
			    			"		<DataType>"+attribute.getString("DataType")+"</DataType>\n" + 
			    			"	</Attribute>");
			    }
			    writer.write("</AttributeData>");
			    writer.write("<PrivacyModel>\n" + 
			    		"	<Model k=\""+privacy_model.getString("k")+"\">"+privacy_model.getString("model")+"</Model>\n" + 
			    		"	<SurpressionRate>"+privacy_model.getString("rate")+"</SurpressionRate>\n" + 
			    		"</PrivacyModel>\n" + 
			    		"");
			   	writer.write("</config>");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "file uploaded successfully";
	}
	
	@POST
	@Path("/uploadHeirarchy")
	@Consumes({MediaType.MULTIPART_FORM_DATA})	
	public Response uploadHeirarchy(@FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception
	{
		path = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		
		String UPLOAD_PATH  =path+"/Anonymization_resources/Heirarchies/";
		 try
		    {
		    	System.out.println(fileMetaData.getFileName());
		        int read = 0;
		        byte[] bytes = new byte[1024];
		 
		        OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + fileMetaData.getFileName()));
		    	System.out.println(UPLOAD_PATH+fileMetaData.getFileName());
		        while ((read = fileInputStream.read(bytes)) != -1)
		        {
		            out.write(bytes, 0, read);
		        }
		        out.flush();
		        out.close();
		    } catch (IOException e)
		    {
		        throw new WebApplicationException("Error while uploading file. Please try again !!");
		    }
		    return Response.ok("Data uploaded successfully !!").build();
		    
	}

	@POST
	@Path("/getColumns")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String getColumns(String request)
	{
		JSONObject attr_data = new JSONObject();
		try
		{
			JSONObject data=new JSONObject(request);
			String filename = data.getString("source_file");
			String file_path  = fileSource_Path + filename;
			CSVParser csvParser=null;
			Reader reader = Files.newBufferedReader(Paths.get(file_path));
			if(filename.equals("adult.csv"))
			{
				 csvParser = new CSVParser(reader, CSVFormat.DEFAULT
			
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withDelimiter(';'));
			}
			else
			{
				csvParser = new CSVParser(reader, CSVFormat.DEFAULT
						
	                    .withFirstRecordAsHeader()
	                    .withIgnoreHeaderCase()
	                    .withTrim());
			}
			Map<String, Integer> header = csvParser.getHeaderMap();
			Iterator it = header.keySet().iterator();
			csvParser.close();
			JSONArray arr = new JSONArray();
	        while(it.hasNext())
	        {
	        	String key = (String) it.next();
	        	arr.put(key);
	        }
	        attr_data.put("attributes", arr);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return attr_data.toString();
	}
	
	@GET
	@Path("/download")
	public Response download()
	{
		path = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
		
		StreamingOutput fileStream =  new StreamingOutput()
        {
            @Override
            public void write(java.io.OutputStream output) throws IOException, WebApplicationException
            {
                try
                {
                	String directory_name = path+"/Anonymization_resources/results/output.csv";

                	
                    java.nio.file.Path path = Paths.get(directory_name);
                    byte[] data = Files.readAllBytes(path);
                    output.write(data);
                    output.flush();
                }
                catch (Exception e)
                {
                    throw new WebApplicationException("File Not Found !!");
                }
            }
        };
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition","attachment; filename = output.csv")
                .build();
    }

	
	//API to anonymize the given data file and configuration
	@POST
	@Path("/anonymize")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String anonymize(String req) throws IOException {

		try {
			JSONObject obj  = new JSONObject(req);
			String sourceType = obj.getString("sourceType");
			String configuration  = obj.getString("configuration");
			String heirarchy_path;
			if(sourceType.equalsIgnoreCase("csv"))
			{
				String datasource = obj.getString("datasource");
				String data_path = fileSource_Path + datasource;
				String inputs[]= datasource.split("\\.");
				String input_format = inputs[1];
				heirarchy_path = heirarchySource_Path + inputs[0] +"_hierarchy_";
				File file = new File(data_path);
			
				// Creating the respective datasource object
				if(input_format.equalsIgnoreCase("csv"))
					input_source = DataSource.createCSVSource(file, Charset.defaultCharset(),';',true);
				else
					input_source = DataSource.createExcelSource(file, ';',true);
			}
			else 
			{
				JSONObject jdbcData = obj.getJSONObject("jdbc_info");
				String Username = jdbcData.getString("username");
				String Password =jdbcData.getString("password");
				String url = jdbcData.getString("url");
				String table = jdbcData.getString("table");
				heirarchy_path = heirarchySource_Path + table +"_hierarchy_";
				
				input_source = DataSource.createJDBCSource(url, Username, Password, table);
			}
			String Config_file = configSource_Path + configuration;
			
			
			// Reading the config file to import attributes and their types to datasource
			
			File configFile = new File(Config_file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
			Document doc = dbBuilder.parse(configFile);
			doc.getDocumentElement().normalize();// Normalize the root element
			System.out.println("Root : " + doc.getDocumentElement().getNodeName());

			NodeList att_data = doc.getElementsByTagName("AttributeData");
			Node att = att_data.item(0);
			Element eAtt_data = (Element) att;
			NodeList att_list = eAtt_data.getElementsByTagName("Attribute");

			// adding attributes to datasource
			for (int i = 0; i < att_list.getLength(); i++) {
				Node att_node = att_list.item(i);
				System.out.println("attribute :" + att_node.getAttributes().item(0).getNodeValue());
				input_source.addColumn(att_node.getAttributes().item(0).getNodeValue());
			}
			
			//Creating data object
			
			data = Data.create(input_source);
			// print(data.getHandle());

			def = data.getDefinition();

			
			
			// Setting up attribute types to data and adding heirarches to attributes
			String mPath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();	
			for (int i = 0; i < att_list.getLength(); i++) {
				Node att_node = att_list.item(i);
				Element mnode = (Element) att_node;
				String attribute = att_node.getAttributes().item(0).getNodeValue();
				NodeList type_list = mnode.getElementsByTagName("AttributeType");
				String att_type = type_list.item(0).getTextContent();
				if (att_type.equals("QUASI_IDENTIFYING")) 
				{	
					String h_path = heirarchy_path + attribute + ".csv";
					File f = new File(h_path);
					Hierarchy heirarchy=null;
					if(f.exists())
						heirarchy = Hierarchy.create(h_path, Charset.defaultCharset(), ';');
				
					def.setAttributeType(attribute, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);
					if(f.exists())
						def.setAttributeType(attribute, heirarchy);
				}
				else if (att_type.equals("IDENTIFYING"))
					def.setAttributeType(attribute, AttributeType.IDENTIFYING_ATTRIBUTE);
				else if (att_type.equals("SENSITIVE"))
					def.setAttributeType(attribute, AttributeType.SENSITIVE_ATTRIBUTE);
				else
					def.setAttributeType(attribute, AttributeType.INSENSITIVE_ATTRIBUTE);

				
			}
			
			// Importing the privacy criterion
			
			NodeList pmodel_data = doc.getElementsByTagName("PrivacyModel");
			Node pmodel = pmodel_data.item(0);
			Element eP_model = (Element) pmodel;
			NodeList model_list = eP_model.getElementsByTagName("Model");
			NodeList surpression_list = eP_model.getElementsByTagName("SurpressionRate");

			String model = model_list.item(0).getTextContent();
			String surpressionrate = surpression_list.item(0).getTextContent();
			
			// Initialising ARX configurator
			
			ARXConfiguration config = ARXConfiguration.create();
			
			if (model.equals("KAnonymity")) {
				String k = model_list.item(0).getAttributes().item(0).getNodeValue();
				config.addPrivacyModel(new KAnonymity(Integer.parseInt(k)));
			}
			else {
				return "Supports only k-anonymity, please modify privacy model and try again";
			}
			config.setSuppressionLimit(Double.parseDouble(surpressionrate));

			System.out.println("model :" + model + " rate: " + Double.parseDouble(surpressionrate));
			
			// Create an instance of the anonymizer
			
			ARXAnonymizer anonymizer = new ARXAnonymizer();

			config.setQualityModel(Metric.createLossMetric());

			// anonymize!!!!!
			ARXResult result = anonymizer.anonymize(data, config);

			DataHandle out_view = result.getOutput();
			printResult(result, data);
			
			out_view.save(outputSource_Path + "output.csv", ',');
			
			
			return "success";

		} 
		catch (Exception e) {
			e.printStackTrace();
			return " error, check and try again";
		}

		// Print input
		/*
		 * System.out.println(" - Input data:"); print(data.getHandle().iterator());
		 * 
		 * 
		 * // Print info printResult(result, data);
		 * 
		 * // Print results System.out.println(" - Transformed data:");
		 * print(result.getOutput(false).iterator());
		 * 
		 * 
		 */
		

	}

	/**
	 * Prints a given data handle.
	 *
	 * @param handle
	 */
	protected static void print(DataHandle handle) {
		final Iterator<String[]> itHandle = handle.iterator();
		print(itHandle);
	}

	/**
	 * Prints a given iterator.
	 *
	 * @param iterator
	 */
	protected static void print(Iterator<String[]> iterator) {
		while (iterator.hasNext()) {
			System.out.print("   ");
			System.out.println(Arrays.toString(iterator.next()));
		}
	}

	/**
	 * Prints java array.
	 *
	 * @param array
	 */
	protected static void printArray(String[][] array) {
		System.out.print("{");
		for (int j = 0; j < array.length; j++) {
			String[] next = array[j];
			System.out.print("{");
			for (int i = 0; i < next.length; i++) {
				String string = next[i];
				System.out.print("\"" + string + "\"");
				if (i < next.length - 1) {
					System.out.print(",");
				}
			}
			System.out.print("}");
			if (j < array.length - 1) {
				System.out.print(",\n");
			}
		}
		System.out.println("}");
	}

	/**
	 * Prints a given data handle.
	 *
	 * @param handle
	 */
	protected static void printHandle(DataHandle handle) {
		final Iterator<String[]> itHandle = handle.iterator();
		printIterator(itHandle);
	}

	/**
	 * Prints java array.
	 *
	 * @param iterator
	 */
	protected static void printIterator(Iterator<String[]> iterator) {
		while (iterator.hasNext()) {
			String[] next = iterator.next();
			System.out.print("[");
			for (int i = 0; i < next.length; i++) {
				String string = next[i];
				System.out.print(string);
				if (i < next.length - 1) {
					System.out.print(", ");
				}
			}
			System.out.println("]");
		}
	}

	/**
	 * Prints the result.
	 *
	 * @param result
	 * @param data
	 */
	protected static void printResult(final ARXResult result, final Data data) {

		// Print time
		final DecimalFormat df1 = new DecimalFormat("#####0.00");
		final String sTotal = df1.format(result.getTime() / 1000d) + "s";
		System.out.println(" - Time needed: " + sTotal);

		// Extract
		final ARXNode optimum = result.getGlobalOptimum();
		final List<String> qis = new ArrayList<String>(data.getDefinition().getQuasiIdentifyingAttributes());

		if (optimum == null) {
			System.out.println(" - No solution found!");
			return;
		}

		// Initialize
		final StringBuffer[] identifiers = new StringBuffer[qis.size()];
		final StringBuffer[] generalizations = new StringBuffer[qis.size()];
		int lengthI = 0;
		int lengthG = 0;
		for (int i = 0; i < qis.size(); i++) {
			identifiers[i] = new StringBuffer();
			generalizations[i] = new StringBuffer();
			identifiers[i].append(qis.get(i));
			generalizations[i].append(optimum.getGeneralization(qis.get(i)));
			if (data.getDefinition().isHierarchyAvailable(qis.get(i)))
				generalizations[i].append("/").append(data.getDefinition().getHierarchy(qis.get(i))[0].length - 1);
			lengthI = Math.max(lengthI, identifiers[i].length());
			lengthG = Math.max(lengthG, generalizations[i].length());
		}

		// Padding
		for (int i = 0; i < qis.size(); i++) {
			while (identifiers[i].length() < lengthI) {
				identifiers[i].append(" ");
			}
			while (generalizations[i].length() < lengthG) {
				generalizations[i].insert(0, " ");
			}
		}

		// Print
		System.out.println(" - Information loss: " + result.getGlobalOptimum().getLowestScore() + " / "
				+ result.getGlobalOptimum().getHighestScore());
		System.out.println(" - Optimal generalization");
		for (int i = 0; i < qis.size(); i++) {
			System.out.println("   * " + identifiers[i] + ": " + generalizations[i]);
		}
		System.out.println(" - Statistics");
		System.out.println(
				result.getOutput(result.getGlobalOptimum(), false).getStatistics().getEquivalenceClassStatistics());
	}
}
