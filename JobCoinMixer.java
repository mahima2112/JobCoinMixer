package practice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class JobCoinMixer {
	public static final String houseAccount = "House";
	public static final double FEE = 0.015;
	private static String depositAddress = "";
	private static String balance = "";
	private static List<String> addresses;
	public static final String url = ""; // add an an address here for API endpoints to work
	
	private static void userAddresses() {
		addresses = new ArrayList<>(3);
		System.out.println("Please provide 3 unused addresses");
		Scanner sc = new Scanner(System.in);
		int addressCounter = 0;
		while(addressCounter<3) {
			String tempAddress = sc.next();
			addresses.add(tempAddress);
			addressCounter++;
		}
		
		System.out.println(addresses.get(0) + " " + addresses.get(1) + " " + addresses.get(2));
	}
	
	private static String chooseDepositAddress() {
		List<String> depositOptions = new ArrayList<>(Arrays.asList("deposit1", "deposit2", "deposit3", "deposit4", "deposit5"));
		Random rand = new Random();
		int randomnumber = 0 + rand.nextInt(((depositOptions.size()-1)-0)+1);
		return depositOptions.get(randomnumber);
	}
	
	public static void getDepositInfo() throws Exception {
		String stringBalance = "0";
		Boolean firstTime = true;
		
		while(stringBalance.charAt(0) == '0') {
			if(!firstTime) {
				System.out.println("Remider: Please deposit your bitcoins "+ depositAddress);
				TimeUnit.SECONDS.sleep(8);
			}
			
			String urltoRead = url + "addresses/";
			StringBuilder stringBuilder = new StringBuilder(urltoRead);
			stringBuilder.append(URLEncoder.encode(depositAddress, "UTF-8"));
			URL obj = new URL(stringBuilder.toString());
			
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept-Charset", "UTF-8");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			StringBuffer response = new StringBuffer();
			while((line = in.readLine())!=null) {
				response.append(line);
			}
			in.close();
			
			String addressInfo = response.toString();
			stringBalance = addressInfo.substring(12, addressInfo.indexOf(","));
			firstTime = false;
			
		}
		balance = stringBalance;
		
	}
	
	public static void distributeFunds() throws Exception {
		double curBalance = (Integer.parseInt(balance)) * 1.0;
		curBalance = curBalance - (curBalance * FEE);
		
		double balance1 = curBalance/(5.0);
		curBalance-=balance1;
		TimeUnit.SECONDS.sleep(5);
		postValue(houseAccount, addresses.get(0), String.valueOf(balance1));
		
		double balance2 = curBalance/(3.0);
	    curBalance -= balance2;
	    TimeUnit.SECONDS.sleep(7);
	    postValue(houseAccount, addresses.get(1) , String.valueOf(balance2));

	    TimeUnit.SECONDS.sleep(4);
	    postValue(houseAccount, addresses.get(2) , String.valueOf(curBalance));
	}
	
	public static void postValue(String from, String to, String coins) throws Exception {
		String urltoRead = url + "transactions";
		URL postUrl = new URL(urltoRead);
		URLConnection con = postUrl.openConnection();
		HttpURLConnection http = (HttpURLConnection)con;
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		
		Map<String, String> agruments = new HashMap<>();
		agruments.put("fromAddress", from);
		agruments.put("toAddress", to);
		agruments.put("amount", coins);
		
		StringJoiner sj = new StringJoiner("&");
		for(Map.Entry<String,String> entry : agruments.entrySet())
	        sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
	             + URLEncoder.encode(entry.getValue(), "UTF-8"));
	    byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
	    int length = out.length;

	    http.setFixedLengthStreamingMode(length);
	    http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	    http.connect();

	    try(OutputStream os = http.getOutputStream()) {
	        os.write(out);
	    }
	  }

		
	
	
	public static void main (String args[]) throws Exception {
		
		// system will ask for 3 unused addresses
		
		userAddresses();
		
		// system chooses a deposit address
		
		depositAddress = chooseDepositAddress();
		System.out.println("please deposit to this address "+ depositAddress);
		System.out.println("1.5 % fee will be taken");
		
		// system waits 5 seconds and checks if deposit is taken
		
		TimeUnit.SECONDS.sleep(5);
	    getDepositInfo();
	    System.out.println("Your deposit was a success!");
	    
	    // deposit will be moved to house account
	    
	    Random rand = new Random();
	    int num = 3 + rand.nextInt((10 - 3) + 1);
	    TimeUnit.SECONDS.sleep(num);
	    postValue(depositAddress, houseAccount, balance);
	    
	  // different increments of the balance is moved from the house account to different addresses provided by the user
	    distributeFunds();
	}
	

}
