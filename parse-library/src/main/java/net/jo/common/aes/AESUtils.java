package net.jo.common.aes;

import net.jo.common.HexUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * @Title: AESUtils.java
 * @author:JO
 * @Company:JO
 * @copyright:JO
 * @version V2.0
 * @description
 */
public class AESUtils {
	private final static String AES = "AES";

	public static byte[] encrypt(byte[] data, byte[] key,String transformation){
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key, AES);
			Cipher localCipher = Cipher.getInstance(transformation);
			localCipher.init(1, secretKey);
			return localCipher.doFinal(data);
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public static byte[] decrypt(byte[] data, byte[] key,String transformation){
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key, AES);
			Cipher localCipher = Cipher.getInstance(transformation);
			localCipher.init(2, secretKey);
			return localCipher.doFinal(data);
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}

	public static byte[] encrypt(byte[] data, byte[] key,byte[] iv,String transformation){
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key, AES);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
	        Cipher localCipher = Cipher.getInstance(transformation);
			localCipher.init(Cipher.ENCRYPT_MODE, secretKey,ivParameterSpec);
			return localCipher.doFinal(data);
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public static byte[] decrypt(byte[] data, byte[] key,byte[] iv,String transformation){
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key, AES);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
	        Cipher localCipher = Cipher.getInstance(transformation);
			localCipher.init(Cipher.DECRYPT_MODE, secretKey,ivParameterSpec);
			return localCipher.doFinal(data);
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}


	//int keySize = 256;
	//int ivSize = 128;
	public static byte[] decrypt(byte[] data, byte[] key,int keySize,int ivSize,String transformation){
		try {
			byte[] saltBytes = Arrays.copyOfRange(data, 0, 8);
			byte[] ciphertextBytes = Arrays.copyOfRange(data, 8, data.length);

			byte[] dynamic_key = new byte[keySize/8];
			byte[] iv = new byte[ivSize/8];
			EvpKDF(key, keySize, ivSize, saltBytes, dynamic_key, iv);
			Cipher cipher = Cipher.getInstance(transformation);
			if(transformation.contains("/ECB/")){
				cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(dynamic_key, AES));
			} else {
				cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(dynamic_key, AES), new IvParameterSpec(iv));
			}
			return cipher.doFinal(ciphertextBytes);
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}

	public static byte[] encrypt(byte[] data, byte[] key,int keySize,int ivSize,String transformation){
		try {
			SecureRandom sr = new SecureRandom();
			byte[] saltBytes = new byte[8];
			sr.nextBytes(saltBytes);

			byte[] dynamic_key = new byte[keySize/8];
			byte[] iv = new byte[ivSize/8];
			EvpKDF(key, keySize, ivSize, saltBytes, dynamic_key, iv);
			Cipher cipher = Cipher.getInstance(transformation);
			if(transformation.contains("/ECB/")){
				cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(dynamic_key, AES));
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(dynamic_key, AES), new IvParameterSpec(iv));
			}
			byte[] encryptedData = cipher.doFinal(data);
			byte[] prefixAndSaltAndEncryptedData = new byte[16 + encryptedData.length];
			// Copy prefix (0-th to 7-th bytes)
			System.arraycopy("Salted__".getBytes("UTF-8"), 0, prefixAndSaltAndEncryptedData, 0, 8);
			// Copy salt (8-th to 15-th bytes)
			System.arraycopy(saltBytes, 0, prefixAndSaltAndEncryptedData, 8, 8);
			// Copy encrypted data (16-th byte and onwards)
			System.arraycopy(encryptedData, 0, prefixAndSaltAndEncryptedData, 16, encryptedData.length);
			return prefixAndSaltAndEncryptedData;
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}

	public static byte[] EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
		return EvpKDF(password, keySize, ivSize, salt, 1, "MD5", resultKey, resultIv);
	}

	public static byte[] EvpKDF(byte[] password, int keySize, int ivSize, byte[] salt, int iterations, String hashAlgorithm, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
		keySize = keySize / 32;
		ivSize = ivSize / 32;
		int targetKeySize = keySize + ivSize;
		byte[] derivedBytes = new byte[targetKeySize * 4];
		int numberOfDerivedWords = 0;
		byte[] block = null;
		MessageDigest hasher = MessageDigest.getInstance(hashAlgorithm);
		while (numberOfDerivedWords < targetKeySize) {
			if (block != null) {
				hasher.update(block);
			}
			hasher.update(password);
			block = hasher.digest(salt);
			hasher.reset();
			System.out.println(HexUtils.byte2hex(block));
			// Iterations
			for (int i = 1; i < iterations; i++) {
				block = hasher.digest(block);
				hasher.reset();
			}
			System.out.println(HexUtils.byte2hex(block));
			System.arraycopy(block, 0, derivedBytes, numberOfDerivedWords * 4, Math.min(block.length, (targetKeySize - numberOfDerivedWords) * 4));
			numberOfDerivedWords += block.length/4;
		}

		System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4);
		System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4);
		return derivedBytes; // key + iv
	}

	static class CryptoProvider extends Provider {
		public CryptoProvider() {
			super("Crypto", 1.0, "HARMONY (SHA1 digest; SecureRandom; SHA1withDSA signature)");
			put("SecureRandom.SHA1PRNG", "org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl");
			put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
		}
	}
}
