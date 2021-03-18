package com.mindtree.aemupload;

import javax.jcr.Repository;
import javax.jcr.SimpleCredentials;
import javax.jcr.Node;
import javax.jcr.Workspace;
import org.apache.jackrabbit.commons.JcrUtils;
import javax.jcr.NodeIterator;
public class CopyNodesFromSource {

public static void main(String[] args) throws Exception {

try {
   //Create a connection to the CQ repository running on local host
    Repository repository = JcrUtils.getRepository("http://localhost:4502/crx/server/");

  //Create a Session
   javax.jcr.Session session = repository.login( new SimpleCredentials("admin", "admin".toCharArray()));

 //Create a node that represents the root node
   Node root = session.getRootNode();
   Node srcnodepath = root.getNode("content/Page-Template");
   //Node srcnodepath = root.getNode("content/marriott-hws/language-masters/na/en-us/hotels/newyork-marriott-marquis/dining/dine-test");
  
   Node destnode = root.addNode("content/Bulk_develop/hongkong");
   destnode.setPrimaryType("cq:Page");
   
   session.save();
   Workspace workspace = session.getWorkspace();
   if (srcnodepath.hasNodes()) {
	   NodeIterator worknodes = srcnodepath.getNodes();
	   while (worknodes.hasNext()) {
	   Node childnde = worknodes.nextNode();
	  
	   // formation of destination path and its relative path variables
	   String[] parts = childnde.getPath().split("/");
	   //System.out.println(parts);
	   String destrelativepath = parts[(parts.length)-1];
	   //System.out.println(destrelativepath);
	   String destpath = destnode.getPath() + "/" + parts[(parts.length)-1];
	   //System.out.println(destpath);
	 
	  
	   if (validatedestpath(destnode,destrelativepath)) {
	   workspace.copy(childnde.getPath(), destpath);
	   
	   //workspace.move(srcAbsPath, destAbsPath);
	   }
	   }
	   }
	   System.out.println("Succesfully copied to the destination");
	   session.logout();
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
	   }