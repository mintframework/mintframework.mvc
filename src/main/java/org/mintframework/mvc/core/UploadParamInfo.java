package org.mintframework.mvc.core;

public class UploadParamInfo {
	public final long limitSize;
	public final int paramIndex;
	// 0:TempFile, 1:TempFile[], 2:List<TempFile>
	public final int paramType;
	
	UploadParamInfo(long limitSize, int paramIndex, int paramType) {
		this.limitSize = limitSize;
		this.paramIndex = paramIndex;
		this.paramType = paramType;
	}
}
