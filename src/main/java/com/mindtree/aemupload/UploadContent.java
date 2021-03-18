package com.mindtree.aemupload;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;
import org.apache.jackrabbit.commons.JcrUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UploadContent {
    public static void listFilesForFolder(File folder) throws Exception {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                System.out.println(fileEntry.getPath()); 
                String file_path= fileEntry.getPath();
                readJson(file_path);
            }
            }
        }


    @SuppressWarnings("unchecked")
    public static void readJson(String file_path) throws Exception 
    {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        File configFile = new File("C:/Users/M1041877/eclipse-workspace2020-03/AEM_repo/src/main/java/AEM_Migration/AEM_repo/config.properties");
        
        Repository repository = JcrUtils.getRepository("http://localhost:4502/crx/server/");

        //Create a Session
        javax.jcr.Session session = repository.login( new SimpleCredentials("admin", "admin".toCharArray()));
        
        try (FileReader reader = new FileReader(file_path))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONObject page = (JSONObject) obj;
            JSONObject inside_page = (JSONObject) page.get("pagename");
            String title = (String) inside_page.get("title");
            
           
            
//            System.out.println(title);
            JSONArray components = (JSONArray) inside_page.get("components");
            JSONObject component = (JSONObject) components.get(0);
            
            Set<Map.Entry<String, JSONArray>> entries = component.entrySet();
            ArrayList<String> list=new ArrayList<String>(); 
            for(Map.Entry<String, JSONArray> entry: entries) {
               list.add(entry.getKey());
            }
            FileReader config_reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(config_reader);
        
            
            String root_path=props.getProperty("root_path");
            String page_path=props.getProperty("page_path");
            duplicateNode(title,root_path,session);
            
            
            for(String list_value:list)
            {
            JSONArray topheader = (JSONArray) component.get(list_value);
            JSONObject fields = (JSONObject) topheader.get(0);
            JSONArray field = (JSONArray) fields.get("Fields");
            String serializedMap = props.getProperty(list_value);
            
            String component_path = props.getProperty(list_value);
           
            field.forEach( field_inside -> {
				try {
					parseComponentObject( (JSONArray) field_inside ,list_value,component_path,session,root_path,page_path,title);
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
			} );
            }
           
           session.save();
           session.logout();
           
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
		
    }
 
    private static void parseComponentObject(JSONArray component, String list_value, String path_values, Session session, String root_path, String page_path,String page_name) throws PathNotFoundException, RepositoryException 
    {         
//    	System.out.println(path_values);
//    	String root_path="content/Aem-Copy-Site-page/Marriot-Template/jcr:content/root/responsivegrid";
    	if (path_values.length()>0) {
    	String[] path=path_values.split(",");
    	int button=0;
    	for(int path_index=0;path_index<path.length;path_index++)
    	{
    		
    		
    		JSONObject field_inside = (JSONObject) component.get(0);
        	String element = (String) field_inside.get("Field Name");
        	String element_value = (String) field_inside.get("Field Value");
        	
        	
        	JSONObject field_inside1 = (JSONObject) component.get(1);
        	String alt = (String) field_inside1.get("Field Name");
        	String alt_value = (String) field_inside1.get("Field Value");
        	
        	
        	JSONObject field_inside2 = (JSONObject) component.get(2);
        	String href = (String) field_inside2.get("Field Name");
        	String href_value = (String) field_inside2.get("Field Value");
        	
        	
        	JSONObject field_inside3 = (JSONObject) component.get(3);
        	String tag = (String) field_inside3.get("Field Name");
        	String tag_value = (String) field_inside3.get("Field Value");
        	
        	
        	String aem_path_value=path[path_index];
        	 String image_path=root_path+page_name+page_path+aem_path_value;
        	 System.out.println(image_path);
        	 Node rootnode=session.getRootNode().getNode(image_path);
        	 
        	 if(element=="buttonText") {
        		 button=button+1;
		        	 if(element_value.length()>1) {
		        	 rootnode.setProperty(element+button, element_value);
		        	 }
		        	 
		        	 if(href_value.length()>1) {
		            	 rootnode.setProperty("link"+button, href_value);
		            	 }
		      }
        	 else {
        		 if(element_value.length()>1) {
		        	 rootnode.setProperty(element, element_value);
		        	 }
		        	 
		        	 if(href_value.length()>1) {
		            	 rootnode.setProperty("link", href_value);
		            	 }
        	 }
        	 
        	 if(alt_value.length()>1) {
            	 rootnode.setProperty(alt, alt_value);
            	 }
        	
        	 
    		
    		
    		 
    	}
    	}
    }
    
    
    
    
    
    
    
    public static void duplicateNode(String title,String root_path,javax.jcr.Session session) throws Exception
    {
    	try {
    	   
    	   Node root = session.getRootNode();
    	  
    	   Node srcnodepath = root.getNode("content/Page-Template");
    	   //Node srcnodepath = root.getNode("content/marriott-hws/language-masters/na/en-us/hotels/newyork-marriott-marquis/dining/dine-test");
    	  
    	   Node destnode = root.addNode(root_path+title);
    	   destnode.setPrimaryType("cq:Page");
    	   session.save();
    	//   Node destnode = root.getNode("content/pagedemo");
    	   
    	   Workspace workspace = session.getWorkspace();
    	   if (srcnodepath.hasNodes()) {
    		   NodeIterator worknodes = srcnodepath.getNodes();
    		   while (worknodes.hasNext()) {
    		   Node childnde = worknodes.nextNode();
    		  
    		   // formation of destination path and its relative path variables
    		   String[] parts = childnde.getPath().split("/");
    		   String destrelativepath = parts[(parts.length)-1];
    		   String destpath = destnode.getPath() + "/" + parts[(parts.length)-1];
    		 
    		  
    		   if (validatedestpath(destnode, destrelativepath)) {
    		   workspace.copy(childnde.getPath(), destpath);
    		   }
    		   }
    		   }
    	   
    	   Node JCR_destnode = root.getNode(root_path+title+"/jcr:content");
    	   JCR_destnode.getProperty("jcr:title").remove();
    	   JCR_destnode.setProperty("jcr:title", title);
    	   
    			   
    		   System.out.println("Succesfully copied to the destination");
    		   
    		   
    		   } 
    	catch (Exception e) {
 		   e.printStackTrace();
 		   }
 		   
 		   
}

    
    
    
    
    
    static Boolean validatedestpath(Node destnode, String destrelativepath) {
		   try {
		   if (destnode.hasNode(destrelativepath)) {
		   return false;
		   }
		   } catch (Exception e) {
		   e.printStackTrace();
		   }
		   return true;
		   }
	
 
    
    
    
    
    
    
    public static void main(String[] args) throws Exception
    {
    	listFilesForFolder(new File("D:\\marriot\\Data\\Json\\target"));   
}
}