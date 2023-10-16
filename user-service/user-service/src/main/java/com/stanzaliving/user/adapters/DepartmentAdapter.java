package com.stanzaliving.user.adapters;

import java.util.ArrayList;
import java.util.List;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.enums.EnumListing;

import lombok.experimental.UtilityClass;

/**
 * @author piyush srivastava "piyush@stanzaliving.com"
 *
 * @date 16-Apr-2020
 *
 */
@UtilityClass
public class DepartmentAdapter {

	public List<EnumListing<Department>> getDepartmentEnumAsEnumListing() {

		List<EnumListing<Department>> data = new ArrayList<>();

		for (Department department : Department.values()) {
			data.add(EnumListing.of(department, department.getDepartmentName()));
		}
		
		return data;
	}
}