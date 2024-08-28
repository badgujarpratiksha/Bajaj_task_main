package com.bajajhealth;

/**
 * Hello world!
 *
 */
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;

public class DestinationHashGenerator {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN> <JSON File Path>");
			return;
		}

		String prn = args[0].trim().toLowerCase();
		String jsonFilePath = args[1];

		try {
			// Read and parse the JSON file
			File file = new File(jsonFilePath);
			Scanner scanner = new Scanner(new FileReader(file));
			StringBuilder jsonStr = new StringBuilder();
			while (scanner.hasNext()) {
				jsonStr.append(scanner.nextLine());
			}
			scanner.close();

			JSONObject jsonObject = new JSONObject(jsonStr.toString());

			// Find the "destination" key
			String destinationValue = findDestinationValue(jsonObject);

			if (destinationValue == null) {
				System.out.println("No 'destination' key found in the JSON file.");
				return;
			}

			// Generate a random 8-character alphanumeric string
			String randomString = generateRandomString(8);

			// Generate the MD5 hash
			String concatenatedValue = prn + destinationValue + randomString;
			String md5Hash = generateMD5Hash(concatenatedValue);

			// Output the result in the required format
			System.out.println(md5Hash + ";" + randomString);

		} catch (IOException e) {
			System.out.println("Error reading the JSON file: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error generating MD5 hash: " + e.getMessage());
		}
	}

	private static String findDestinationValue(JSONObject jsonObject) {
		for (String key : jsonObject.keySet()) {
			Object value = jsonObject.get(key);

			if (key.equals("destination")) {
				return value.toString();
			}

			if (value instanceof JSONObject) {
				String result = findDestinationValue((JSONObject) value);
				if (result != null)
					return result;
			} else if (value instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray) value;
				for (int i = 0; i < jsonArray.length(); i++) {
					if (jsonArray.get(i) instanceof JSONObject) {
						String result = findDestinationValue(jsonArray.getJSONObject(i));
						if (result != null)
							return result;
					}
				}
			}
		}
		return null;
	}

	private static String generateRandomString(int length) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(characters.charAt(random.nextInt(characters.length())));
		}
		return sb.toString();
	}

	private static String generateMD5Hash(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] messageDigest = md.digest(input.getBytes());
		StringBuilder sb = new StringBuilder();
		for (byte b : messageDigest) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
