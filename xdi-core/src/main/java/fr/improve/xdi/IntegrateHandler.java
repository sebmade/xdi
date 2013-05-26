package fr.improve.xdi;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;

import fr.improve.xdi.mapping.EnterpriseObject;

/**
 * Interface to implement for handling import event process
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public interface IntegrateHandler extends Serializable {
	public void setErrorHandler(IntegrateErrorHandler in_errorHandler);
    public void setLogName(String in_name);
    public void setLog(Log in_log);
	public IntegrateErrorHandler errorHandler();
	public void validate() throws IntegrateException;
	public boolean searchOnly();
	public EnterpriseObject search() throws IntegrateException;
	public EnterpriseObject selectObjectInFindedObjects(List in_findedObjects);
	public EnterpriseObject willBindValues(EnterpriseObject in_enterpriseObject) throws IntegrateException;
	public boolean willTakeValueForKey(Object in_value, String in_key);
	public void didTakeValueForKey(Object in_value, String in_key);
	public void didBindValues(EnterpriseObject in_enterpriseObject) throws IntegrateException;
    public void setDecoder(XMLDecoder in_decoder);
}
