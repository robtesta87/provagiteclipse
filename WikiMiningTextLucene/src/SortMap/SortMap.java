package SortMap;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class SortMap {
	public <K, V > Map<K, V> sortByValues(final Map<K, V> map) {
		Comparator<K> valueComparator = 
				new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = k1.toString().length()-k2.toString().length();
				if (compare > 0) 
					return -1;
				else 
					return 1;
			}
		};

		Map<K, V> sortedByValues = 
				new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}
}
