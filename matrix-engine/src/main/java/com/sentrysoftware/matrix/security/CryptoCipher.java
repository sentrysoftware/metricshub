package com.sentrysoftware.matrix.security;

import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoCipher {

	private CryptoCipher() {

	}

	public static final int GCM_IV_LENGTH = 16;
	public static final int GCM_TAG_LENGTH = 16;
	public static final int ITERATIONS = 2333;
	public static final int KEY_LENGTH = 32;

	public static final String MASTER_KEY = "bWFzdGVyLWtleQ==";
	private static final byte[] IV = "c2VudHJ5aXY=".getBytes();
	private static final String CIPHER_ALGO = "AES/GCM/NoPadding";

	public static char[] encrypt(char[] plainText, char[] passPhrase) throws HardwareCipherException {

		final byte[] decodedKey = Base64.getDecoder().decode(new String (passPhrase));
		final SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		final byte[] cipherText = encrypt(new String(plainText).getBytes(), secretKey, IV);
		final byte[] clippedCipherText = Arrays.copyOfRange(cipherText, 0, cipherText.length - (128 / Byte.SIZE));

		final byte[] tagVal = Arrays.copyOfRange(cipherText, cipherText.length - (128 / Byte.SIZE), cipherText.length);

		try {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(IV);
			outputStream.write(tagVal);
			outputStream.write(clippedCipherText);

			final byte[] addedEncyptedVal = outputStream.toByteArray();

			return Base64.getEncoder().encodeToString(addedEncyptedVal).toCharArray();
		} catch (Exception e) {
			throw new HardwareCipherException("IOException detected when building final encrypted text", e);
		}

	}

	private static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv) throws HardwareCipherException {

		try {
			final Cipher cipher = Cipher.getInstance(CIPHER_ALGO);

			final SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

			final GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

			cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

			return cipher.doFinal(plaintext);
		} catch (Exception e) {
			throw new HardwareCipherException("Cannot perform encryption", e);
		}

	}

	public static char[] decrypt(char[] crypted, char[] passPhrase) throws HardwareCipherException {

		final byte[] decodedCrypt = Base64.getDecoder().decode(new String(crypted));
		final byte[] clippedTaggedCipherText = Arrays.copyOfRange(decodedCrypt, IV.length, decodedCrypt.length);
		final byte[] clippedCipherText = Arrays.copyOfRange(clippedTaggedCipherText, (128 / Byte.SIZE),
				clippedTaggedCipherText.length);
		final byte[] tagVal = Arrays.copyOfRange(clippedTaggedCipherText, 0, (128 / Byte.SIZE));

		final byte[] cipherText;

		try {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(clippedCipherText);
			outputStream.write(tagVal);

			cipherText = outputStream.toByteArray();

		} catch (Exception e) {
			throw new HardwareCipherException("IOException detected when building the cipher text", e);
		}

		final byte[] decodedKey = Base64.getDecoder().decode(new String(passPhrase));
		final SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		return decrypt(cipherText, secretKey, IV);
	}

	private static char[] decrypt(byte[] cipherText, SecretKey key, byte[] iv) throws HardwareCipherException {

		try {
			final Cipher cipher = Cipher.getInstance(CIPHER_ALGO);

			final SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

			final GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

			byte[] decryptedText = cipher.doFinal(cipherText);

			return new String(decryptedText).toCharArray();
		} catch (Exception e) {
			throw new HardwareCipherException("Cannot perform decryption", e);
		}

	}

	public static char[] generateRandomKeyFromMaster() throws HardwareCipherException  {

		try {
			final byte[] salt = getSalt();

			final char[] masterKey = MASTER_KEY.toCharArray();
			final PBEKeySpec spec = new PBEKeySpec(masterKey, salt, ITERATIONS, KEY_LENGTH * 8);
			final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			return Base64.getEncoder().encodeToString(skf.generateSecret(spec).getEncoded()).toCharArray();
		} catch (Exception e) {
			throw new HardwareCipherException("Error while building the master key", e);
		}

	}

	private static byte[] getSalt() throws NoSuchAlgorithmException {

		final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		final byte[] salt = new byte[64];
		sr.nextBytes(salt);
		return salt;
	}

}