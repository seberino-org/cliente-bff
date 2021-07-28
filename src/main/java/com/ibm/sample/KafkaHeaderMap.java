package com.ibm.sample;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.messaging.MessageHeaders;

import io.opentracing.propagation.TextMap;

public class KafkaHeaderMap implements TextMap {

	
	public KafkaHeaderMap() 
	{
		
	}
	public KafkaHeaderMap(MessageHeaders headers) {
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                if (!header.getKey().startsWith("tracer_context_")) {
                    continue;
                }
                String key = header.getKey().replaceFirst("^tracer_context_", "");
                String value = header.getValue().toString();
                map.put(key, value);
            }
        }
	
	private final Map<String, String> map = new HashMap<>();
	
	  
	@Override
	public void put(String key, String value) {
		map.put(key, value);
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		  return map.entrySet().iterator();
	}
	
	@Override
	public String toString()
	{
		StringBuilder s1 = new StringBuilder("headers: ");
		Iterator<Entry<String, String>> x1 = iterator();
		while (x1.hasNext())
		{
			Entry<String, String> y1 = x1.next();
			s1.append(y1.getKey() + "=" + y1.getValue() +", ");
		}
		return s1.toString();
	}

	public Entry<String, String> getContext()
	{
		Entry<String, String> retorno=null;
		Iterator<Entry<String, String>> x1 = iterator();
		while (x1.hasNext())
		{
			retorno = x1.next();
		}
		return retorno;
	}
}