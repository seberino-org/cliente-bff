package com.ibm.sample;

import java.util.Iterator;
import java.util.Map.Entry;

import org.springframework.http.HttpHeaders;

import io.opentracing.propagation.TextMap;

public class HttpHeaderInjectAdapter implements TextMap
{
    private final HttpHeaders headers;

    public HttpHeaderInjectAdapter(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String key, String value) {
        headers.set(key, value);
    }

    public HttpHeaders getHeaders()
    {
        return this.headers;
    }
}
