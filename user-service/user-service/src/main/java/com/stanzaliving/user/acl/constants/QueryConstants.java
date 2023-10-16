/**
 * 
 */
package com.stanzaliving.user.acl.constants;

import lombok.experimental.UtilityClass;

/**
 * @author naveen.kumar
 *
 * @date 22-Oct-2019
 *
 **/
@UtilityClass
public class QueryConstants {

	public static String STATUS = "status";

	public static class Api {

		public static String API_NAME = "apiName";
		public static String API_URL = "actionUrl";
		public static String SERVICE = "service";

	}

	public static class Role {

		public static String ROLE_UUID = "uuid";
		public static String ROLE_NAME = "roleName";
		public static String DEPARTMENT = "department";
		public static String ACCESS_LEVEL = "accessLevel";
	}
}