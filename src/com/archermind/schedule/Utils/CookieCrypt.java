package com.archermind.schedule.Utils;
import java.io.IOException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.util.Base64;

//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

/**
 * CookieCrypt
 * 
 * @author Cobra Pang
 * @version 1.0.0 2007-12-17
 */
public class CookieCrypt {
	// Crypt Key
	private byte[] desKey;

	public CookieCrypt(String desKey) {
		this.desKey = desKey.getBytes();
	}

	/**
	 * DES Encoder
	 * 
	 * @param plainText
	 * @return
	 * @throws Exception
	 */
	public byte[] desEncrypt(byte[] plainText) throws Exception {
		SecureRandom sr = new SecureRandom();
		byte rawKeyData[] = desKey;

		DESKeySpec dks = new DESKeySpec(rawKeyData);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, key, sr);

		byte data[] = plainText;
		byte encryptedData[] = cipher.doFinal(data);
		return encryptedData;
	}

	/**
	 * DES Decoder
	 * 
	 * @param encryptText
	 * @return
	 * @throws Exception
	 */
	public byte[] desDecrypt(byte[] encryptText) throws Exception {

		SecureRandom sr = new SecureRandom();
		byte rawKeyData[] = desKey;
		DESKeySpec dks = new DESKeySpec(rawKeyData);

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(dks);

		Cipher cipher = Cipher.getInstance("DES");

		cipher.init(Cipher.DECRYPT_MODE, key, sr);

		byte encryptedData[] = encryptText;
		byte decryptedData[] = cipher.doFinal(encryptedData);
		return decryptedData;
	}

	/**
	 * Cookie Encoder
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public String encrypt(String input) throws Exception {
		return base64Encode(desEncrypt(input.getBytes()));
	}

	/**
	 * Cookie Decoder
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public String decrypt(String input) throws Exception {
		byte[] result = base64Decode(input);
		return new String(desDecrypt(result));
	}

	/**
	 * Base64 Encode
	 * 
	 * @param s
	 * @return
	 */
	public static String base64Encode(byte[] s) {
		if (s == null)
			return null;
		// BASE64Encoder b = new sun.misc.BASE64Encoder();
		return android.util.Base64.encodeToString(s, Base64.DEFAULT);
		// return b.encode(s);
	}

	/**
	 * Base64 Decode
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public static byte[] base64Decode(String s) throws IOException {
		if (s == null)
			return null;
		// BASE64Decoder decoder = new BASE64Decoder();
		// byte[] b = decoder.decodeBuffer(s);
		byte[] b = android.util.Base64.decode(s, Base64.DEFAULT);
		return b;
	}

	public static String encrypt(String key, String input) throws Exception {
		CookieCrypt crypt = new CookieCrypt(key);
		String res_encrypt = crypt.encrypt(input);
		return res_encrypt;

	}
	public static String decrypt(String key, String input) throws Exception {
		CookieCrypt crypt = new CookieCrypt(key);
		String res_decrypt = crypt.decrypt(input);
		return res_decrypt;

	}
}