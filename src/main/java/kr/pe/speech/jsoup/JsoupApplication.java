package kr.pe.speech.jsoup;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class JsoupApplication {
	private final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

	// SSL 우회 등록
	public static void setSSL() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultHostnameVerifier(
				new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				}
		);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	public static void main(String[] args) throws IOException {
	try{

		print("\nLinks: (%d)", args.length );

		//Validate.isTrue(args.length == 1, "Usage: supply url to fetch");


		String url = args[0];
		print("Fetching %s...", url);

		String domain="";
		if( args.length == 2)
			domain = args[1];

		if(url.indexOf("https://") >= 0 ){
			JsoupApplication.setSSL();
		}

		//Document doc = Jsoup.connect(url).get();
		Connection conn = Jsoup.connect(url)
				.header("Content-Type", "application/json;charset=UTF-8")
				.userAgent(USER_AGENT)
				.method(Connection.Method.GET)
				.ignoreContentType(true);

		Document doc = conn.get();

		Elements links = doc.select("a[href]");
		Elements media = doc.select("[src]");
		Elements imports = doc.select("link[href]");



		print("\nLinks: (%d)", links.size());
		//for (Element link : links) {
		//	print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
		//}


		List<Element> links2 = links.stream().distinct().collect( Collectors.toList() );
		print("\nLinks: (%d)", links2.size());
		for (Element link2 : links2) {
			print("1- * a: <%s>  (%s)", link2.attr("abs:href"), trim(link2.text(), 35));
		}


		// 2-depth
		int nSources = links2.size();
		int nLinks = 0;
		int nIter = 0;
		List<Element> links3_all = new ArrayList<>();
		FileWriter fDomainException = new FileWriter("./DomainException.txt", false);
		BufferedWriter bw = new BufferedWriter(fDomainException);
		for (Element link2 : links2) {
			nIter++;

			print("[%5d / %5d]- * a: <%s>  (%s)", nIter, nSources, link2.attr("abs:href"), trim(link2.text(), 35));

			url = link2.attr("abs:href");

			if (url.indexOf(domain) == -1) {
				bw.write( url +"\t" + link2.text()  );
				bw.newLine();
				continue;
			}

			Connection conn2 = Jsoup.connect(url)
					.header("Content-Type", "application/json;charset=UTF-8")
					.userAgent(USER_AGENT)
					.method(Connection.Method.GET)
					.ignoreContentType(true);

			Document doc2 = conn2.get();

			Elements links3 = doc2.select("a[href]");
			Elements media3 = doc2.select("[src]");
			Elements imports3 = doc2.select("link[href]");

			nLinks += links3.size();
			print("           2th   Links: (%d) ==> (%d)", links3.size(), nLinks);


			for (Element link3 : links3)
				links3_all.add(link3);

		}
		bw.close();

		//중복제거
		List<Element> links4 =  links3_all.stream().distinct().collect( Collectors.toList() );
		print("           3th   Links: (%d) ==> (%d)", links3_all.size(), links4.size() );

		nIter = 0;
		nSources = links4.size();
		List<String>  list_url = new ArrayList<>();
		for (Element link4 : links4 ) {
			nIter++;
			list_url.add( link4.attr("abs:href") );

			print("[%5d / %5d]- * a: <%s>  (%s)", nIter, nSources, link4.attr("abs:href"), trim(link4.text(), 35));
		}

		Set<String> mySets = new HashSet<String>( list_url );
		print(" *****  Summary  *****"  );
		print(" URL : %s", url );
		print(" has  total   %d   3th links  ", mySets.size() );

		FileWriter fTotalLinks = new FileWriter("./TotalLinks.txt", false);
		BufferedWriter bwTotalLinks = new BufferedWriter(fTotalLinks);

		nIter = 0;
		nSources = mySets.size();
		Iterator<String> ite = mySets.iterator();
		while( ite.hasNext() ){

			String data = ite.next();

			/*
			Optional<Element> result = links4.stream().filter( x -> x.attr("abs:href").equals( data )).findFirst();
			bwTotalLinks.write( result.get().attr("abs:href") + "\n" + result.get().text() );
			bwTotalLinks.newLine();
			print("[%5d / %5d]- * a: <%s>  (%s)", nIter, nSources, result.get().attr("abs:href"), result.get().text(), 35 );
			*/

			bwTotalLinks.write(data);
			bwTotalLinks.newLine();
			print("[%5d / %5d]- * a: <%s>  ", nIter, nSources, data, 35 );

			nIter++;
		}
		bwTotalLinks.close();



	}catch (IOException e) {
		// Exp : Connection Fail
		e.printStackTrace();
	} catch (KeyManagementException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	private static void print (String msg, Object...args){
		System.out.println(String.format(msg, args));
	}

	private static String trim (String s,int width){
		if (s.length() > width)
			return s.substring(0, width - 1) + "...";
		else
			return s;
	}
}




