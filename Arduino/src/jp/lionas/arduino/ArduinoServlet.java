package jp.lionas.arduino;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import javax.servlet.http.*;

//import com.google.appengine.api.urlfetch.HTTPRequest;
//import com.google.appengine.api.urlfetch.URLFetchServiceFactory;


import java.util.List;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;


@SuppressWarnings("serial")
public class ArduinoServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/plain");
		resp.setCharacterEncoding("UTF-8");

// RSSフィード取得
//	    final String FEED_URL = "http://www.rssweather.com/wx/jp/tokushima/rss.php";
//	    FeedFetcher fetcher = new HttpURLFeedFetcher();
//        try {
//            SyndFeed feed = fetcher.retrieveFeed(new URL(FEED_URL));
//    		resp.getWriter().println("Blog Title:" + feed.getTitle());
//            for (SyndEntry entry : (List<SyndEntry>)feed.getEntries()){
//            	resp.getWriter().println("Title:" + entry.getTitle());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

		// 入退室状態
		String imageURI = req.getServerName() + ":" + req.getServerPort();
        resp.getWriter().println("1,田中 太郎,True,http://" + imageURI + "/images/android-logo.png");
        resp.getWriter().println("2,山田 次郎,False,http://" + imageURI + "/images/android-logo.png");
	}
}
