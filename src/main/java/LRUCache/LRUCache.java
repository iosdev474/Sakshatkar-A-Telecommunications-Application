package LRUCache;

import java.util.*;

public class LRUCache<K , V> {
    Deque<K> dq;
    HashSet<K> map;
    static int csize;

    Map<K ,V> dataMap;

    public LRUCache(int size){
        dq = new LinkedList<>();
        map = new HashSet<>();
        dataMap = new HashMap<K,V>(size);
        csize = size;
    }

    public void replace(K k, V v){
        dataMap.replace(k,v);
        refer(k,v);
    }

    public V readValue(K key){
        return dataMap.get(key);
    }

    public V getValue(K key){
        refer(key,dataMap.get(key));
        return dataMap.get(key);
    }

    public void addValue(K key, V value){
        refer(key,dataMap.get(key));
        dataMap.put(key,value);
    }

    private boolean removeValue(K key, V value){
        return dataMap.remove(key , value);
    }

    private void refer(K x, V v){
        if (!map.contains(x)) {
            if (dq.size() == csize) {
                K last = dq.removeLast();
                map.remove(last);
                dataMap.remove(last);
            }
        }
        else {

            int index = 0, i = 0;
            Iterator<K> itr = dq.iterator();
            while (itr.hasNext()) {
                if (itr.next() == x) {
                    index = i;
                    break;
                }
                i++;
            }
            dq.remove(index);
            dataMap.remove(index);

        }
        dq.push(x);
        map.add(x);
        dataMap.put(x,v);

    }

    private void display()
    {
        System.out.println(dq);
    }

    public static void main(String[] args){}

}

