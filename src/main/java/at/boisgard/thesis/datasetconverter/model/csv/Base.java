/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model.csv;

/**
 *
 * @author BUERO
 */
public abstract class Base {

    public String name;
    public String synonymsString;

    public String getName() {

        return this.name;
    }

    public String[] getSynonyms() {

        if (synonymsString == null) {
            synonymsString = "";
        }
        return synonymsString.split(";");
    }
}
