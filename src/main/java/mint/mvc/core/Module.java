package mint.mvc.core;

import java.util.Set;

public class Module {
	private Set<API> apiSet;

	protected Set<API> getApiSet() {
		return apiSet;
	}

	public void setApiSet(Set<API> apiSet) {
		this.apiSet = apiSet;
	}
}
