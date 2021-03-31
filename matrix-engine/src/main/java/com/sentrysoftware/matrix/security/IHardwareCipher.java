package com.sentrysoftware.matrix.security;

public interface IHardwareCipher {

	/**
	 * encrypt given string with the given passPhrase and encode it into base64
	 * 
	 * @param str
	 * @param passPhrase
	 * @return
	 * @throws HardwareCipherException
	 */
	char[] encrypt(char[] str, char[] passPhrase) throws HardwareCipherException;

	/**
	 * encrypt given string with the given passPhrase, encode it into base64 and
	 * return result, wrapped into { }
	 * decorations
	 * 
	 * @param str
	 * @param passPhrase
	 * @return
	 * @throws HardwareCipherException
	 */
	char[] encryptAndDecorate(char[] str, char[] passPhrase) throws HardwareCipherException;

	/**
	 * decrypt given base64 encrypted string
	 * 
	 * @param str
	 * @param passPhrase
	 * @return
	 * @throws HardwareCipherException
	 */
	char[] decrypt(char[] str, char[] passPhrase) throws HardwareCipherException;

	/**
	 * decrypt given base64 encoded encrypted string. If string is decorated,
	 * decrypt base64 encoded string inside
	 * decorations
	 * 
	 * @param str
	 * @param passPhrase
	 * @return
	 * @throws HardwareCipherException
	 */
	char[] decryptDecorated(char[] str, char[] passPhrase) throws HardwareCipherException;

	/**
	 * check if given string is decorated
	 * 
	 * @param str
	 * @return
	 */
	public boolean isEncryptedString(char[] str);

	/**
	 * return string inside decorations
	 * 
	 * @param str
	 * @return
	 * @throws HardwareCipherException
	 */
	public char[] unDecorate(char[] str) throws HardwareCipherException;

	/**
	 * decorated given string with { and }
	 * 
	 * @param str
	 * @return
	 */
	public char[] decorate(char[] str);

}