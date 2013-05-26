package fr.improve.xdi.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.w3c.dom.Document;

/**
 * Directory service
 * 
 * @author Sébastien Letélié <s.letelie@improve.fr>
 *
 */
public class FileBoxService implements Service {

    /** 
     * @see fr.improve.xdi.service.Service#callService(java.util.Hashtable)
     */
    public Document callService(Hashtable in_parameters) throws ServiceException {
        try {
            String l_path = (String)in_parameters.get(Service.FILEBOX_PATH);
            String l_filename = (String)in_parameters.get(Service.FILEBOX_FILENAME);
            FileOutputStream l_outputStream = new FileOutputStream(new File(l_path, l_filename));
            l_outputStream.write((byte[])in_parameters.get(Service.FILEBOX_CONTENT));
            l_outputStream.close();
        } catch (IOException e) {
            new ServiceException(e);
        }
        return null;
    }

}
