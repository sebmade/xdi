package fr.improve.xdi.mapping;

import java.io.Serializable;

import fr.improve.xdi.mapping.exception.EnterpriseContextException;

/**
 * Delegate for handling commit actions
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public interface EnterpriseContextHandler extends Serializable {
    public void willSaveChanges(EnterpriseContext in_enterpriseContext) throws EnterpriseContextException;
    public void didSaveChanges(EnterpriseContext in_enterpriseContext) throws EnterpriseContextException;
}
