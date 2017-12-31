/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author BUERO
 */
@AllArgsConstructor
public @Data
class NamedEntity {

    public String name;
    public EntityType entityType;
    public int startAt;
    public int endAt;
    public String[] synonyms;

}
