package com.ra4king.opengl.util;

import java.util.ArrayList;

/**
 * @author ra4king
 */
public class StringUtil {
	public static String[] split(String input, char delim) {
		ArrayList<String> parts = new ArrayList<>();
		for(int index; (index = input.indexOf(delim)) != -1;) {
			parts.add(input.substring(0, index));
			input = input.substring(index + 1);
		}
		parts.add(input);
		return parts.toArray(new String[parts.size()]);
	}
}
