package fr.improve.resedim.converter.nx;

import com.resurgences.utils.ExceptionUtils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

import fr.improve.xdi.Integrator;
import fr.improve.xdi.converter.Converter;
import fr.improve.xdi.converter.ConverterException;

public class NxConverter implements Converter {
    static NSDictionary _struct;

    PrintWriter theWriter;
    DecoupeLigne theDecoupeLigne;
    String theLigne, tabulation = "", codeActe;
    LineNumberReader theLineReader;
    boolean withHistory = false;

    class DecoupeLigne {
        int type;
        String rubrique;
        String sequence;
        NSMutableArray ligne;
    }


    public NxConverter() {
        _struct = (NSDictionary) NSPropertyListSerialization.propertyListWithPathURL(getClass().getResource("Nx.dict"));
    }

    public DecoupeLigne decoupe_ligne(String in_ligne) {
        DecoupeLigne result = new DecoupeLigne();
        result.ligne = new NSMutableArray();
        if (in_ligne != null) {
            result.type = Integer.parseInt(in_ligne.substring(0, 3));
            result.rubrique = in_ligne.substring(3, 5);
            result.sequence = in_ligne.substring(5, 7);
            NSArray l_struct = (NSArray) _struct.objectForKey(in_ligne.substring(0, 5));
            if (l_struct != null) {
                int l_index = 7, l_nb, l_repeat;
                for (int l_i = 1; l_i < l_struct.size(); l_i++) {
                    if (l_struct.get(l_i) instanceof NSArray) {
                        l_nb = Integer.parseInt(((NSArray) l_struct.get(l_i)).get(0).toString());
                        l_repeat = Integer.parseInt(((NSArray) l_struct.get(l_i)).get(1).toString());
                        for (int l_j = 0; l_j < l_repeat; l_j++) {
                            if (l_index + l_nb < in_ligne.length())
                                result.ligne.addObject(in_ligne.substring(l_index, l_index + l_nb));
                            else
                                result.ligne.addObject("");
                            l_index += l_nb;
                        }

                    } else {
                        l_nb = Integer.parseInt(l_struct.get(l_i).toString());
                        if (l_index + l_nb < in_ligne.length())
                            result.ligne.addObject(in_ligne.substring(l_index, l_index + l_nb));
                        else
                            result.ligne.addObject("");

                        l_index += l_nb;
                    }

                }
            }
        }
        return (result);
    }

    private String _arrayDesc(DecoupeLigne decoupeLigne, int index, int counter) {
        return _arrayDesc(null, decoupeLigne, index, counter);
    }

    private String _arrayDesc(String balise, DecoupeLigne decoupeLigne, int index, int counter) {
        return _arrayDesc(balise, decoupeLigne, index, counter, false);
    }

    private String _arrayDesc(String balise, DecoupeLigne decoupeLigne, int index, int counter, boolean notNull) {
        StringBuffer sb = new StringBuffer();

        if (balise != null) {
            sb.append("<");
            sb.append(balise);
            sb.append(">(");
        }

        for (int l_i = 0; l_i < counter; l_i++) {
            String tmp = ((String) decoupeLigne.ligne.get(index + l_i)).trim();

            if (tmp.length() != 0 && (!notNull || (notNull && Integer.parseInt(tmp) != 0))) {
                if (l_i != 0) sb.append(",");
                sb.append("\"" + tmp + "\"");
            }
        }

        if (balise != null) {
            sb.append(")</");
            sb.append(balise);
            sb.append(">");
        }

        return sb.toString();
    }

    private String _numValue(String balise, DecoupeLigne decoupeLigne, int index, boolean notNull) {
        StringBuffer sb = new StringBuffer();

        if (balise != null) {
            sb.append("<");
            sb.append(balise);
            sb.append(">");
        }

        String tmp = ((String) decoupeLigne.ligne.get(index)).trim();
        if (tmp.length() != 0 && (!notNull || (notNull && Integer.parseInt(tmp) != 0)))
            sb.append(tmp);

        if (balise != null) {
            sb.append("</");
            sb.append(balise);
            sb.append(">");
        }

        return sb.toString();
    }

    public void traiter_101() throws IOException {
        if (theDecoupeLigne.rubrique.equals("01")) {
            codeActe = ((String) theDecoupeLigne.ligne.get(0)).trim();
            theWriter.println(tabulation + "<codeActe>" + ((String) theDecoupeLigne.ligne.get(0)).trim() + "</codeActe>");
            theWriter.println(tabulation + "<intituleCourt>" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1))).trim() + "</intituleCourt>");
            theWriter.println(tabulation + "<typeActe>" + ((String) theDecoupeLigne.ligne.get(2)).trim() + "</typeActe>");
            theWriter.println(tabulation + _numValue("sexeCompatible", theDecoupeLigne, 3, true));
            theWriter.println(tabulation + "<debutValide>" + ((String) theDecoupeLigne.ligne.get(4)).trim() + "</debutValide>");
            theWriter.println(tabulation + "<finValide>" + ((String) theDecoupeLigne.ligne.get(5)).trim() + "</finValide>");
        } else if (theDecoupeLigne.rubrique.equals("02")) {
            theWriter.println(tabulation + _arrayDesc("natureAssurancePermise", theDecoupeLigne, 0, 10));
            theWriter.println(tabulation + "<fraisDeplacement>" + ((String) theDecoupeLigne.ligne.get(10)).trim() + "</fraisDeplacement>");
        } else if (theDecoupeLigne.rubrique.equals("03")) {
            String l_arborescence = null;

            for (int l_i = 9; l_i >= 0; l_i--) {
                int l_a = Integer.parseInt((String) theDecoupeLigne.ligne.get(l_i));
                if (l_a != 0) {
                    if (l_arborescence == null)
                        l_arborescence = ((l_a <= 9) ? "0":"") + l_a;
                    else
                        l_arborescence = ((l_a <= 9) ? "0":"") + l_a + "." + l_arborescence;
                }
            }
            theWriter.println(tabulation + "<arborescence>" + l_arborescence + "</arborescence>");
            theWriter.println(tabulation + "<placeDansArborescence>"+((String) theDecoupeLigne.ligne.get(10)).trim()+"</placeDansArborescence>");
            theWriter.println(tabulation + "<codeStructureActe>"+((String) theDecoupeLigne.ligne.get(11)).trim()+"</codeStructureActe>");
            theWriter.println(tabulation + "<codeActePrecedent>"+((String) theDecoupeLigne.ligne.get(12)).trim()+"</codeActePrecedent>");
            theWriter.println(tabulation + "<codeActeSuivant>"+((String) theDecoupeLigne.ligne.get(13)).trim()+"</codeActeSuivant>");
        } else if (theDecoupeLigne.rubrique.equals("04")) {
        }
    }

    public void traiter_110() throws IOException {
        if (theDecoupeLigne.rubrique.equals("01")) {
            int index = 0;
            theWriter.println(tabulation + "<dateEffet>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</dateEffet>");
            theWriter.println(tabulation + "<dateArreteMinisteriel>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</dateArreteMinisteriel>");
            theWriter.println(tabulation + "<dateJO>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</dateJO>");
            theWriter.println(tabulation + _numValue("admissionRemboursement", theDecoupeLigne, index++, true));
            theWriter.println(tabulation + "<ententePrealable>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</ententePrealable>");
            theWriter.println(tabulation + _arrayDesc("ticketsModerateurs", theDecoupeLigne, index, 5, true)); index+=5;
            theWriter.println(tabulation + _arrayDesc("classesPrescripteurPermises", theDecoupeLigne, index, 10)); index+=10;
            theWriter.println(tabulation + _arrayDesc("typesForfait", theDecoupeLigne, index, 10)); index+=10;
        }
    }

    public void traiter_120() throws IOException {
        if (theDecoupeLigne.rubrique.equals("01")) {
            theWriter.print(_arrayDesc(theDecoupeLigne, 0, 8));
        }
    }

    public void traiter_130() throws IOException {
        if (theDecoupeLigne.rubrique.equals("01")) {
            theWriter.print(_arrayDesc(theDecoupeLigne, 0, 8));
        }
    }

    public void traiter_201() throws IOException {
        if (theDecoupeLigne.rubrique.equals("01")) {
            int index = 0;
            theWriter.println(tabulation + "<codeActivite>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</codeActivite>");
            theWriter.println(tabulation + _arrayDesc("extensionsDocumentaire", theDecoupeLigne, index, 10)); index+=10;
            theWriter.println(tabulation + _arrayDesc("recommendationsMedicales", theDecoupeLigne, index, 5)); index+=5;
            theWriter.println(tabulation + _arrayDesc("typesAgrementRadio", theDecoupeLigne, index, 10)); index+=10;
        }
    }

    public void traiter_210() throws IOException {
        if (theDecoupeLigne.rubrique.equals("01")) {
            int index = 0;
            theWriter.println(tabulation + "<dateEffet>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</dateEffet>");
            theWriter.println(tabulation + "<dateArreteMinisteriel>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</dateArreteMinisteriel>");
            theWriter.println(tabulation + "<dateJO>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</dateJO>");
            theWriter.println(tabulation + _arrayDesc("modificateurs", theDecoupeLigne, index, 10)); index+=10;
            theWriter.println(tabulation + "<categorieMedicale>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</categorieMedicale>");
            theWriter.println(tabulation + _arrayDesc("classesExecutantPermises", theDecoupeLigne, index, 10)); index+=10;
            theWriter.println(tabulation + "<codeRegroupement>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</codeRegroupement>");
        }
    }

    public void traiter_220() throws IOException {
        StringBuffer str = new StringBuffer();

        for (int l_i = 0; l_i < 8; l_i++) {
            String tmp = ((String) theDecoupeLigne.ligne.get(l_i)).trim();

            if (tmp.length() != 0) {
                if (str.length() != 0) str.append(", ");
                str.append("{ codeActe=\"");
                str.append(((String) theDecoupeLigne.ligne.get(l_i)).substring(0, 13).trim());
                str.append("\"; activite=\"");
                str.append(((String) theDecoupeLigne.ligne.get(l_i)).substring(13, 14).trim());
                str.append("\"; regleTarifaire=\"");
                str.append(((String) theDecoupeLigne.ligne.get(l_i + 8)).trim());
                str.append("\"; }");
            }
        }

        theWriter.print(str.toString());
    }

    public void traiter_301() throws IOException {
        if (theDecoupeLigne.rubrique.equals("01")) {
            int index = 0;
            theWriter.println(tabulation + "<codePhase>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</codePhase>");
            theWriter.println(tabulation + "<nombreDeDentsTraitees>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</nombreDeDentsTraitees>");
            theWriter.println(tabulation + _arrayDesc("dentsIncompatibles", theDecoupeLigne, index, 52, true)); index+=52;
            theWriter.println(tabulation + _numValue("ageMinimal", theDecoupeLigne, index++, true));
            theWriter.println(tabulation + _numValue("ageMaximal", theDecoupeLigne, index++, true));
        } else if (theDecoupeLigne.rubrique.equals("50")) {
            int index = 0;
            theWriter.println(tabulation + _numValue("icr", theDecoupeLigne, index++, true));
            theWriter.println(tabulation + "<caractereClassant>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</caractereClassant>");
        }
    }

    public void traiter_310() throws IOException {
        if (theDecoupeLigne.rubrique.equals("01")) {
            int index = 0;
            theWriter.println(tabulation + "<dateEffet>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</dateEffet>");
            theWriter.println(tabulation + "<dateArreteMinisteriel>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</dateArreteMinisteriel>");
            theWriter.println(tabulation + "<dateJO>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</dateJO>");
            theWriter.println(tabulation + _numValue("nombreMaximalSeance", theDecoupeLigne, index++, true));
            theWriter.println(tabulation + "<uniteOeuvre>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</uniteOeuvre>");
            String tmp = ((String) theDecoupeLigne.ligne.get(index++)).trim();
            theWriter.println(tabulation + "<coefficientUniteOeuvre>" + Integer.parseInt(tmp.substring(0, 4)) + "." + tmp.substring(4, 6) + "</coefficientUniteOeuvre>");
            theWriter.println(tabulation + "<codePaiementUniteOeuvre>" + ((String) theDecoupeLigne.ligne.get(index++)).trim() + "</codePaiementUniteOeuvre>");
            tmp = ((String) theDecoupeLigne.ligne.get(index++)).trim();
            theWriter.println(tabulation + "<prixUnitaire>" + Integer.parseInt(tmp.substring(0, 4)) + "." + tmp.substring(4, 6) + "</prixUnitaire>");
            theWriter.println(tabulation + _arrayDesc("majorationsDOM", theDecoupeLigne, index, 4, true)); index+=4;
            tmp = ((String) theDecoupeLigne.ligne.get(index++)).trim();
            theWriter.println(tabulation + "<supplementChargesEnCabinet>" + Integer.parseInt(tmp.substring(0, 4)) + "." + tmp.substring(4, 6) + "</supplementChargesEnCabinet>");
        }
    }

    public void traiter_350() throws IOException {
        if (theDecoupeLigne.rubrique.equals("01")) {
            int index = 0;
            String tmp = ((String) theDecoupeLigne.ligne.get(index++)).trim();

            if (withHistory) theWriter.println(tabulation + "<dateEffet>" + tmp + "</dateEffet>");
            tmp = ((String) theDecoupeLigne.ligne.get(index++)).trim();
            theWriter.println(tabulation + "<scoreTravailMedical>" + Integer.parseInt(tmp.substring(0, 4)) + "." + tmp.substring(4, 6) + "</scoreTravailMedical>");
            tmp = ((String) theDecoupeLigne.ligne.get(index++)).trim();
            theWriter.println(tabulation + "<coutDeLaPratique>" + Integer.parseInt(tmp.substring(0, 4)) + "." + tmp.substring(4, 6) + "</coutDeLaPratique>");
        }
    }

    public void convertToXML(Reader in_reader, Writer in_writer) throws ConverterException {
        try {
            NSLog.debug.appendln("debut traduction nx");
            theWriter = new PrintWriter(in_writer);
            theLineReader = new LineNumberReader(in_reader);
            String l_ligne2;
            DecoupeLigne l_d2;
            int l_lasttypestruct = 999999;
            theLineReader.readLine();
            theLigne = theLineReader.readLine();
            theWriter.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?><ccam>");
            theWriter.println("<params>");
            do {
                theDecoupeLigne = decoupe_ligne(theLigne);
                NSArray l_struct = (NSArray) _struct.objectForKey(theLigne.substring(0, 5));

                if (theDecoupeLigne.type == 11) {
                    boolean moreThanOne = false;

                    theWriter.print("<associationModificateurCoeffOuForfait>(");
                    while (theDecoupeLigne.type == 11) {
                        for (int i = 0; i < 4; i++) {
                            if (moreThanOne) theWriter.print(", ");
                            theWriter.print("{ codeModif=\"" + ((String) theDecoupeLigne.ligne.get((i*5) + 0)).trim() + "\";");
                            theWriter.print(" dateDebut=\"" + ((String) theDecoupeLigne.ligne.get((i*5) + 1)).trim() + "\";");
                            theWriter.print(" dateFin=\"" + ((String) theDecoupeLigne.ligne.get((i*5) + 2)).trim() + "\";");
                            String tmp = ((String) theDecoupeLigne.ligne.get((i*5) + 3)).trim();
                            if (Integer.parseInt(tmp) != 0)
                                theWriter.print(" forfait=\"" +  + Integer.parseInt(tmp.substring(0, 5)) + "." + tmp.substring(5, 7) + "\";");
                            tmp = ((String) theDecoupeLigne.ligne.get((i*5) + 4)).trim();
                            if (Integer.parseInt(tmp) != 0)
                                theWriter.print(" coefficient=\"" +  + Integer.parseInt(tmp.substring(0, 1)) + "." + tmp.substring(1, 4) + "\"; }");

                            moreThanOne = true;
                        }

                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</associationModificateurCoeffOuForfait>");
                } else if (theDecoupeLigne.type == 50) {
                    boolean moreThanOne = false;

                    theWriter.print("<typesDeNote>(");
                    while (theDecoupeLigne.type == 50) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</typesDeNote>");
                } else if (theDecoupeLigne.type == 51) {
                    boolean moreThanOne = false;

                    theWriter.print("<conditionsGenerales>(");
                    while (theDecoupeLigne.type == 51) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</conditionsGenerales>");
                } else if (theDecoupeLigne.type == 52) {
                    boolean moreThanOne = false;

                    theWriter.print("<classesDMT>(");
                    while (theDecoupeLigne.type == 52) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</classesDMT>");
                } else if (theDecoupeLigne.type == 53) {
                    boolean moreThanOne = false;

                    theWriter.print("<exonerationsTM>(");
                    while (theDecoupeLigne.type == 53) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</exonerationsTM>");
                } else if (theDecoupeLigne.type == 54) {
                    boolean moreThanOne = false;

                    theWriter.print("<naturesAssurance>(");
                    while (theDecoupeLigne.type == 54) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</naturesAssurance>");
                } else if (theDecoupeLigne.type == 55) {
                    boolean moreThanOne = false;

                    theWriter.print("<codesAdmissionRemboursement>(");
                    while (theDecoupeLigne.type == 55) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</codesAdmissionRemboursement>");
                } else if (theDecoupeLigne.type == 56) {
                    boolean moreThanOne = false;

                    theWriter.print("<fraisDeplacement>(");
                    while (theDecoupeLigne.type == 56) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</fraisDeplacement>");
                } else if (theDecoupeLigne.type == 57) {
                    boolean moreThanOne = false;

                    theWriter.print("<typesActe>(");
                    while (theDecoupeLigne.type == 57) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</typesActe>");
                } else if (theDecoupeLigne.type == 58) {
                    boolean moreThanOne = false;

                    theWriter.print("<typesForfait>(");
                    while (theDecoupeLigne.type == 58) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</typesForfait>");
                } else if (theDecoupeLigne.type == 59) {
                    boolean moreThanOne = false;

                    theWriter.print("<extensionsDocumentaires>(");
                    while (theDecoupeLigne.type == 59) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</extensionsDocumentaires>");
                } else if (theDecoupeLigne.type == 60) {
                    boolean moreThanOne = false;

                    theWriter.print("<activites>(");
                    while (theDecoupeLigne.type == 60) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</activites>");
                } else if (theDecoupeLigne.type == 61) {
                    boolean moreThanOne = false;

                    theWriter.print("<categoriesMedicales>(");
                    while (theDecoupeLigne.type == 61) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</categoriesMedicales>");
                } else if (theDecoupeLigne.type == 62) {
                    boolean moreThanOne = false;

                    theWriter.print("<codesRegroupement>(");
                    while (theDecoupeLigne.type == 62) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</codesRegroupement>");
                } else if (theDecoupeLigne.type == 63) {
                    boolean moreThanOne = false;

                    theWriter.print("<categoriesSpecialites>(");
                    while (theDecoupeLigne.type == 63) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</categoriesSpecialites>");
                } else if (theDecoupeLigne.type == 64) {
                    boolean moreThanOne = false;

                    theWriter.print("<agrementsRadio>(");
                    while (theDecoupeLigne.type == 64) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</agrementsRadio>");
                } else if (theDecoupeLigne.type == 65) {
                    boolean moreThanOne = false;

                    theWriter.print("<codesPaiementSeance>(");
                    while (theDecoupeLigne.type == 65) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</codesPaiementSeance>");
                } else if (theDecoupeLigne.type == 66) {
                    boolean moreThanOne = false;

                    theWriter.print("<codesPhaseTraitement>(");
                    while (theDecoupeLigne.type == 66) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</codesPhaseTraitement>");
                } else if (theDecoupeLigne.type == 67) {
                    boolean moreThanOne = false;

                    theWriter.print("<numerosDentsIncompatibles>(");
                    while (theDecoupeLigne.type == 67) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</numerosDentsIncompatibles>");
                } else if (theDecoupeLigne.type == 68) {
                    boolean moreThanOne = false;

                    theWriter.print("<caissesDOM>(");
                    while (theDecoupeLigne.type == 68) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</caissesDOM>");
                } else if (theDecoupeLigne.type == 80) {
                    boolean moreThanOne = false;

                    theWriter.print("<codesAssociationNonPrevue>(");
                    while (theDecoupeLigne.type == 80) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</codesAssociationNonPrevue>");
                } else if (theDecoupeLigne.type == 81) {
                    boolean moreThanOne = false;

                    theWriter.print("<reglesTarifaires>(");
                    while (theDecoupeLigne.type == 81) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</reglesTarifaires>");
                } else if (theDecoupeLigne.type == 82) {
                    boolean moreThanOne = false;

                    theWriter.print("<specialites>(");
                    while (theDecoupeLigne.type == 82) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</specialites>");
                } else if (theDecoupeLigne.type == 83) {
                    boolean moreThanOne = false;

                    theWriter.print("<modificateurs>(");
                    while (theDecoupeLigne.type == 83) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" + StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</modificateurs>");
                } else if (theDecoupeLigne.type == 84) {
                    boolean moreThanOne = false;

                    theWriter.print("<codesDMT>(");
                    while (theDecoupeLigne.type == 84) {
                        if (moreThanOne) theWriter.print(", ");
                        theWriter.print(
                            "{ code=\"" + ((String) theDecoupeLigne.ligne.get(0)).trim()
                            + "\"; ivString=\"" +  StringEscapeUtils.escapeXml(((String) theDecoupeLigne.ligne.get(1)).trim()) + "\"; }");
                        moreThanOne = true;
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                    }

                    theWriter.println(")</codesDMT>");
                } else if (theDecoupeLigne.type <= 99) {
                    if (theDecoupeLigne.type != l_lasttypestruct) {
                        l_lasttypestruct = theDecoupeLigne.type;
                    }
                    if (l_struct.get(0).toString().equals("11")) {
                        theLigne = theLineReader.readLine();
                    } else if (l_struct.get(0).toString().equals("1N")) {
                        theLigne = theLineReader.readLine();
                    } else if (l_struct.get(0).toString().equals("N1")) {
                        l_ligne2 = theLineReader.readLine();
                        l_d2 = decoupe_ligne(l_ligne2);
                        while ((l_d2.type == theDecoupeLigne.type) && (l_d2.rubrique.equals(theDecoupeLigne.rubrique))
                                && (Integer.parseInt(l_d2.sequence) > Integer.parseInt(theDecoupeLigne.sequence))) {

                            theDecoupeLigne.type = l_d2.type;
                            theDecoupeLigne.rubrique = l_d2.rubrique;
                            theDecoupeLigne.sequence = l_d2.sequence;
                            l_ligne2 = theLineReader.readLine();
                            l_d2 = decoupe_ligne(l_ligne2);
                        }
                        theLigne = l_ligne2;

                    }
                } else {
                    theWriter.println("</params>");
                    theWriter.println("<actes>");
                     while (theDecoupeLigne.type == 101) {
                        theWriter.println("<Acte>");
                        tabulation = "  ";
                        traiter_101();
                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                        while (theDecoupeLigne.type == 101) {
                            if (theDecoupeLigne.rubrique.equals("50")) {
                                StringBuffer tmp = new StringBuffer();

                                while (theDecoupeLigne.rubrique.equals("50")) {
                                    String l_str = (String) theDecoupeLigne.ligne.get(0);

                                    tmp.append(l_str);
                                    theLigne = theLineReader.readLine();
                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                }

                                theWriter.println(tabulation + "<intituleLong>" +  StringEscapeUtils.escapeXml(tmp.toString().trim()) + "</intituleLong>");
                            }

                            if (theDecoupeLigne.rubrique.equals("51")) {
                                StringBuffer tmp = new StringBuffer();

                                while (theDecoupeLigne.rubrique.equals("51")) {
                                    tmp.append(_arrayDesc(null, theDecoupeLigne, 0, 30, true));
                                    theLigne = theLineReader.readLine();
                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                }

                                theWriter.println(tabulation + "<codesConditionGenerale>(" + tmp.toString() + ")</codesConditionGenerale>");
                            }

                            if (theDecoupeLigne.rubrique.equals("52")) {
                                StringBuffer tmp = new StringBuffer();
                                String code = ((String) theDecoupeLigne.ligne.get(0)).trim();
                                boolean moreThanOne = false;

                                theWriter.print(tabulation + "<notes>(");
                                while (theDecoupeLigne.rubrique.equals("52")) {
                                    if (code.equals(((String) theDecoupeLigne.ligne.get(0)).trim()) == false) {
                                        if (moreThanOne) theWriter.print(", ");
                                        theWriter.print("{ code=\"" + code + "\"; note=\"" +  StringEscapeUtils.escapeXml(tmp.toString()) + "\"; }");
                                        tmp = new StringBuffer();
                                        code = ((String) theDecoupeLigne.ligne.get(0)).trim();
                                        moreThanOne = true;
                                    }

                                    tmp.append(((String) theDecoupeLigne.ligne.get(1)).trim());
                                    theLigne = theLineReader.readLine();
                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                }

                                if (moreThanOne) theWriter.print(", ");
                                theWriter.print("{ code=\"" + code + "\"; note=\"" +  StringEscapeUtils.escapeXml(tmp.toString()) + "\"; }");
                                theWriter.println(")</notes>");
                            }

                            if (theDecoupeLigne.type == 101) {
                                traiter_101();
                                theLigne = theLineReader.readLine();
                                theDecoupeLigne = decoupe_ligne(theLigne);
                            }
                        }

                        if (theDecoupeLigne.type == 110) {
                            if (withHistory) theWriter.println(tabulation + "<historiques>");
                            while (theDecoupeLigne.type == 110) {
                                if (withHistory) theWriter.println(tabulation + "<HistoriqueActe>");
                                traiter_110();
                                theLigne = theLineReader.readLine();
                                theDecoupeLigne = decoupe_ligne(theLigne);
                                while (theDecoupeLigne.type == 120) {
                                    theWriter.print(tabulation + "<procedures>(");
                                    traiter_120();
                                    theLigne = theLineReader.readLine();
                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                    while (theDecoupeLigne.type == 120) {
                                        theWriter.print(", ");
                                        traiter_120();
                                        theLigne = theLineReader.readLine();
                                        theDecoupeLigne = decoupe_ligne(theLigne);
                                    }

                                    theWriter.println(")</procedures>");
                                }

                                while (theDecoupeLigne.type == 130) {
                                    theWriter.print(tabulation + "<incompatibilites>(");
                                    traiter_130();
                                    theLigne = theLineReader.readLine();
                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                    while (theDecoupeLigne.type == 130) {
                                        theWriter.print(", ");
                                        traiter_130();
                                        theLigne = theLineReader.readLine();
                                        theDecoupeLigne = decoupe_ligne(theLigne);
                                    }

                                    theWriter.println(")</incompatibilites>");
                                }

                                if (withHistory)
                                    theWriter.println(tabulation + "</HistoriqueActe>");
                                else {
                                    while (theDecoupeLigne.type < 199) {
                                        theLigne = theLineReader.readLine();
                                        theDecoupeLigne = decoupe_ligne(theLigne);
                                    }
                                }
                            }

                            if (withHistory)
                                theWriter.println(tabulation + "</historiques>");
                        }

                        if (theDecoupeLigne.type == 201) {
                            theWriter.println(tabulation + "<activites>");
                            while (theDecoupeLigne.type == 201) {
                                tabulation = "    ";
                                theWriter.println(tabulation + "<Activite>");
                                tabulation = "      ";
                                if (theDecoupeLigne.rubrique.equals("50")) {
                                    StringBuffer tmp = new StringBuffer();
                                    String code = ((String) theDecoupeLigne.ligne.get(0)).trim();
                                    boolean moreThanOne = false;

                                    theWriter.print(tabulation + "<recommandationsMedicales>(");
                                    while (theDecoupeLigne.rubrique.equals("50")) {
                                        if (code.equals(((String) theDecoupeLigne.ligne.get(0)).trim()) == false) {
                                            if (moreThanOne) theWriter.print(", ");
                                            theWriter.print("{ numero=" + code + "; texte=\"" + tmp.toString() + "\"; }");
                                            tmp = new StringBuffer();
                                            code = ((String) theDecoupeLigne.ligne.get(0)).trim();
                                            moreThanOne = true;
                                        }

                                        tmp.append(((String) theDecoupeLigne.ligne.get(1)).trim());
                                        theLigne = theLineReader.readLine();
                                        theDecoupeLigne = decoupe_ligne(theLigne);
                                    }

                                    if (moreThanOne) theWriter.print(", ");
                                    theWriter.print("{ numero=" + code + "; texte=\"" + tmp.toString() + "\"; }");
                                    theWriter.println(")</recommandationsMedicales>");
                                } else
                                    traiter_201();

                                theLigne = theLineReader.readLine();
                                theDecoupeLigne = decoupe_ligne(theLigne);
                                if (theDecoupeLigne.type == 210) {
                                    if (withHistory) theWriter.println(tabulation + "<historiques>");
                                    while (theDecoupeLigne.type == 210) {
                                        if (withHistory) theWriter.println(tabulation + "<HistoriqueActivite>");
                                        traiter_210();
                                        theLigne = theLineReader.readLine();
                                        theDecoupeLigne = decoupe_ligne(theLigne);
                                        while (theDecoupeLigne.type == 220) {
                                            theWriter.print(tabulation + "<associabilites>(");
                                            traiter_220();
                                            theLigne = theLineReader.readLine();
                                            theDecoupeLigne = decoupe_ligne(theLigne);
                                            while (theDecoupeLigne.type == 220) {
                                                theWriter.print(", ");
                                                traiter_220();
                                                theLigne = theLineReader.readLine();
                                                theDecoupeLigne = decoupe_ligne(theLigne);
                                            }

                                            theWriter.println(")</associabilites>");
                                        }

                                        if (withHistory)
                                            theWriter.println(tabulation + "</HistoriqueActivite>");
                                        else {
                                            while (theDecoupeLigne.type < 299) {
                                                theLigne = theLineReader.readLine();
                                                theDecoupeLigne = decoupe_ligne(theLigne);
                                            }
                                        }
                                    }

                                    if (withHistory)
                                        theWriter.println(tabulation + "</historiques>");
                                }

                                theWriter.println(tabulation + "<phases>");
                                while (theDecoupeLigne.type == 301) {
                                    tabulation = "        ";
                                    theWriter.println(tabulation + "<Phase>");
                                    tabulation = "          ";
                                    traiter_301();
                                    theLigne = theLineReader.readLine();
                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                    while (theDecoupeLigne.type == 301) {
                                        traiter_301();
                                        theLigne = theLineReader.readLine();
                                        theDecoupeLigne = decoupe_ligne(theLigne);
                                    }

                                    if (theDecoupeLigne.type == 310) {
                                        if (withHistory) theWriter.println(tabulation + "<historiques>");
                                        while (theDecoupeLigne.type == 310) {
                                            if (withHistory) theWriter.println(tabulation + "<HistoriquePhase>");
                                            traiter_310();
                                            theLigne = theLineReader.readLine();
                                            theDecoupeLigne = decoupe_ligne(theLigne);
                                            if (withHistory)
                                                theWriter.println(tabulation + "</HistoriquePhase>");
                                            else {
                                                while (theDecoupeLigne.type < 350) {
                                                    theLigne = theLineReader.readLine();
                                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                                }
                                            }
                                        }

                                        if (withHistory)
                                            theWriter.println(tabulation + "</historiques>");
                                    }

                                    if (theDecoupeLigne.type == 350) {
                                        if (withHistory) theWriter.println(tabulation + "<historiquesComplementaires>");
                                        while (theDecoupeLigne.type == 350) {
                                            if (withHistory) theWriter.println(tabulation + "<HistoriquePhaseComplementaire>");
                                            traiter_350();
                                            theLigne = theLineReader.readLine();
                                            theDecoupeLigne = decoupe_ligne(theLigne);
                                            if (withHistory)
                                                theWriter.println(tabulation + "</HistoriquePhaseComplementaire>");
                                            else {
                                                while (theDecoupeLigne.type < 399) {
                                                    theLigne = theLineReader.readLine();
                                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                                }
                                            }
                                        }

                                        if (withHistory)
                                            theWriter.println(tabulation + "</historiquesComplementaires>");
                                    }

                                    tabulation = "        ";
                                    theWriter.println(tabulation + "</Phase>");
                                    while (theDecoupeLigne.type != 399) {
                                        theLigne = theLineReader.readLine();
                                        theDecoupeLigne = decoupe_ligne(theLigne);
                                    }

                                    theLigne = theLineReader.readLine();
                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                }

                                tabulation = "      ";
                                theWriter.println(tabulation + "</phases>");
                                tabulation = "    ";
                                theWriter.println(tabulation + "</Activite>");
                                while (theDecoupeLigne.type != 299) {
                                    theLigne = theLineReader.readLine();
                                    theDecoupeLigne = decoupe_ligne(theLigne);
                                }

                                theLigne = theLineReader.readLine();
                                theDecoupeLigne = decoupe_ligne(theLigne);
                            }

                            tabulation = "  ";
                            theWriter.println(tabulation + "</activites>");
                        }

                        while (theDecoupeLigne.type != 199) {
                            theLigne = theLineReader.readLine();
                            theDecoupeLigne = decoupe_ligne(theLigne);
                        }

                        theLigne = theLineReader.readLine();
                        theDecoupeLigne = decoupe_ligne(theLigne);
                        theWriter.println("</Acte>");
                    }

                    theWriter.println("</actes>");
                }
            } while (theLigne != null && theDecoupeLigne.type != 999);

            theWriter.println("</ccam>");
            in_reader.close();
            theWriter.close();

        } catch (Exception e) {
            ExceptionUtils.rethrowIfNeeded(e);
            NSLog.debug.appendln(e);
            throw new ConverterException(e);

        }
        NSLog.debug.appendln("fin traduction nx");
    }

    public void convertAndIntegrate(Integrator in_integrator,
                                    Reader in_file,
                                    Reader in_xsl,
                                    boolean in_withSchemaValidation) throws ConverterException {

    }

    public void convertFromXML(Reader in_reader, Writer in_writer) throws ConverterException {

    }

    public void convertFromXML(Document in_document, Writer in_writer) throws ConverterException {

    }

    public Properties getConfig() {
        return null;
    }

    public void setConfig(URL in_filePath) throws IOException {

    }

    public void setConfig(Properties in_properties) throws IOException {
    }

}
