package mint.mvc.core;

public interface ServiceChain {
	
	/**
     * Apply next service around the execution of Action.
     * 
     * @param execution Execution to execute.
     * @throws Exception Any exception if error occured.
     */
	void doService(RequestContext ctx) throws Exception;
}