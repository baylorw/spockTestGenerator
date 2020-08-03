package com.baylorw.spockTestGenerator.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<T1, T2>
{
	private T1 first;
	private T2 second;
}
