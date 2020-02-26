package shadel.dan;
import java.io.*;
import java.awt.Desktop;
import java.awt.image.RenderedImage;
import java.net.URI;
import java.net.URL;
import java.net.http.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.imageio.ImageIO;


public class rover {

	//returns an ArrayList of dates formatted for use for the mars rover's API
	public static ArrayList<String> dateFormatter(ArrayList<String> dates) throws ParseException{
		
		ArrayList<String> convertedDate = new ArrayList<String>();
		ArrayList<SimpleDateFormat> patterns = new ArrayList<SimpleDateFormat>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		boolean matched = false;
		
		//add the known patterns to compare against
		patterns.add(new SimpleDateFormat("MM/dd/yy"));
		patterns.add(new SimpleDateFormat("MMMM d, yyyy"));
		patterns.add(new SimpleDateFormat("MMMM dd, yyyy"));
		patterns.add(new SimpleDateFormat("MMM-dd-yyyy"));
		
		
		
		
		for(int i=0;i<dates.size();i++) {
			
			matched = false;
			
			//compare against all possible date patterns
			for(int j=0;j<patterns.size();j++)
			{
				//must match exactly
				patterns.get(j).setLenient(false);
				try {
					date = patterns.get(j).parse(dates.get(i));
					convertedDate.add(formatter.format(date));
					matched = true;
					break;
					
				}
				catch(Exception e) {
					continue;
				}
			}
			if(matched == false) {
				System.out.println("Unable to match date " + dates.get(i));
			}		
		}
		return convertedDate;
	}
	
	
	//returns a map containing date,img_src from the rover API
	public static Map<String,String> NASARequest(ArrayList<String> dates){
		
		ArrayList<String> images = new ArrayList<String>();
		HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;
        HttpResponse<String> response;
        String APIKey = "GDjd0aewWaCKbOb4AYUnnta7lZlxt5Hcd04kH8tt";
        String uri;
        String imgSource;
        Map <String, String> map = new HashMap<String, String>();
        
        for(int i=0;i<dates.size();i++) {
			try {
				
				//make the request for each day
				uri = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?earth_date="+dates.get(i)+"&api_key="+APIKey;
				request = HttpRequest.newBuilder().uri(URI.create(uri)).build();
				response = client.send(request,HttpResponse.BodyHandlers.ofString());

				//parse response body for image sources
				imgSource = response.body().split("\"img_src\":\"")[1];
				images.add(imgSource.split("\"")[0]);
				//System.out.println(images.get(0));
				map.put(dates.get(i), images.get(i));
				
				
				
				
				
			} catch (IOException | InterruptedException e2) {
				System.out.println("unable to complete request for date: "+dates.get(i));
			}
		
        }
		return map;
	}
	
	
	//creates an index.html to be loaded in a web browser
	public static void genHTML(Map<String,String> images) throws IOException, ParseException {
		
		
		File html = new File("./resources/webpage/index.html");
		FileWriter writer = new FileWriter(html);
		SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
		String date = null;
		String path = null;
		
		
		//Write the base HTML
		writer.write("<!DOCTYPE html>\r\n" + 
				"<html>\r\n" + 
				"<head>\r\n" + 
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"main.css\">\r\n" + 
				"<link rel=\"icon\" type=\"image/png\" href=\"favicon.png\">\r\n" + 
				"</head>\r\n" + 
				"<body style=\"background-image: url('background.png')\">\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"<h1> Nasa Rover Excericse </h1>\r\n" + 
				"\r\n" + 
				"<div class=\"logo-container\">\r\n" + 
				"	<img class = logo src=\"nasa.png\">\r\n" + 
				"</div>\r\n" + 
				"");
	
		//iterate through all images, add them to the html doc
		for(Map.Entry entry: images.entrySet())
		{
			
			//convert date to a more readable format
			date = formatter.format(parser.parse(entry.getKey().toString()));
			//saved image name
			path = entry.getKey().toString() + "_" + entry.getValue().toString().split("/")[14];
			

			writer.write("<br><br>\r\n" + 
					"<div class=\"img-container\">\r\n" + 
					"\r\n" + 
					"	<h2> "+ date +" </h2>\r\n" + 
					"	<br>\r\n" + 
					"	<img class=\"rover-img\" src=\"../images/"+ path + "\">\r\n" +  
					"</div>");
			
			
		}
		writer.close();
		
		
	}
	
	//saves an image to /resources/images
	public static void downloadImage(String date, String link) throws IOException {
		
		RenderedImage image = null;
		URL url;
		File dir = new File("./resources/images");
		//download image and save to a local folder named "images"
		url = new URL(link);
		image = ImageIO.read(url);

		
		//check if images dir exists, if not then create it
		if(!dir.exists()) {
			dir.mkdir();
		}
		
		//filename is yyyy-mm-dd_OriginalFileName
		ImageIO.write(image, "jpg", new File("./resources/images/"+date+"_"+link.split("/")[14]));
	}
	
	public static void main(String[] args) {

		ArrayList<String> dates = new ArrayList<String>();
		Map<String, String> map;
		File f = new File("./resources/inputDates.txt");
		
		//read in dates from file
		try {
			Scanner scanner = new Scanner(f);
			
			//copy all lines to the list of dates
			while(scanner.hasNextLine()) {
				
				dates.add(scanner.nextLine());
			}
		}
		catch(Exception e) {
			System.out.println("Unable to open file. Make sure a file named \"inputDates.txt\" exists and is in the resources folder.");
			System.exit(-1);
		}
		
		
		
		
		//format all the dates provided in format yyyy-mm-dd
		try {
			dates = dateFormatter(dates);
		} catch (ParseException e1) {
			System.out.println("Unable to parse dates");
			System.exit(-1);
		}
		
		
	
		//request all images from the mars rover API
		map = NASARequest(dates);
		
		for (Map.Entry entry : map.entrySet())
		{
			//download all images into ./resources/images
		    try {
				downloadImage(entry.getKey().toString(), entry.getValue().toString());
			} catch (IOException e) {
				System.out.println("Unable to download image for date: " + entry.getKey().toString());
			}
		}
		

		//generate a HTML page with all the images.
		try {
			genHTML(map);
			Desktop desktop = Desktop.getDesktop();
			//open a web browser with the generated HTML page.
			f = new File((System.getProperty("user.dir")+"\\resources\\webpage\\index.html"));
			URI page = f.toURI();
			desktop.browse(page);
		} 
		catch (IOException | ParseException e1) {
			System.out.println("Unable to generate html page, images can be found in /resources/images");
		}

	}

}