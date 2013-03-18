/**
 * 
 */
package com.syw.SNSsync.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author songyouwei
 *
 */
public class HTTPUtil {
	
	/**
	 * 得到url的输入流
	 */
	public static InputStream getInputStreamFromUrl(String urlString) {
		URL url = null;
		InputStream inputStream = null;
		try {
			url = new URL(urlString);
			inputStream = (InputStream) url.getContent();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputStream;
	}
	
}
