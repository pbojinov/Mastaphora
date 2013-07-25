//CRAWLER
//AUTHOR: Petar Bojinov
//DATE: Nov 2011
//
//Using JSOUP to connect to webpage and parse html content
//based off list links example
//Using java.net package for URL manipulation
//
import java.io.IOException;
import java.util.*;
import java.io.*;
import java.net.*;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.String;
import java.math.BigInteger;

public class Mastaphora {
	
	//private static BigInteger chunkSize = BigInteger.valueOf(0);  //size of the entire tree of documents
	private static int chunkSize = 0;
	private static BigInteger grandSum = BigInteger.valueOf(0);
    public static void main(String[] args) throws Exception {
		if (args.length != 2) {
		    System.err.println("Usage: <url> linkdepth");
		    System.exit(1);
		}
		String site = args[0]; //URL to traverse
		int linkDepth = Integer.parseInt(args[1]); //max link depth
		//int chunkSize = 0; //size of the entire tree of documents
		int curr = 0; //current link depth (used in process_queue to determine when maximum link depth is reached)
		ArrayList<String> queueEVEN = new ArrayList<String>();
		ArrayList<String> queueODD = new ArrayList<String>();	
		String value = "";
		try {
			//Test connection to make sure URL is valid
			URL root = new URL(site);
			URLConnection c = root.openConnection();
			c.connect();
			
			//initiate JSOUP connection to document
			//getDocument(site);
			
			//add root site to QUEUE and start processing
			queueEVEN.add(site);
			value = process_queueEVEN(queueEVEN, queueODD, chunkSize, curr, linkDepth);
		}
		catch (MalformedURLException e) { 
			System.out.println("Invalid URL");
			e.printStackTrace();  //Output goes to System.err.
		    e.printStackTrace(System.out);  //Send trace to stdout.
		} 
		catch (IOException e) {           
			// openConnection() failed
		}
		System.out.println("woooo " + value);

	}//end main
	
	//Returns JSOUP doc with all html content ready for parsing
	//
	//Cannot handle .m3u currently, see what other formats it can't handle...
	//need try catch for exception handeling
	
	public static Document getDocument(ArrayList<String> queue) {
		int retry = 0;
		int maxRetry = 5;
		Document doc = null;
		if (retry == maxRetry) {
			System.out.println("Max retry of " + maxRetry + "reached. Too many broken URLS");
			System.exit(1);
		}
		while (retry < maxRetry) {
			String site = queue.get(0);
			System.out.println("getDocument call - " + site);

			try {
				//Instantiate connection to URL provided with time out of 5 sec
				System.out.println("Fetching " + site + "...");
				doc = Jsoup.connect(site).timeout(5000).get();	
				return doc;
			}
			// catch (MalformedURLException e) { 
			// 	System.out.println("Malformed URL");
			// 	e.printStackTrace();  //Output goes to System.err.
			//     e.printStackTrace(System.out);  //Send trace to stdout.
			// }
			catch (Exception e) {
				System.out.println(site + ": " + e.getMessage()); 
				queue.remove(0);
				retry++;
			}
		}
		return doc;
	}
	//Returns all CSS documents
	public static Elements getImports(Document doc) {
		Elements imports = doc.select("link[href]");
		return imports;
	}
	//Returns all MIME types and JS docs
	public static Elements getMedia(Document doc) {
		Elements media = doc.select("[src]");
		return media;
	}
	//Returns all <a href=...> links 
	public static Elements getLinks(Document doc) {
		Elements links = doc.select("a[href]");
		return links;
	}
	//Returns the total number of links for any given page
	public static int linksTotal(Document doc) {
		Elements links = getLinks(doc);
		System.out.println("Links total: (" + links.size() + ")");
		return links.size();
	}
	//Returns total number of MIME and JS files
	public static int mediaTotal(Document doc) {
		Elements media = getMedia(doc);
		System.out.println("Media items total: (" + media.size() + ")");
		return media.size();
	}
	//Returns total number of imports (CSS documents)
	public static int importsTotal(Document doc) {
		Elements imports = getImports(doc);
		System.out.println("\nImports: (" + imports.size() + ")");
		return imports.size();
	}
	//Returns <title> of given page
	public static String pageTitle(Document doc) {
		String title = doc.title();
		System.out.println("Title for page: " + title);
		return title;
	}
	//Returns the size of all media content (MIME and .JS)
	public static int mediaSize(Document doc) throws Exception {
		//BigInteger bigSize = BigInteger.valueOf(0);
		int totalSize = 0;
		Elements media = getMedia(doc);
		for (Element src : media) {
			if (src.tagName().equals("img")) {
				
				System.out.print(" - " + src.tagName() + ":    " + src.attr("abs:src")); 
				//if img doesnt not have a defined width/height in tag then it does not show
				if ((src.attr("width") != null) && (src.attr("height") != null)) {
					System.out.print(" " + src.attr("width") + "x" + src.attr("height") + "\n");
				}
				//size finder
				String imgURL = src.attr("abs:src"); //throws Malform URL exception if its HREF
				URL mediaURL = new URL(imgURL);
				URLConnection mediaC = mediaURL.openConnection();
				int sizeOfIMG = mediaC.getContentLength();

				if (sizeOfIMG < 0) {
					System.out.println("Could not determine size of IMG.");
				}
				else {
					totalSize += sizeOfIMG;
					System.out.println("Size of img = " + sizeOfIMG);
					//System.out.println("chunkSize: " + chunkSize);
				}
			}
			else {
				System.out.println(" - " + src.tagName() + ": " + src.attr("abs:src"));
				//size finder
				URL mediaSRC = new URL(src.attr("abs:src"));
				URLConnection mediaC2 = mediaSRC.openConnection();
				int size = mediaC2.getContentLength();
				if (size < 0) {
					System.out.println("Could not determine size of SRC file");
				}
				else {
					totalSize += size;
					System.out.println("Size of src = " + size);
					System.out.println("chunkSize: " + totalSize);
				}
			}
		}
		//bigSize = BigInteger.valueOf(totalSize);
		//return bigSize;
		return totalSize;
	}
	//Returns size of all .CSS documents and .ico
	public static int importSize(Document doc) throws Exception {
		int totalSize = 0;
		Elements imports = getImports(doc);
		for (Element link : imports) {
			System.out.println(" - " + link.tagName() +
 				  				":   "  + link.attr("abs:href") + 
								" type:"  + link.attr("rel"));

			URL importURL = new URL(link.attr("abs:href"));
			URLConnection importC = importURL.openConnection();
			int size = importC.getContentLength();
			if (size < 0) System.out.println("Could not determine size of import URL.");
			else {
				totalSize += size;
				System.out.println("Size of import url = " + size);
				System.out.println("chunkSize: " + totalSize);
			}
        }
		return totalSize;
	}
	//Returns size of HTML document
	//If request fails, saves document locally and finds size (method #2)
	public static int htmlSize(Document doc, String site) throws Exception {
		int totalSize = 0;
		
		//Method #1
		//Find size of html file by pinging site
		//Usually fails due to server restrictions
		////////////////////////////////////////////
		URL baseURL = new URL(site);
		URLConnection cc = baseURL.openConnection();
		int sizeHTML = cc.getContentLength();

		//if size could not be calculated, (returns -1)
		if (sizeHTML < 0) {
			// //Method #2
			// 			//Copy html code from source to local file using BufferedReader and Buffered Write
			// 			//Calculate size locally using .length()
			// 			//!! (int)float could cause overflow
			// 			///////////////////////////////////////////////
			// 			System.out.println("Could not determine size.");
			// 			System.out.println("Calculating Alternative Way...");
			// 			
			// 			//Read html from URL connection
			// 			BufferedReader in = new BufferedReader(new InputStreamReader(cc.getInputStream()));
			// 	        String inputLine = "";
			// 			String htmlFile = "";
			// 			//traverse through html file
			// 	        while ((inputLine = in.readLine()) != null)  {
			// 				//save html to temporary String
			// 				htmlFile += inputLine + "\n";
			// 				//System.out.println(inputLine);
			// 			}
			// 			//close buffer reader
			// 	        in.close();
			// 			//System.out.println(htmlFile);
			// 			
			// 			//WRITE TO LOCAL FILE PROCESS
			// 			///////////////////////////////////
			// 			//Create writer for local file save
			// 			Writer output = null;
			// 			//file is assumed to be .html, will not cause problems if extention is other
			// 			//would be nice to check for actual extention in future
			// 		  	File localFile = new File(site + ".html");
			// 		  	output = new BufferedWriter(new FileWriter(localFile));
			// 			//the string htmlFile is written to local file 
			// 		  	output.write(htmlFile);
			// 		  	output.close();
			// 		  	System.out.println("file" + localFile + " has been written locally");
			// 
			// 			// Get the number of bytes in the file
			// 			long length = htmlFile.length();
			// 			System.out.println("Size of " + localFile + " is: " + length);
			// 			
			// 			//!! important
			// 			//could cause an exception if the long is not within int range 
			// 			//using blindly right now and assuming it's within range as most html pages arent > 2^31 in size
			// 			totalSize += (int)length; 
		}
		else {
			System.out.println("Size of html = " + sizeHTML);
			totalSize += sizeHTML;
		}
		System.out.println("Total SIZE: " + totalSize);
		return totalSize;
	}
	//Returns size of all related content to a URL
	//Consists of media (MIME & .JS), imports (CSS), and size of html file
	//! second parameter String site needed for alternative html file size calculation
	public static int findSize(Document doc, String site) throws Exception {
		int totalSizeOfSite = 0;
		//BigInteger total = BigInteger.valueOf(0);
		
	 	totalSizeOfSite += mediaSize(doc);
		totalSizeOfSite += importSize(doc);
		totalSizeOfSite += htmlSize(doc, site);
		System.out.println("total of findSize:" + totalSizeOfSite);
		
		//total = BigInteger.valueOf(totalSizeOfSite);
		return totalSizeOfSite;
	}
	//Holds the size of entire traversable chunk
	public static void storeChunk(int chunkSize) {
		grandSum = grandSum.add(BigInteger.valueOf(chunkSize));
		//grandSum += chunkSize;
		printHR(1);
		System.out.println("GRANDSUM = " + grandSum);
		printHR(1);
	}
	public static BigInteger getChunk() {
		printHR(1);
		System.out.println("GRANDSUM = " + grandSum);
		printHR(1);
		return grandSum;
	}
	private static Boolean isValid(String linkHref) {
		return (!(linkHref.contains(".m3u"))) &&
		 	   (!(linkHref.contains("mailto:"))) && 
			   (!(linkHref.contains("ftp:"))) &&
			   (!(linkHref.contains("tel:"))) && 
			   (!(linkHref.contains("telnet:"))) && 
			   (!(linkHref.contains("urn:")))  &&
			   (!(linkHref.contains("ldap")));
	}
	//Stores <a href=...> links to given QUEUE
	//also removes invalid (un-traversable) file types in order to prevent crawler from crashing
	public static void addLinks_queueEVEN(Document doc, ArrayList<String> queueEVEN) {
		printLinks(doc);
		Elements links = getLinks(doc);
		for (Element link : links) {
			String linkHref = link.attr("abs:href");
			//check for non valid link types here
			//want to ideally traverse <html> content
			//MIME formats cause program to exit with error (1)
			//adds all links to QUEUE that are of verified extention 
			if (isValid(linkHref)) {   
			//Only http & https protocols supported
				queueEVEN.add(link.attr("abs:href"));
			}
			else {
				System.out.println("Only http & https protocols supported:" + linkHref);
			}
			//System.out.println(link.attr("abs:href"));
        }
		//Def:     Use linked-hash list to preserve order and remove non-unique links
		//Process: Add all links from queue to linked hash set,
		//         clear queue, and insert unique links back into queue.
		//         Linked hash list also acts as store for all previously visited links,
		//		   so same link is not traversed twice.
		Set<String> lhs = new LinkedHashSet<String>();
		lhs.addAll(queueEVEN);
		queueEVEN.clear();
		queueEVEN.addAll(lhs);
		//ideally want linked hash set to calculate unique number of URLs visisted
		
		System.out.println("After duplElim");
		Iterator<String> it = queueEVEN.iterator();
		while (it.hasNext()) {
	      String element = it.next();
	      System.out.println(element + " ");
	    }
	}
	//Stores <a href=...> links to given QUEUE
	//Removes invalid file types in order to prevent crawler from crashing
	public static void addLinks_queueODD(Document doc, ArrayList<String> queueODD) {
		printLinks(doc);
		Elements links = getLinks(doc);
		//Iterate through all links in document
		for (Element link : links) {
			//Saves Link extention to string in order to validate
			String linkHref = link.attr("abs:href");
			//check for non valid link types here
			//want to ideally traverse <html> content
			//MIME formats cause program to exit with error (1)
			//adds all links to QUEUE that are of verified extention 
			if (isValid(linkHref)) {   
			//Only http & https protocols supported
				queueODD.add(link.attr("abs:href"));
			}
			else {
				System.out.println("Only http & https protocols supported:" + linkHref);
			}
        }
		//Use linked-hash list to preserve order and remove non-unique links
		//add all links from queue to linked hash set,
		//clear queue, and insert unique links back into queue.
		Set<String> lhs = new LinkedHashSet<String>();
		lhs.addAll(queueODD);
		queueODD.clear();
		queueODD.addAll(lhs);
		
		System.out.println("After duplElim");
		Iterator<String> it = queueODD.iterator();
		while (it.hasNext()) {
	      String element = it.next();
	      System.out.println(element + " ");
	    }
	}
	public static String process_queueEVEN(ArrayList<String> queueEVEN, 
										 ArrayList<String> queueODD, int chunkSize, int curr, int linkDepth) throws Exception {
			printHR(2);
			System.out.println("TOP OF process_queueEVEN, CHUNK SIZE = " + chunkSize);
			if (queueEVEN.isEmpty()) {
				System.out.println("Crawl Complete with total size: " + chunkSize);
				//finished();
				//System.exit(0);
				BigInteger tempChunk = getChunk();
				String result = tempChunk.toString();
				return result;
			}
			else {
				while (!(queueEVEN.isEmpty())) {
					//goTo queueEVEN[0]
					Document doc = getDocument(queueEVEN);
					String site = queueEVEN.get(0);
					System.out.println("qeueEVEN get[0]: " + queueEVEN.get(0));
					
					//find size
					chunkSize += findSize(doc, site); //most definitely not being added!!
					storeChunk(chunkSize);
					
					if (curr < linkDepth) {
						//add all links to queueODD
						addLinks_queueODD(doc, queueODD);
					}
					//remove queueEVEN[0], the last element that was just crawled
					queueEVEN.remove(0);
					if (queueEVEN.isEmpty()) { 
						curr++;
					}
				}
			}
			process_queueODD(queueEVEN, queueODD, chunkSize, curr, linkDepth);
			BigInteger tempChunk = getChunk();
			String result = tempChunk.toString();
			return result;
		}
	public static String process_queueODD(ArrayList<String> queueEVEN,
										ArrayList<String> queueODD, int chunkSize, int curr, int linkDepth) throws Exception {
		printHR(2);
		System.out.println("TOP OF process_queueODD, CHUNK SIZE = " + chunkSize);
		if (queueODD.isEmpty()) {
			System.out.println("Crawl Complete with total size: " + chunkSize);
			//finished();
			BigInteger tempChunk = getChunk();
			String result = tempChunk.toString();
			return result;
		}
		else {
			while (!(queueODD.isEmpty())) {
				//goTo queueODD[0]
				Document doc = getDocument(queueODD);
				String site = queueODD.get(0);
				System.out.println("qeueODD get[0]: " + queueODD.get(0));
				
				//find size
				chunkSize += findSize(doc, site);
				storeChunk(chunkSize);
				
				if (curr < linkDepth) {
					//add all links to queueEVEN
					addLinks_queueEVEN(doc, queueEVEN);
				}
				//remove queueODD[0]
				queueODD.remove(0);
				if (queueODD.isEmpty()) { 
					curr++; 
				}
			}
		}
		process_queueEVEN(queueEVEN, queueODD, chunkSize, curr, linkDepth);
		BigInteger tempChunk = getChunk();
		String result = tempChunk.toString();
		return result;
	}
	public static void finished() {
		BigInteger tempChunk = getChunk();
		printHRStar(5);
		String message = "The size of the entire tree of documents viewable \n" +
						 "given the URL... with max depth... is: ";
		System.out.println(message + tempChunk + " bytes");

		System.exit(0);
	}
	//Prints link total and all links (2 in 1)
	public static void printLinks(Document doc) {
		linksTotal(doc);
		Elements links = getLinks(doc);
		for (Element link : links) {
			System.out.println(link.attr("abs:href"));
		}
	}
	public static void printHR(int size) {
		for (int i=0;i<size;i++) {
			System.out.println("-----------------------------------");
		}
	}
	public static void printHRStar(int size) {
		for (int i=0;i<size;i++) {
			System.out.println("***********************************");
		}
	}
}


