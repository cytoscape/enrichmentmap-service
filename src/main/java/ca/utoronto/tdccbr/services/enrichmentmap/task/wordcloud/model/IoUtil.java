package ca.utoronto.tdccbr.services.enrichmentmap.task.wordcloud.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class IoUtil {

	public static String readAll(InputStream inputStream) throws IOException {
		InputStreamReader reader = new InputStreamReader(inputStream);
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[16 * 1024];
		int totalRead = 0;
		while (true) {
			totalRead = reader.read(buffer, 0, buffer.length);
			if (totalRead == -1) {
				break;
			}
			builder.append(buffer, 0, totalRead);
		}
		return builder.toString();
	}

	public static String readAll(URL url) throws IOException {
		return readAll(url.openStream());
	}
}
