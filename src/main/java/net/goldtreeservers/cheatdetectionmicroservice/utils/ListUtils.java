package net.goldtreeservers.cheatdetectionmicroservice.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtils
{
	@SafeVarargs
	public static <T> List<T> unmodifiableListAdd(List<T> list, T... values)
	{
		if (values.length == 0)
		{
			return list;
		}
		
		if (list.size() == 0)
		{
			ArrayList<T> newList = new ArrayList<T>(values.length);
			
			for(T value : values)
			{
				newList.add(value);
			}
			
			return Collections.unmodifiableList(newList);
		}
		
		List<T> newList = new ArrayList<T>(list.size() + values.length);
		newList.addAll(list);
		
		for(T value : values)
		{
			newList.add(value);
		}
		
		return Collections.unmodifiableList(newList);
	}
	
	public static <T> List<T> unmodifiableListAdd(List<T> list, List<T> otherList)
	{
		if (otherList.size() == 0)
		{
			return list;
		}
		
		if (list.size() == 0)
		{
			ArrayList<T> newList = new ArrayList<T>(otherList.size());
			newList.addAll(otherList);
			
			return Collections.unmodifiableList(newList);
		}
		
		List<T> newList = new ArrayList<T>(list.size() + otherList.size());
		newList.addAll(list);
		newList.addAll(otherList);
		
		return Collections.unmodifiableList(newList);
	}
}
