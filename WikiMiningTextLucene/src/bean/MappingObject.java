package bean;

public class MappingObject {
	private String wikid;
	private String mid;
	private String types;
	private String keywords;
	
	/**
	 * 
	 * @param wikid
	 * @param mid
	 * @param types
	 * @param keywords
	 */
	public MappingObject(String wikid, String mid, String types, String keywords) {
		super();
		this.wikid = wikid;
		this.mid = mid;
		this.types = types;
		this.keywords = keywords;
	}
	/**
	 * @return the wikid
	 */
	public String getWikid() {
		return wikid;
	}
	/**
	 * @param wikid the wikid to set
	 */
	public void setWikid(String wikid) {
		this.wikid = wikid;
	}
	/**
	 * @return the mid
	 */
	public String getMid() {
		return mid;
	}
	/**
	 * @param mid the mid to set
	 */
	public void setMid(String mid) {
		this.mid = mid;
	}
	/**
	 * @return the types
	 */
	public String getTypes() {
		return types;
	}
	/**
	 * @param types the types to set
	 */
	public void setTypes(String types) {
		this.types = types;
	}
	/**
	 * @return the keywords
	 */
	public String getKeywords() {
		return keywords;
	}
	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result + ((mid == null) ? 0 : mid.hashCode());
		result = prime * result + ((types == null) ? 0 : types.hashCode());
		result = prime * result + ((wikid == null) ? 0 : wikid.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingObject other = (MappingObject) obj;
		if (keywords == null) {
			if (other.keywords != null)
				return false;
		} else if (!keywords.equals(other.keywords))
			return false;
		if (mid == null) {
			if (other.mid != null)
				return false;
		} else if (!mid.equals(other.mid))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		if (wikid == null) {
			if (other.wikid != null)
				return false;
		} else if (!wikid.equals(other.wikid))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MappingObject [wikid=" + wikid + ", mid=" + mid + ", types="
				+ types + ", keywords=" + keywords + "]";
	}
	
	
}
