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
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
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

		Validate.isTrue(args.length == 1, "Usage: supply url to fetch");


		String url = args[0];
		print("Fetching %s...", url);

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


		List<Element> links2 = links.stream().distinct().collect(Collectors.toList());
		print("\nLinks: (%d)", links2.size());
		for (Element link2 : links2) {
			print(" * a: <%s>  (%s)", link2.attr("abs:href"), trim(link2.text(), 35));
		}

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




