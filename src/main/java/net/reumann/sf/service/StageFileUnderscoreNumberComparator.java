package net.reumann.sf.service;

import java.util.Comparator;

public class StageFileUnderscoreNumberComparator  implements Comparator<String> {
	@Override
	public int compare(String s1, String s2) {
		try {
			// Split strings by underscore and convert to integers
			String[] parts1 = s1.split("_");
			String[] parts2 = s2.split("_");

			// Check if both strings have the correct format (4 parts with 3 numbers)
			if (parts1.length != 4 || parts2.length != 4) {
				throw new RuntimeException("after splitting string of files, we didn't get four parts");
			}

			// Compare each number position left to right
			//NOTE we don't care about array pos 0 since that's a throw away (the name to the left of first underscore)
			int len = parts1.length;
			for (int i = 1; i < len; i++) {
				//last one has extension on it, parse that off
				if (i == len - 1) {
					int endIndex = parts1[i].indexOf(".") - 1;
					parts1[i] = parts1[i].substring(0,endIndex);
					parts2[i] = parts2[i].substring(0,endIndex);
				}
				int num1 = Integer.parseInt(parts1[i]);
				int num2 = Integer.parseInt(parts2[i]);

				if (num1 != num2) {
					return num1 - num2;
				}
			}
			return 0; // Strings are equal
		} catch (NumberFormatException e) {
			throw new RuntimeException("Error in compare", e);
		}
	}
}
