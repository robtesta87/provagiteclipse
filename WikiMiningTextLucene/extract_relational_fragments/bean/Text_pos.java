package bean;

import java.util.Arrays;

public class Text_pos {
	private String[] words;
	private String[] pos;
	/**
	 * @return the words
	 */
	public String[] getWords() {
		return words;
	}
	/**
	 * @param words the words to set
	 */
	public void setWords(String[] words) {
		this.words = words;
	}
	/**
	 * @return the pos
	 */
	public String[] getPos() {
		return pos;
	}
	/**
	 * @param pos the pos to set
	 */
	public void setPos(String[] pos) {
		this.pos = pos;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(pos);
		result = prime * result + Arrays.hashCode(words);
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
		Text_pos other = (Text_pos) obj;
		if (!Arrays.equals(pos, other.pos))
			return false;
		if (!Arrays.equals(words, other.words))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "text_pos [words=" + Arrays.toString(words) + ", pos="
				+ Arrays.toString(pos) + "]";
	}
	
	public String getText(){
		StringBuilder text_builder = new StringBuilder();
		for (String word : words) {
			//text_builder.append(word.toLowerCase()+" ");
			text_builder.append(word+" ");
		}
		return text_builder.toString();
	}
	
}
