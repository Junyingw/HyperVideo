public class HyperLink {
	private String name;
	private BoundingBox boxOnCurrentFrame;
	private String targetVideoDir;
	private int targetFrameNum;
	
	// TODO: experimenting area
	private int startFrameNum;
	private int endFrameNum;

	public int getStartFrameNum() {
		return startFrameNum;
	}

	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}

	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	public BoundingBox getBoxOnCurrentFrame() {
		return boxOnCurrentFrame;
	}
	
	public void setBoxOnCurrentFrame(BoundingBox boxOnCurrentFrame) {
		this.boxOnCurrentFrame = boxOnCurrentFrame;
	}

	public String getTargetVideoDir() {
		return targetVideoDir;
	}

	public void setTargetVideoDir(String targetVideoDir) {
		this.targetVideoDir = targetVideoDir;
	}

	public int getTargetFrameNum() {
		return targetFrameNum;
	}

	public void setTargetFrameNum(int targetFrameNum) {
		this.targetFrameNum = targetFrameNum;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "HyperLink [name=" + name + ", targetVideoDir=" + targetVideoDir + ", targetFrameNum=" + targetFrameNum
				+ "]";
	}
}
