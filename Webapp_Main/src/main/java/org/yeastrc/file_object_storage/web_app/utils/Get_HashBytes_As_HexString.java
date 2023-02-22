package org.yeastrc.file_object_storage.web_app.utils;

import org.slf4j.LoggerFactory;  import org.slf4j.Logger;

public class Get_HashBytes_As_HexString {

	private static final Logger log = LoggerFactory.getLogger(Get_HashBytes_As_HexString.class);

	/**
	 * private constructor
	 */
	private Get_HashBytes_As_HexString(){}
	public static Get_HashBytes_As_HexString getInstance( ) throws Exception {
		Get_HashBytes_As_HexString instance = new Get_HashBytes_As_HexString();
		return instance;
	}

	/**
	 * @param hashBytes
	 * @return
	 */
	public String get_HashBytes_As_HexString( byte[] hashBytes ) {

		StringBuilder hashBytesAsHexSB = new StringBuilder( hashBytes.length * 2 + 2 );

		for ( int i = 0; i < hashBytes.length; i++ ) {
			String byteAsHex = Integer.toHexString( Byte.toUnsignedInt( hashBytes[ i ] ) );
			if ( byteAsHex.length() == 1 ) {
				hashBytesAsHexSB.append( "0" ); //  Leading zero dropped by 'toHexString' so add here
			}
			hashBytesAsHexSB.append( byteAsHex );
		}

		String result = hashBytesAsHexSB.toString();

		return result;
		
		//  WAS - which is equivalent, except for the added "0" when a hex pair starts with "0"
		
		//convert the byte to hex format
//		StringBuffer sb = new StringBuffer("");
//		for (int i = 0; i < hashBytes.length; i++) {
//			sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
//		}
//		
//		String result = sb.toString();
//		
//		return result;
	}
	

}
