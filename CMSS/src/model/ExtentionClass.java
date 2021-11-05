package model;

import java.io.Serializable;
import java.util.Hashtable;

public class ExtentionClass implements Serializable{
	private static final long serialVersionUID  = 7L;
	private Hashtable<String, String> list;
	
	public ExtentionClass() {
		list = new Hashtable<String, String>();
	}
	
	public void put(String key, String vaule) {
		list.put(key, vaule);
	}
	public void put(String key, long vaule) {
		list.put(key, "" + vaule);
	}
	public String get(String key) {
		return list.get(key);
	}
	public String remove(String key) {
		return list.remove(key);
	}
	public boolean containsKey(String key) {
		return list.containsKey(key);
	}
	public int size() {
		return list.size();
	}
	public Hashtable<String, String> getHashTable(){
		return this.list;
	}
}
