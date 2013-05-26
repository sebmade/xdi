package fr.improve.xdi;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.w3c.dom.Node;

import fr.improve.xdi.mapping.EnterpriseContext;

/**
 * Interface to implement for decoding XML element by type
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public interface XMLDecoder extends Serializable  {
    public EnterpriseContext getEnterpriseContext();
	public Object decodeObjectForNode(Node in_node) throws DecodeException, IntegrateException;
    public void setDateFormat(String in_pattern);
    public String getDateFormat();
    public void setLogName(String in_name);
    public void setLog(Log in_log);
	public void setErrorHandler(IntegrateErrorHandler in_errorHandler);
	public IntegrateErrorHandler errorHandler();
	public void setBinaryHandler(BinaryHandler in_binaryHandler);
	public BinaryHandler binaryHandler();
    public void setDefaultIntegrateHandler(Class<? extends IntegrateHandler> defaultIntegrateHandler);
}
