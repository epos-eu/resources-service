package org.epos.api.clienthelpers.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash(value="cacheData", timeToLive = 3600L)
public class CacheData<T> {
    @Id
    private String key;

    @Indexed
    private T value;

	public CacheData(String key, T value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
    
    
    
}