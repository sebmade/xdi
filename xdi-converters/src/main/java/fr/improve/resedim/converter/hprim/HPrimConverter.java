package fr.improve.resedim.converter.hprim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

import fr.improve.xdi.Integrator;
import fr.improve.xdi.converter.Converter;
import fr.improve.xdi.converter.ConverterException;

public class HPrimConverter implements Converter {
    private PrintWriter _xml = null;
    //private String _tmpPath = null;
    //private int _countTmpFile = 0;
    //private boolean _hasAnalyse = false;
    private NSDictionary _segments;
    private int _selectorIteration = 0;
    private String _token;
    private String _subToken;
    private String _subSubToken;
    private String _repeat;
    private int _countPatient = 0;
    static final String CR = "#CR#";

    public HPrimConverter() {
        //_tmpPath = System.getProperty("TEMP") + "/hprim_xml_tmp";
        _segments = (NSDictionary)NSPropertyListSerialization.propertyListWithPathURL(getClass().getResource("HPrim21.dict"));
    }

    public void setSegments(NSDictionary segments) {
        if (segments != null)
            _segments = segments;
    }
    
    public void addXMLElementWithDictionary(NSArray in_values, NSDictionary in_segment) {
        //System.out.println("in_values : "+in_values);
        //System.out.println("in_segment : "+in_segment);
        String l_key = (String) in_segment.allKeys().lastObject();

        //System.out.println("l_key : "+l_key);
        NSArray l_elements = (NSArray) in_segment.objectForKey(l_key);

        //System.out.println("l_elements : "+l_elements);
        int i;

        if (l_key.equals("patient") && (_countPatient++ > 0)) {
        	_xml.println("</patient>");
            //_xml.print("</patient></HPrim>");
            //_xml.close();

            //if (!_hasAnalyse) {
            //    _countTmpFile--;
            //}

            //_xml = new PrintWriter(new BufferedWriter(new FileWriter(_tmpPath + _countTmpFile++)));
            //_xml.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
            //_xml.println("<HPrim>");
            //_hasAnalyse = false;
        }

        if (l_key.equals("analyse")) {
            //_hasAnalyse = true;
        }

        _selectorIteration++;
        _xml.print("<");
        _xml.print(l_key);
        _xml.print(">\n");

        for (i = 0; i < in_values.count() && i < l_elements.count(); i++) {
            String l_value = (String) in_values.objectAtIndex(i);

            //System.out.println("l_value : "+l_value);
            Object l_element = l_elements.objectAtIndex(i);

            //System.out.println("l_element : "+l_element);
            //System.out.println("_xml : "+_xml.length());
            if ((l_value != null) && !l_value.equals("")) {
                if (l_element instanceof NSDictionary) {
                    NSArray repeat; 
                    repeat=componentsSeparatedByString(l_value, _repeat);
                    if (repeat.count()>1 && _selectorIteration==1)
                    {
                    for (int repeati=0;repeati<repeat.count();repeati++)
                      {
                        
                      addXMLElementWithDictionary(componentsSeparatedByString((String)repeat.objectAtIndex(repeati), _subToken),(NSDictionary) l_element);
                      }
                    }
                    if (repeat.count()==1 &&_selectorIteration > 1 ) {
                        addXMLElementWithDictionary(componentsSeparatedByString(l_value, _subSubToken), (NSDictionary) l_element);
                    } 
                    if (repeat.count()==1 &&_selectorIteration <=1 ) {
                        addXMLElementWithDictionary(componentsSeparatedByString(l_value, _subToken), (NSDictionary) l_element);
                    }
                } else {
                    String l_tag = (String) l_elements.objectAtIndex(i);
                    _xml.print("<");
                    _xml.print(l_tag);
                    _xml.print(">");

                    String l_tValue = transform(l_value, l_tag);

                    if ((l_value.indexOf('<') != -1) || (l_value.indexOf('>') != -1) || (l_value.indexOf('&') != -1)) {
                        _xml.print("<![CDATA[");
                        _xml.print(StringUtils.replace(l_tValue, CR, "\n"));
                        _xml.print("]]>");
                    } else {
                        _xml.print(StringUtils.replace(l_tValue, CR, "\n"));
                    }

                    _xml.print("</");
                    _xml.print(l_tag);
                    _xml.println(">");
                }
            }
        }

        if (!l_key.equals("patient")) {
            _xml.print("</");
            _xml.print(l_key);
            _xml.print(">\n");
        }

        _selectorIteration--;
    }

    public NSArray componentsSeparatedByString(String s, String s1) {
        if (s == null)
            return new NSArray();

        int i = s.length();
        int j = (s1 == null) ? 0 : s1.length();

        if ((i == 0) || (j == 0))
            return new NSArray(s);

        int k = 0;
        int j1 = 0;
        NSMutableArray nsmutablearray;

        if (j == 1) {
            char[] ac = s.toCharArray();
            char c = s1.charAt(0);

            for (int k1 = 0; k1 < i; k1++) {
                if (ac[k1] == c)
                    j1++;
            }

            if (j1 == 0)
                return new NSMutableArray(s);

            nsmutablearray = new NSMutableArray();

            int l;

            for (l = 0; l < i; l++) {
                if (ac[l] == c) {
                    if (ac[k] != c)
                        nsmutablearray.addObject(s.substring(k, l));
                    else
                        nsmutablearray.addObject("");

                    k = l + 1;
                }
            }

            if (k < i)
                nsmutablearray.addObject(s.substring(k, l));
        } else {
            nsmutablearray = new NSMutableArray();

            int i1;

            for (; k < i; k = i1 + j) {
                i1 = s.indexOf(s1, k);

                if (i1 < 0)
                    i1 = i;

                nsmutablearray.addObject(s.substring(k, i1));
            }
        }

        return nsmutablearray;
    }

    public String transform(String in_str, String in_tag) {
        return _replaceStringByStringForString("\\XC\\", "\n", _replaceStringByStringForString(_repeat, "</"+in_tag+"><"+in_tag+">", in_str));
    }

    // remplacement des caracteres repeat et escape
    private String _replaceStringByStringForString(String in_replace, String in_by, String in_str) {
        int index1;

        while ((index1 = in_str.indexOf(in_replace)) != -1) {
            int index2 = index1 + in_replace.length();
            String l_str1 = in_str.substring(0, index1);
            String l_str2 = in_str.substring(index2);
            in_str = l_str1 + in_by + l_str2;
        }

        return in_str;
    }

	/**
	 * @see fr.improve.xdi.converter.Converter#convertToXML(java.io.Reader, java.io.Writer)
	 */
	public void convertToXML(Reader in_reader, Writer in_writer) throws ConverterException {
		try {
			LineNumberReader l_scanner = new LineNumberReader(in_reader);
			String l_line = l_scanner.readLine();
            _xml = new PrintWriter(new BufferedWriter(in_writer));
            _xml.println("<?xml version=\"1.0\"?>");
            _xml.println("<HPrim>");
			if (l_line != null) {
                StringBuffer l_content = new StringBuffer();
                // recuperation des separateurs
                _token = l_line.substring(1, 2);
                _subToken = l_line.substring(2, 3);
                _subSubToken = l_line.substring(5, 6);
                _repeat = l_line.substring(3, 4);
                
                //System.out.println(""+(_token)+" "+(_subToken)+" "+(_subSubToken));
                // concatenation des segments A avec le segment précédent
                boolean isComment = false;
                while ((l_line = l_scanner.readLine()) != null) {
                    if (l_line.length() > 0) {
                        if (l_line.startsWith("C"+_token)) {
                            // si c'est un commentaire je dois ajouter un retour chariot aux A suivants
                            isComment = true;
                            if (l_scanner.getLineNumber() > 2) {
                                l_content.append('\n');
                            }
                            l_content.append(l_line);
                        } else if (l_line.startsWith("A"+_token)) {
                            // ajout du retour chariot (on evite d'en mettre un en dernière ligne)
                            if (isComment) {
                                l_content.append(CR);
                            }
                            l_content.append(l_line.substring(2));
                        } else {
                            isComment = false;
                            //System.out.println("n : "+l_scanner.getLineNumber());
                            if (l_scanner.getLineNumber() > 2) {
                                l_content.append('\n');
                            }

                            l_content.append(l_line);
                        }
                    }
                }
                
                l_scanner.close();
                
                //System.out.println("content : "+l_content);
                //_xml = new PrintWriter(new BufferedWriter(new FileWriter(_tmpPath + _countTmpFile++)));
                l_scanner = new LineNumberReader(new StringReader(l_content.toString()));
                
                while ((l_line = l_scanner.readLine()) != null) {
                    if (l_line.length() > 0) {
                        NSMutableArray l_tmp = new NSMutableArray(componentsSeparatedByString(l_line, _token));
                        String l_key = (String) l_tmp.objectAtIndex(0);
                    
                        if (!l_key.equals("L")) {
                            l_tmp.removeObjectAtIndex(0);
                            NSDictionary l_segment = (NSDictionary) _segments.objectForKey(l_key);
                            if (l_segment != null) {
                                addXMLElementWithDictionary(l_tmp, l_segment);
                            } else {
                                System.out.println("segment inconnu : "+l_key);
                            }
                        }
                    }
                }
                
                l_scanner.close();
                _xml.print("</patient>");
            }
            _xml.print("</HPrim>");
            _xml.close();
		} catch (IOException e) {
			throw new ConverterException(e);
		}

		//if (!_hasAnalyse) {
		//    _countTmpFile--;
		//}

		//System.out.println("tmp files : " + _countTmpFile);

		/*Vector l_fileReaders = new Vector();

		for (int i = 0; i < _countTmpFile; i++) {
			l_fileReaders.addElement(new FileReader(_tmpPath + i));
		}

		return l_fileReaders.elements();*/
	}

	/**
	 * @see fr.improve.xdi.converter.Converter#convertAndIntegrate(java.io.Reader, java.io.Reader, boolean)
	 */
	public void convertAndIntegrate(Integrator in_integrator, Reader in_file, Reader in_xsl, boolean in_withSchemaValidation) throws ConverterException {
		try {
			if (in_xsl == null) {
				in_xsl = new InputStreamReader(getClass().getResourceAsStream("HPrim21.xsl"));
			}
			//PipedWriter l_pipeOut = new PipedWriter();
			//PipedReader l_pipeIn = new PipedReader(l_pipeOut);
			File l_tmpFile = File.createTempFile("hprim", "xml");
			convertToXML(in_file, new FileWriter(l_tmpFile));
			in_integrator.transformAndIntegrate(new FileReader(l_tmpFile), in_xsl, in_withSchemaValidation);
            l_tmpFile.delete();
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	/**
	 * @see fr.improve.xdi.converter.Converter#convertFromXML(java.io.Reader, java.io.Writer)
	 */
	public void convertFromXML(Reader in_reader, Writer in_writer) throws ConverterException {
	}

    /* 
     * @see fr.improve.resedim.converter.Converter#convertFromXML(org.w3c.dom.Document, java.io.Writer)
     */
    public void convertFromXML(Document in_document, Writer in_writer) throws ConverterException {
        // TODO Auto-generated method stub
        
    }

    /* 
     * @see fr.improve.resedim.converter.Converter#setConfig(java.net.URL)
     */
    public void setConfig(URL in_filePath) throws IOException {
        
    }
    public void setConfig(Properties in_properties) throws IOException {
    }

    /* 
     * @see fr.improve.resedim.converter.Converter#getConfig()
     */
    public Properties getConfig() {
        return null;
    }
}
