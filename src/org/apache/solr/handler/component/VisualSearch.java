package org.apache.solr.handler.component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.utils.FileUtils;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class VisualSearch implements ISearcher {
	
	private String indexpath;
	private int origX;
	private int origY; 
	private int width;
	private int height;

	public VisualSearch(String indexpath) {
		this.indexpath = indexpath;
	}
	
	public void buildIndex(String imagepath) {
		// Getting all images from a directory and its sub directories.
		try {
			List<String> images = FileUtils.getAllImages(new File(imagepath),true);
			// DocumentBuilder builder = DocumentBuilderFactory
			// .getAutoColorCorrelogramDocumentBuilder();
            //DocumentBuilderFactory is a function in LIRe
			DocumentBuilder builder = DocumentBuilderFactory
					.getCEDDDocumentBuilder();
			// Creating an Lucene IndexWriter config
			IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40,
					new WhitespaceAnalyzer(Version.LUCENE_40));
            //Create an index writer based on index writer config
			IndexWriter iw = new IndexWriter(FSDirectory.open(new File(
					indexpath)), conf);
			for (Iterator<String> it = images.iterator(); it.hasNext();) {
				String imageFilePath = it.next();
				//System.out.println("Indexing " + imageFilePath);
				try {
					BufferedImage img = ImageIO.read(new File(imageFilePath));
					Document document = builder.createDocument(img,
							imageFilePath);
					iw.addDocument(document);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// closing the IndexWriter
			iw.close();
			//System.out.println("Finished indexing.");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void buildIndex(List<String> images) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory
					.getCEDDDocumentBuilder();
			// Creating an Lucene IndexWriter
			IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40,
					new WhitespaceAnalyzer(Version.LUCENE_40));

			IndexWriter iw = new IndexWriter(FSDirectory.open(new File(
					indexpath)), conf);
			for (Iterator<String> it = images.iterator(); it.hasNext();) {
				String imageFilePath = it.next();
				System.out.println("Indexing " + imageFilePath);
				try {
					BufferedImage img = ImageIO.read(new File(imageFilePath));
					Document document = builder.createDocument(img,
							imageFilePath);
					iw.addDocument(document);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// closing the IndexWriter
			iw.close();
			//System.out.println("Finished indexing.");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	public List<String> Search(BufferedImage img) {

		IndexReader ir;
		List<String> results = new ArrayList<String>();
		try {
			ir = DirectoryReader.open(FSDirectory.open(new File(indexpath)));
			ImageSearcher searcher = ImageSearcherFactory
					.createCEDDImageSearcher(10);

			ImageSearchHits hits = searcher.search(img, ir);

			for (int i = 0; i < hits.length(); i++) {
				String fileName = hits.doc(i).getValues(
						DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
				System.out.println("scores: " + hits.score(i) + ": \t" + fileName);
				results.add(fileName);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;
	}

	public List<String> Retrieve(String query) {

		boolean seg = false;
		BufferedImage img = null;
		BufferedImage fimg = null;

		List<String> results = new ArrayList<String>();

		try {
			img = readImage(query);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			fimg = img;
			
		results = Search(fimg);
		return results;
	}

	@Override
	public List<String> Retrieve(String query, int origX, int origY, int width,
			int height) {
		// TODO Auto-generated method stub

		this.origX = origX;
		this.origY = origY;
		this.width = width;
		this.height = height;
		
		//boolean seg = true;
		BufferedImage img = null;
		BufferedImage subimg = null;
		BufferedImage fimg = null;


		List<String> results = new ArrayList<String>();

		try {
			img = readImage(query);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		if (!this.isBBoxInsideImage(img, origX, origY, width, height)) /*
//																		 * AVOID
//																		 * RasterFormatException
//																		 */{
//			return null;
//		}
		
		fixBBox(img, origX, origY, width, height);		
		subimg = img.getSubimage(this.origX, this.origY, this.width, this.height);

			fimg = subimg;

		results = Search(fimg);
		return results;
	}

	public BufferedImage readImage(String imageFilePath)
			throws FileNotFoundException, IOException {

		BufferedImage bffImage = null;
		try {
			bffImage = ImageIO.read(new File(imageFilePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bffImage;
	}

	private boolean isBBoxInsideImage(BufferedImage img, int origX, int origY,
			int width, int height) {

		int imageWidth = img.getWidth();
		int imageHeight = img.getHeight();

		if (origX + width <= imageWidth && origY + height <= imageHeight) {
			return true;
		}
		return false;
	}
	
	private void fixBBox(BufferedImage img, int origX, int origY,
			int width, int height) {

		int imageWidth = img.getWidth();
		int imageHeight = img.getHeight();

		if (origX + width > imageWidth) {
			this.width = imageWidth - origX;			
		}
		if (origY + height > imageHeight) {
			this.height = imageHeight - origY;				
		}
	}	

	/*public static void main(String[] args) throws Exception 
	{
		
		System.out.println(System.getProperty("java.library.path"));
		long start = System.currentTimeMillis();
		VisualSearch searcher = new VisualSearch("index-demo-test");
		searcher.buildIndex("D:\\DevelopmentEnv\\eclipse-juno-64\\workspace\\VisualSearch\\Yiming\\demo_bags");
		String query = "D:\\DevelopmentEnv\\eclipse-juno-64\\workspace\\VisualSearch\\Yiming\\boundingbox\\frame_01410.jpg";
		List<String> searchresults = new ArrayList<String>();
		//searchresults = searcher.Retrieve(query);
		searchresults = searcher.Retrieve(query,0,0,1000,10);
		long end = System.currentTimeMillis();
		System.out.println("took " + ((end - start) / 1000.0));
		// OpenCVUtils utils = new OpenCVUtils();
		// IplImage myImage = utils.loadOrExit(
		// new
		// java.net.URL("http://localhost/~fflei001/images_tcl/screenshot/2.jpg"),
		// new int[] {441,272,726,459});
		// utils.show(myImage, "test");
	}*/
}
