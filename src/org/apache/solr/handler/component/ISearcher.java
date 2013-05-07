package org.apache.solr.handler.component;

import java.util.List;
/**
 * 
 * @author learlee
 *   fi
 */
public interface ISearcher {
	
	public void buildIndex(String imagepath);
  
	public void buildIndex(List<String> images);

	public List<String> Retrieve(String query);// search by preposs photo

	public List<String> Retrieve(String query, int origX, int origY, int width,
			int height);// search by origin photo
}
