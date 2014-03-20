component output="false" persistent="false" {

	remote Items function acceptNestedArray (Items items) {
		dump(var=arguments.items.items, output="D:\files.html");
		return arguments.items;
	}
}