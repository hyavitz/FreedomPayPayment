package freedompay.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

/**
 * This pojo class represents a collection of items
 * purchased from a given transaction.  An instance
 * of Items should be populated with an array of type Item,
 * created by the Item class, to represent total items
 * purchased for receipt details.  An instance of this
 * class is converted to / from XML for transaction
 * request / response objects.
 *
 * @author Hunter Yavitz - 8/10/21 - Revision
 */

@Data
public class Items {

    @JacksonXmlElementWrapper(localName = "Item", useWrapping = false)
    @JsonProperty("Item")
    private Item[] items;

    public Items(){}

    public Items(Item[] items) {
        this.items = items;
    }
}