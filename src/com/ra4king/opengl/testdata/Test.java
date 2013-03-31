package com.ra4king.opengl.testdata;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author ra4king
 */
public class Test {
	public static void main(String[] args) {
		boolean add = true;
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(Test.class.getResourceAsStream("test.txt"), "UTF-8"))) {
			String s;
			while((s = reader.readLine()) != null) {
				s = s.trim();
				if(!add) {
					if(s.startsWith("-"))
						System.out.print(s.substring(1) + "+");
				}
				else if(Character.isDigit(s.charAt(0)))
					System.out.print(s + "+");
			}
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}
}
