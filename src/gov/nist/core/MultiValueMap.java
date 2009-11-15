package gov.nist.core;

import java.util.List;
import java.util.Map;

public interface MultiValueMap<K,V> extends Map<K,List<V>> {
    public Object remove( K key, V item );
}
