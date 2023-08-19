package com.tarento.upsmf.examsAndAdmissions.util;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class CryptoUtil {
	private static final Charset US_ASCII = Charset.forName("US-ASCII");

	public static boolean verifyRSASign(String payLoad, byte[] signature, PublicKey key, String algorithm) {
		Signature sign;
		try {
			sign = Signature.getInstance(algorithm);
			sign.initVerify(key);
			sign.update(payLoad.getBytes(US_ASCII));
			return sign.verify(signature);
		} catch (NoSuchAlgorithmException e) {
			return false;
		} catch (InvalidKeyException e) {
			return false;
		} catch (SignatureException e) {
			return false;
		}
	}

}
