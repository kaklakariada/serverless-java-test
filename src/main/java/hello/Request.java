package hello;

public class Request {

	private String key1;
	private String key2;
	private String key3;

	public String getKey1() {
		return key1;
	}

	public String getKey2() {
		return key2;
	}

	public String getKey3() {
		return key3;
	}

	@Override
	public String toString() {
		return "Request [key1=" + key1 + ", key2=" + key2 + ", key3=" + key3 + "]";
	}
}
